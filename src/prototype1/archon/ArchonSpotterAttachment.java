package prototype1.archon;

import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;

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
    }
}
