package prototype1.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.comms.LeadCluster;

public class LeadSpotterAttachment extends Attachment {
    public LeadSpotterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > 1) {
                if (!robot.isLocationInLeadCluster(loc)) {
                    robot.getComms().addLeadCluster(new LeadCluster(loc, 0));
                    rc.setIndicatorString("Lead Cluster " + loc);
                    robot.update(); // ensure new lead cluster is registered
                }
            }
        }
    }
}
