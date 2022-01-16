package JFPROTO.prototype1.generic;

import JFPROTO.prototype1.Attachment;
import JFPROTO.prototype1.Robot;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class LeadSpotterAttachment extends Attachment {
    private final boolean[][] visited = new boolean[16][16];

    private static final int BROADCAST_THRESHOLD = 20;

    public LeadSpotterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (visited[rc.getLocation().x / 4][rc.getLocation().y / 4]) {
            return;
        }

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.type == RobotType.MINER) {
                return;
            }
        }

        for (MapLocation loc : rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared)) {
            int lead = rc.senseLead(loc);
            if (lead > BROADCAST_THRESHOLD) {
                robot.getComms().addLeadLocation(loc);
                visited[rc.getLocation().x / 4][rc.getLocation().y / 4] = true;
            }
        }
    }
}
