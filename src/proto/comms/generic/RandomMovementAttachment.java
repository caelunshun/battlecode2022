package proto.comms.generic;

import battlecode.common.GameActionException;
import proto.comms.Attachment;
import proto.comms.Robot;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
