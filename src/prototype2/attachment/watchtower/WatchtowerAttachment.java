package prototype2.attachment.watchtower;

import prototype2.Util;
import battlecode.common.*;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.comms.EnemySpottedLocation;
import prototype2.nav.Navigator;

import java.util.Arrays;

public class WatchtowerAttachment extends Attachment {
    private Navigator nav;

    public WatchtowerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!moveAwayFromArchon()) {
            optimizeLocation();
        }
    }

    private void optimizeLocation() throws GameActionException {
        MapLocation nearestEnemy = getNearestEnemy();

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        if (visibleEnemies.length > 0) {
            enterTurret();
            return;
        }

        if (nearestEnemy == null) {
            enterTurret();
            return;
        }

        double dx = nearestEnemy.x - robot.getLeadArchon().x;
        double dy = nearestEnemy.y - robot.getLeadArchon().y;
        double enemyAngle = Math.atan2(dy, dx);
        double ourAngle = Math.atan2(rc.getLocation().y - robot.getLeadArchon().y,
                rc.getLocation().x - robot.getLeadArchon().x);
        if (Util.cmpAngles(enemyAngle, ourAngle) > Math.PI / 2 && !rc.getLocation().equals(nearestEnemy)) {
            enterPortable();
            nav.advanceToward(nearestEnemy);
        } else {
            enterTurret();
        }
    }

    private boolean moveAwayFromArchon() throws GameActionException {
        boolean isAdjacent = false;
        for (RobotInfo info : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (info.type == RobotType.ARCHON) {
                isAdjacent = true;
                break;
            }
        }

        if (isAdjacent) {
            enterPortable();
            nav.advanceToward(Util.getCenterLocation(rc));
            return true;
        } else {
            enterTurret();
        }

        return false;
    }

    private void moveOntoLattice() throws GameActionException {
        Direction best = null;
        int leastRubble = 0;
        for (Direction dir : Util.DIRECTIONS) {
            MapLocation loc = rc.getLocation().add(dir);
            if (!isOnLattice(loc)) continue;
            if (!rc.canSenseLocation(loc)) continue;
            if (!rc.canMove(dir)) continue;

            int rubble = rc.senseRubble(loc);
            if (best == null || rubble < leastRubble) {
                best = dir;
                leastRubble = rubble;
            }
        }

        if (best == null) {
            robot.moveRandom();
        } else if (rc.canMove(best)) {
            rc.move(best);
        }
    }

    private void enterTurret() throws GameActionException {
        if (!isOnLattice(rc.getLocation())) {
            moveOntoLattice();
        } else if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
            rc.transform();
        }
    }

    private void enterPortable() throws GameActionException {
        if (rc.getMode() == RobotMode.TURRET && rc.canTransform()) {
            rc.transform();
        }
    }

    private MapLocation getNearestEnemy() throws GameActionException {
        MapLocation nearest = null;
        for (EnemySpottedLocation loc : robot.getComms().getEnemySpottedLocations()) {
            if (loc == null) continue;
            if (nearest == null || loc.loc.distanceSquaredTo(rc.getLocation()) < nearest.distanceSquaredTo(rc.getLocation())) {
                nearest = loc.loc;
            }
        }
        return nearest;
    }

    private boolean isOnLattice(MapLocation loc) throws GameActionException {
        return loc.x % 2 == loc.y % 2;
    }
}
