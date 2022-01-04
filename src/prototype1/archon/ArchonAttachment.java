package prototype1.archon;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;

public class ArchonAttachment extends Attachment {
    public ArchonAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        build();
    }

    private void build() throws GameActionException {

    }

    private Direction getAvailableBuildDirection() throws GameActionException {
        for (Direction dir : Util.DIRECTIONS) {
            if (rc.senseRobotAtLocation(rc.getLocation().add(dir)) == null) {
                return dir;
            }
        }
        return Direction.CENTER;
    }
}
