package JFPROTO.generic;

import JFPROTO.Attachment;
import JFPROTO.Robot;
import battlecode.common.GameActionException;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
