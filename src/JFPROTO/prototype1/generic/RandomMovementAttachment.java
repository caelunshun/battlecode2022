package JFPROTO.prototype1.generic;

import JFPROTO.prototype1.Attachment;
import JFPROTO.prototype1.Robot;
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
