package prototype1.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;

public class AttackAttachment extends Attachment {
    private Navigator nav;

    public AttackAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        advanceTowardEnemyArchon();
        lookForEnemy();
    }

    private void advanceTowardEnemyArchon() throws GameActionException {
        MapLocation closestArchon = null;
        for (MapLocation loc : robot.getEnemyArchons()) {
            if (closestArchon == null
                || closestArchon.distanceSquaredTo(rc.getLocation()) > loc.distanceSquaredTo(rc.getLocation())) {
                closestArchon = loc;
            }
        }

        if (closestArchon != null && closestArchon.distanceSquaredTo(rc.getLocation()) > 8) {
            nav.advanceToward(closestArchon);
        }
    }

    public boolean lookForEnemy() throws GameActionException {
        if (!rc.isActionReady()) {
            return false;
        }
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        //looks for archons first
        //canbeoptimized find locations on first loop through
        for (MapLocation location : locations) {
            if (rc.canSenseRobotAtLocation(location) && rc.senseRobotAtLocation(location) != null) {
                if (rc.senseRobotAtLocation(location).getType() == RobotType.ARCHON && rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()) {
                    if (rc.canAttack(location)) {
                        rc.attack(location);
                        return true;
                    }
                }
            }
        }
        for (MapLocation location : locations) {
            if (rc.canSenseRobotAtLocation(location) && rc.senseRobotAtLocation(location) != null) {
                if (rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()) {
                    if (rc.canAttack(location)) {
                        rc.attack(location);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}