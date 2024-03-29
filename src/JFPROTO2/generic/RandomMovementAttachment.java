package JFPROTO2.generic;

import JFPROTO2.Attachment;
import JFPROTO2.Robot;
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
