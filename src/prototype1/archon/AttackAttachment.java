package prototype1.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;

public class AttackAttachment extends Attachment {

    public AttackAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        lookForEnemy();
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