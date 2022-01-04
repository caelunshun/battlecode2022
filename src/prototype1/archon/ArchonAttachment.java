package prototype1.archon;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;

public class ArchonAttachment extends Attachment {
    private int buildIndex = 0;

    public ArchonAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        build();
    }

    private void build() throws GameActionException {
        RobotType type;
        if (buildIndex % 3 < 2) {
            type = RobotType.MINER;
        } else {
            type = RobotType.SOLDIER;
        }

        if (tryBuild(type)) {
            ++buildIndex;
        }
    }

    private boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = getAvailableBuildDirection();
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Built a " + type);
            return true;
        }
        return false;
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
