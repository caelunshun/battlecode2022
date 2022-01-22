package prototype2.attachment.sage;

import battlecode.common.*;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.Util;
import prototype2.comms.CryForHelp;
import prototype2.nav.Navigator;

public class SageMicroAttachment extends Attachment {
    private SageAttackAttachment attack;
    private Navigator nav;

    // states reset each turn and used for micro

    private int turnsUntilAttackIsReady;
    private int numEnemies;
    private int numEnemiesAtEdgeOfVision;
    private int numCloseEnemies;
    private int numFriendlies;
    private RobotInfo closestEnemy;

    public SageMicroAttachment(Robot robot) {
        super(robot);
        this.attack = new SageAttackAttachment(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        updateStates();
        if (closestEnemy != null) {
            doCombatMicro();
        }
    }

    private void updateStates() {
        turnsUntilAttackIsReady = rc.getActionCooldownTurns() / GameConstants.COOLDOWNS_PER_TURN;

        numEnemies = 0;
        numEnemiesAtEdgeOfVision = 0;
        closestEnemy = null;
        numCloseEnemies = 0;
        numFriendlies = 0;

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            ++numEnemies;
            if (info.location.distanceSquaredTo(rc.getLocation()) >= 25) {
                ++numEnemiesAtEdgeOfVision;
            }
            if (info.location.distanceSquaredTo(rc.getLocation()) <= 2) {
                ++numCloseEnemies;
            }
            if (closestEnemy == null || info.location.distanceSquaredTo(rc.getLocation()) < closestEnemy.location.distanceSquaredTo(rc.getLocation())) {
                closestEnemy = info;
            }
         }

         if (closestEnemy == null) return;
         for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.location.distanceSquaredTo(closestEnemy.location) <= 36) {
                ++numFriendlies;
            }
         }
    }

    private void doCombatMicro() throws GameActionException {
        if (turnsUntilAttackIsReady > 2) {
            retreat();
            return;
        }

        if (rc.getHealth() <= numEnemies * 3) {
            attack.doTurn();
            retreat();
            return;
        }

        if (numFriendlies >= 3) {
            advance();
        }

        if (numEnemiesAtEdgeOfVision <= 1 || numCloseEnemies >= 2) {
            attack.doTurn();
            retreat();
        }

        if (numEnemies > 2) {
            issueCryForHelp();
        }
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

    private void issueCryForHelp() throws GameActionException {
        CryForHelp cry = new CryForHelp(closestEnemy.location, numEnemies, rc.getRoundNum());
        robot.getComms().addCryForHelp(cry);
    }
}
