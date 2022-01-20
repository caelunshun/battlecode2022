package prototype2.attachment.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype2.Attachment;
import prototype2.Robot;

public class ArchonSpotterAttachment extends Attachment {
    public ArchonSpotterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.type == RobotType.ARCHON && info.team == rc.getTeam().opponent()
                && !robot.getEnemyArchons().contains(info.location)) {
                robot.getComms().addEnemyArchon(info.location);
                rc.setIndicatorString("Enemy Archon " + info.location);
            }
        }

        for (MapLocation loc : robot.getEnemyArchons()) {
            if (rc.canSenseLocation(loc)) {
                RobotInfo rob = rc.senseRobotAtLocation(loc);
                if (rob == null || rob.type != RobotType.ARCHON) {
                    robot.getComms().removeEnemyArchon(loc);
                }
            }
        }
        for (int i = 0; i < robot.getFriendlyArchons().size(); i++) {
            MapLocation loc = robot.getFriendlyArchons().get(i).loc;
            if (rc.canSenseLocation(loc)) {
                RobotInfo rob = rc.senseRobotAtLocation(loc);
                if (rob == null || rob.type != RobotType.ARCHON) {
                    robot.getComms().setFriendlyArchonDead(i);
                }
            }
        }
    }
}
