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

        if (rc.getLocation().distanceSquaredTo(robot.getFriendlyArchons().get(0).loc) >= 25
            || nearestEnemy.distanceSquaredTo(robot.getFriendlyArchons().get(0).loc) >= 64) {
            enterTurret();
            return;
        }

        if (nearestEnemy.distanceSquaredTo(rc.getLocation()) > 81) {
            enterTurret();
            return;
        }

        if (rc.getLocation().distanceSquaredTo(robot.getFriendlyArchons().get(0).loc) < 4) {
            enterTurret();
            return;
        }

        enterPortable();

        nav.advanceToward(nearestEnemy);
    }

    private boolean moveAwayFromArchon() throws GameActionException {
        boolean isAdjacent = false;
        for (RobotInfo info : rc.senseNearbyRobots(2, rc.getTeam())) {
            if (info.type == RobotType.ARCHON) {
                isAdjacent = true;
                break;
            }
        }

        if (isAdjacent && !isOnLattice(rc.getLocation())) {
            enterPortable();
            nav.advanceToward(Util.getCenterLocation(rc));
            return true;
        } else {
            enterTurret();
            return false;
        }
    }

    private void enterTurret() throws GameActionException {
        if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
            rc.transform();
        }
    }

    private void enterPortable() throws GameActionException {
        if (rc.getMode() != RobotMode.PORTABLE && rc.canTransform()) {
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
