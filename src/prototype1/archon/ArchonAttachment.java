package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.generic.SymmetryType;

import java.util.ArrayList;
import java.util.List;

public class ArchonAttachment extends Attachment {
    private int lastBuiltIndex = -1;

    public static int SOLDIER_BUILDING_OFFSET = 3;

    private final int numArchons;

    private boolean isInDanger = false;

    public static final int tiebreakerRound = 1500;

    public ArchonAttachment(Robot robot) throws GameActionException {
        super(robot);
        robot.getComms().addFriendlyArchon(rc.getLocation());
        numArchons = rc.getArchonCount();
    }

    @Override
    public void doTurn() throws GameActionException {
        isInDanger = isInDanger();
        robot.getComms().setArchonInDanger(robot.getFriendlyArchons().indexOf(rc.getLocation()), isInDanger);
        if (robot.getComms().getSymmetryType() != null) {
            rc.setIndicatorString("Symmetry: " + robot.getComms().getSymmetryType());
        } else {
            rc.setIndicatorString("Symmetry Unknown");
        }
        if (rc.getRoundNum() < tiebreakerRound) {
            build();
        } else {
            tiebreakerMode();
        }
        repair();
        computeSymmetry();

        if (rc.getRoundNum() == 2) {
            initialFriendlyArchons.addAll(robot.getFriendlyArchons());
        }

        if (isInDanger) {
            rc.setIndicatorString("In Danger");
        }
        if (rc.getRoundNum() == 2) {
            initialFriendlyArchons.addAll(robot.getFriendlyArchons());
        }
        initiateRush();

        if (robot.getComms().getRushingArchon() != null) {
            rc.setIndicatorString("Rushing " + robot.getComms().getRushingArchon());
        }
        if (isInDanger) {
            rc.setIndicatorString("In Danger");
        }
    }

    private void build() throws GameActionException {
        int currentBuildIndex = robot.getComms().getBuildIndex();
        if (currentBuildIndex - lastBuiltIndex < robot.getFriendlyArchons().size() - 1 && !isInDanger) {
            return;
        }

        RobotType type;
        if (isInDanger) {
            type = RobotType.SOLDIER;
        } else if (currentBuildIndex < SOLDIER_BUILDING_OFFSET - 1) {
            type = RobotType.MINER;
        } else if (currentBuildIndex < SOLDIER_BUILDING_OFFSET + 2) {
            type = RobotType.SOLDIER;
        } else if (currentBuildIndex < 6) {
            type = RobotType.MINER;
        } else if (rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
            type = RobotType.SAGE;
        } else if (currentBuildIndex % 6 < 1) {
            type = RobotType.BUILDER;
        } else if (currentBuildIndex % 6 < 3) {
            type = RobotType.MINER;
        } else {
            type = RobotType.SOLDIER;
        }

        rc.setIndicatorString("Build #" + currentBuildIndex);

        if (rc.getTeamLeadAmount(rc.getTeam()) < 200
                && rc.getRoundNum() > 60
                && !isInDanger) {
            return;
        }

        if (tryBuild(type)) {
            ++currentBuildIndex;
            robot.getComms().setBuildIndex(currentBuildIndex);
            lastBuiltIndex = currentBuildIndex;
        }
    }

    private boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = getAvailableBuildDirection();
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Built a " + type);
            return true;
        }
        return false;
    }

    private Direction getAvailableBuildDirection() throws GameActionException {
        for (Direction dir : Util.DIRECTIONS) {
            if (rc.senseRobotAtLocation(rc.getLocation().add(dir)) == null) {
                return dir;
            }
        }
        return Direction.CENTER;
    }

    private void repair() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        for (RobotInfo robs : nearbyRobots) {
            if (robs.getHealth() < robs.getType().health) {
                if (rc.canRepair(robs.getLocation())) {
                    rc.repair(robs.getLocation());
                    return;
                }
            }
        }
    }

    List<MapLocation> initialEnemyArchons = new ArrayList<>();
    List<MapLocation> initialFriendlyArchons = new ArrayList<>();
    List<MapLocation> destroyedFriendlyArchons = new ArrayList<>();

    private void computeSymmetry() throws GameActionException {
        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (!initialEnemyArchons.contains(enemy)) {
                initialEnemyArchons.add(enemy);
            }
        }
        for (MapLocation initialEnemy : initialEnemyArchons) {
            if (!robot.getEnemyArchons().contains(initialEnemy)
                    && !destroyedFriendlyArchons.contains(initialEnemy)) {
                destroyedFriendlyArchons.add(initialEnemy);
            }
        }

        List<SymmetryType> possibleSymmetry = new ArrayList<>();
        for (SymmetryType symmetry : SymmetryType.values()) {
            boolean symmetryWorks = true;
            List<MapLocation> usedReflections = new ArrayList<>();
            for (MapLocation enemyLoc : initialEnemyArchons) {
                boolean works = false;
                for (MapLocation ourLoc : initialFriendlyArchons) {
                    if (usedReflections.contains(ourLoc)) continue;
                    if (symmetry.getSymmetryLocation(ourLoc, rc).equals(enemyLoc)) {
                        System.out.println(symmetry + ", " + ourLoc + ", E: " + enemyLoc);
                        works = true;
                        usedReflections.add(ourLoc);
                        break;
                    }
                }
                if (!works) symmetryWorks = false;
            }
            if (symmetryWorks) {
                possibleSymmetry.add(symmetry);
            }
        }

        if (possibleSymmetry.size() == 1) {
            robot.getComms().setSymmetryType(possibleSymmetry.get(0));
        } else if (!possibleSymmetry.isEmpty()) {
            if (initialEnemyArchons.size() == numArchons) {
                // Multiple possible symmetry types, but they all work,
                // as we know the entire map.
                robot.getComms().setSymmetryType(possibleSymmetry.get(0));
            } else {
                rc.setIndicatorString("SU: " + possibleSymmetry);
            }
        }

        // Add enemy archons based on symmetry
        SymmetryType symmetry = robot.getComms().getSymmetryType();
        if (symmetry == null) return;
        for (MapLocation friendly : initialFriendlyArchons) {
            MapLocation enemy = symmetry.getSymmetryLocation(friendly, rc);
            if (!robot.getEnemyArchons().contains(enemy) && !destroyedFriendlyArchons.contains(enemy)) {
                robot.getComms().addEnemyArchon(enemy);
                robot.update();
            }
        }
    }

    private void tiebreakerMode() throws GameActionException {
        if (rc.getRoundNum() % 50 == 0 && rc.getTeamGoldAmount(rc.getTeam()) > 2000) {
            if (rc.canBuildRobot(RobotType.BUILDER, getAvailableBuildDirection())) {
                rc.buildRobot(RobotType.BUILDER, getAvailableBuildDirection());
            }
        }
    }

    private boolean isInDanger() throws GameActionException {
        // Check if the total health of nearby enemy robots (that can attack)
        // is greater than the total health of our nearby robots (that can attack).
        int ourHealth = 0;
        int enemyHealth = 0;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.ID == rc.getID()) continue;
            if (info.type.canAttack()) {
                if (info.team == rc.getTeam()) {
                    ourHealth += info.health;
                } else {
                    enemyHealth += info.health;
                }
            }
        }

        return enemyHealth > ourHealth;
    }

    private void initiateRush() throws GameActionException {
        if (isInDanger) {
            robot.getComms().setRushingArchon(null);
            return;
        }

        if (rc.getRoundNum() < 200) {
            return;
        }

        if (robot.getComms().getRushingArchon() != null) return;

        if (robot.getEnemyArchons().isEmpty()) return;

        robot.getComms().setRushingArchon(Util.getClosest(rc.getLocation(), robot.getEnemyArchons()));
    }
}
