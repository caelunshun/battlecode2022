package prototype1_01_12_2022.archon;

import battlecode.common.*;
import prototype1_01_12_2022.Attachment;
import prototype1_01_12_2022.BotConstants;
import prototype1_01_12_2022.Robot;
import prototype1_01_12_2022.Util;
import prototype1_01_12_2022.build.BuildType;
import prototype1_01_12_2022.build.BuildWeightTable;
import prototype1_01_12_2022.comms.BecomeSwarmLeader;
import prototype1_01_12_2022.generic.SymmetryType;
import prototype1_01_12_2022.nav.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArchonAttachment extends Attachment {
    private int lastBuiltIndex = -1;

    public static int SOLDIER_BUILDING_OFFSET = 3;

    private final int numArchons;

    private boolean isInDanger = false;

    public static final int tiebreakerRound = 1850;

    private BuildWeightTable buildWeights = new BuildWeightTable();

    private boolean isLead;

    private boolean isMapTestSmall;
    private boolean isMovingAwayFromRubble = false;
    private Navigator nav;
    private MapLocation idealSpot;
    public ArchonAttachment(Robot robot) throws GameActionException {
        super(robot);

        this.nav = new Navigator(robot);
        idealSpot = new MapLocation(rc.getLocation().x,rc.getLocation().y);
        robot.getComms().addFriendlyArchon(rc.getLocation());
        numArchons = rc.getArchonCount();

        isMapTestSmall = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared).length
                > 40;
    }

    @Override
    public void doTurn() throws GameActionException {
        if(rc.getRoundNum() == 1){
            moveAway();
        }
        if(isMovingAwayFromRubble && rc.getRoundNum() != 1){
            moveAway();
        }

        isLead = robot.getFriendlyArchons().indexOf(rc.getLocation()) == 0;
        isInDanger = isInDanger();
        robot.getComms().setArchonInDanger(robot.getFriendlyArchons().indexOf(rc.getLocation()), isInDanger);
        if (robot.getComms().getSymmetryType() != null) {
            rc.setIndicatorString("Symmetry: " + robot.getComms().getSymmetryType());
        } else {
            rc.setIndicatorString("Symmetry Unknown");
        }

        incrementBuildWeights();
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

        if (isLead) {
            initiateRush();
        }

        if (isInDanger) {
            rc.setIndicatorString("In Danger");
        }
    }

    private void incrementBuildWeights() throws GameActionException {
        // Increment the weights in the build table based on priorities.
        if (rc.getRoundNum() < 100) {
            buildWeights.addWeight(BuildType.MINER, 60);
        } else {
            buildWeights.addWeight(BuildType.MINER, 5);
        }

        if (robot.isAnyArchonInDanger()) {
            buildWeights.addWeight(BuildType.DEFENSE_SOLDIER, 200);
        } else if (rc.getRoundNum() >= 60) {
            buildWeights.addWeight(BuildType.SOLDIER, 30);
        }

        if (rc.getRoundNum() > BotConstants.DEFENSE_MODE_TURN) {
            int weight;
            if (isMapTestSmall) {
                if (rc.getTeamLeadAmount(rc.getTeam()) > 1000) {
                    weight = 5;
                } else {
                    weight = 2;
                }
            } else {
                weight = 50;
            }
            buildWeights.addWeight(BuildType.BUILDER, weight);
        }
    }

    private void build() throws GameActionException {
        int currentBuildIndex = robot.getComms().getBuildIndex();
        if (currentBuildIndex - lastBuiltIndex < robot.getFriendlyArchons().size() - 1 && !isInDanger) {
            // No need to balance builds if we have tons of lead.
            if (rc.getTeamLeadAmount(rc.getTeam()) < 1000) {
                return;
            }
        }

        BuildType buildType = buildWeights.getHighestWeight();

        rc.setIndicatorString(buildType.toString());

        if (buildType == BuildType.DEFENSE_SOLDIER && !isInDanger && robot.isAnyArchonInDanger()) {
            return;
        }

        if (tryBuild(buildType.getRobotType())) {
            ++currentBuildIndex;
            robot.getComms().setBuildIndex(currentBuildIndex);
            lastBuiltIndex = currentBuildIndex;
            buildWeights.clearWeight(buildType);

            if (robot.getFriendlyArchons().size() == 1) {
                // We can build again if we have more lead.
                build();
            }
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
        Direction[] list = Arrays.copyOf(Util.DIRECTIONS, Util.DIRECTIONS.length);
        Util.shuffle(list);
        for (Direction dir : list) {
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

    int inDangerTurns = 0;
    private boolean isInDanger() throws GameActionException {
        // Check if the total health of nearby enemy robots (that can attack)
        // is greater than the total health of our nearby robots (that can attack).
        double ourHealth = 0;
        double enemyHealth = 0;
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

        boolean inDanger = enemyHealth * 1.6 > ourHealth;

        if (inDanger) inDangerTurns = 20;
        if (inDangerTurns-- > 0) {
            inDanger = true;
        }
        return inDanger;
    }

    int lastRushTurn = -1;
    private void initiateRush() throws GameActionException {
        // robot.getComms().setRushingArchon(null);

        if (rc.getRoundNum() < 1200) {
            return;
        }

        if (rc.getRoundNum() - lastRushTurn < 200) {
            return;
        }
        rc.setIndicatorString(robot.getEnemyArchons().toString());

        if (robot.getEnemyArchons().isEmpty()) return;

        robot.getComms().setRushingArchon(Util.getClosest(rc.getLocation(), robot.getEnemyArchons()));
        lastRushTurn = rc.getRoundNum();
    }
    public void moveAway() throws GameActionException{
       MapLocation[] locs =  rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
       int spot = -1;
       if(locs.length == 0){
           isMovingAwayFromRubble = false;
           return;
       }
       if(rc.senseRubble(rc.getLocation()) < 50 && rc.getMode() == RobotMode.TURRET){

           isMovingAwayFromRubble = false;
           return;
       }
       isMovingAwayFromRubble = true;
       double minRubble = rc.senseRubble(rc.getLocation());
       for(int i = 0; i < locs.length; i++){
           double rub = rc.senseRubble(locs[i]) + Math.pow( rc.getLocation().distanceSquaredTo(locs[i]), 0.2);
           if( (rub + Math.pow(rc.getLocation().distanceSquaredTo(locs[i]), 0.2 ) ) < minRubble){
               minRubble = rub;
               spot = i;
           }
       }
       if(spot == -1){
           if(rc.getMode() == RobotMode.TURRET){
               isMovingAwayFromRubble = false;
               return;
           }
           if(rc.canTransform()){
               rc.transform();
           }
       } else {
           idealSpot = locs[spot];
       }
            moveToLoc(idealSpot);

    }
    public void moveToLoc(MapLocation toGo) throws GameActionException{
        if(rc.getMode() == RobotMode.TURRET){
            if(rc.canTransform()){
                rc.transform();
            }
            return;
        }
        if(rc.getLocation().equals(toGo)){
            if(rc.getMode() == RobotMode.TURRET){
                return;
            }
            if(rc.canTransform()){
                rc.transform();
                isMovingAwayFromRubble = false;
            }
            robot.getComms().addFriendlyArchon(rc.getLocation());
            return;
        }
        nav.advanceToward(toGo);
    }
}
