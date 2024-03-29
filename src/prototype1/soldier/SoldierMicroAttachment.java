package prototype1.soldier;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.comms.CryForHelp;
import prototype1.generic.DispersionAttachment;
import prototype1.nav.Navigator;

import java.util.Arrays;

public class SoldierMicroAttachment extends Attachment {
    private final Navigator nav;

    // States valid for current round.
    // Used for combat micro.

    // number of enemy soldiers in the vision radius
    private int numEnemies;
    // closest enemy (prioritizing soldiers over other units)
    private RobotInfo closestEnemy;
    // number of friendly soldiers within attack radius + 1
    // of closestEnemy
    private int numFriendlies;
    // other cries for help
    private CryForHelp[] criesForHelp;

    public SoldierMicroAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        updateStates();
        doCombatMicro();
    }

    private void updateStates() throws GameActionException {
        numEnemies = 0;
        closestEnemy = null;
        numFriendlies = 0;
        criesForHelp = robot.getComms().getCriesForHelp();

        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.team == rc.getTeam().opponent()) {
                if (info.type.canAttack()) {
                    ++numEnemies;
                }

                if (closestEnemy == null
                        || (!closestEnemy.type.canAttack() && info.type.canAttack())
                        || rc.getLocation().distanceSquaredTo(info.location) < rc.getLocation().distanceSquaredTo(closestEnemy.location)) {
                    closestEnemy = info;
                }
            }
        }

        if (closestEnemy == null) return;

        // Second pass: friendlies
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.ID == rc.getID()) continue;
            if (closestEnemy.location.distanceSquaredTo(info.location) <= 20) {
                ++numFriendlies;
            }
        }
    }

    private void updateRush() throws GameActionException {
        MapLocation closestEnemyArchon = Util.getClosest(rc.getLocation(), robot.getEnemyArchons());
        if (closestEnemyArchon != null && closestEnemyArchon.distanceSquaredTo(rc.getLocation()) <= 20) {
            if (isOutnumbered()) {
                robot.getComms().setRushingArchon(null);
            } else {
                robot.getComms().setRushingArchon(closestEnemyArchon);
            }
        }

        if (robot.getComms().getRushingArchon() != null
            && !robot.getEnemyArchons().contains(robot.getComms().getRushingArchon())) {
            robot.getComms().setRushingArchon(null);
        }
    }

    private void doCombatMicro() throws GameActionException {
        if (closestEnemy == null) return;

        // Goal: stay exactly within attack radius of
        // the closest enemy, while avoiding all other enemies.
        // HOWEVER: if we're outnumbered, then we issue a cry
        // for help instead, then retreat.
        if (isOutnumbered() && !isDefendingArchon()) {
            issueCryForHelp();
            retreat();
            rc.setIndicatorString("Outnumbered from " + closestEnemy.location);
        } else {
            if (rc.getLocation().distanceSquaredTo(closestEnemy.location) < 9) {
                retreat(); // keep at minimum distance away
            } else if (rc.getLocation().distanceSquaredTo(closestEnemy.location) > rc.getType().actionRadiusSquared) {
                advance(); // get close enough to shoot
            }
        }
    }

    private boolean isDefendingArchon() {
        for (MapLocation loc : robot.getFriendlyArchons()) {
            if (loc.distanceSquaredTo(rc.getLocation()) <= 25) {
                return true;
            }
        }
        return false;
    }

    private void issueCryForHelp() throws GameActionException {
        CryForHelp cry = new CryForHelp(closestEnemy.location, numEnemies, rc.getRoundNum());
        robot.getComms().addCryForHelp(cry);
    }

    private void advance() throws GameActionException {
        if (!rc.isMovementReady()) return;
        Direction idealDir = rc.getLocation().directionTo(closestEnemy.location);
        Direction dir = getOptimalDir(idealDir);
        if (dir != null) {
            rc.move(dir);
            rc.setIndicatorString("Advanced " + dir + " (ideal = " + idealDir + ")");
        } else {
            rc.setIndicatorString("Failed to Advance " + idealDir);
        }
    }

    private void retreat() throws GameActionException {
        if (!rc.isMovementReady()) return;
        Direction idealDir = closestEnemy.location.directionTo(rc.getLocation());
        Direction dir = getOptimalDir(idealDir);
        if (dir != null) {
            rc.move(dir);
            rc.setIndicatorString("Retreated " + dir + " (ideal = " + idealDir + ")");
        } else {
            rc.setIndicatorString("Failed to Retreat " + idealDir);
        }
    }

    private Direction getOptimalDir(Direction ideal) throws GameActionException {
        Direction best = null;
        double bestScore = 0;
        for (Direction dir : Util.DIRECTIONS) {
            if (!rc.canMove(dir)) continue;
            double angle = Util.getAngle(dir, ideal);
            if (angle > Math.PI / 2) {
                continue;
            }
            MapLocation loc = rc.getLocation().add(dir);
            int rubble = rc.senseRubble(loc);
            double score = angle / (Math.PI / 2) * 20 + rubble;
            if (best == null || score < bestScore) {
                best = dir;
                bestScore = score;
            }
        }
        return best;
    }

    private boolean isOutnumbered() {
        return numEnemies > numFriendlies * 0.8;
    }
}
