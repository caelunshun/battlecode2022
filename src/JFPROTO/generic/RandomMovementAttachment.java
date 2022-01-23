package JFPROTO.generic;

import battlecode.common.*;
import JFPROTO.Attachment;
import JFPROTO.Robot;
import JFPROTO.Util;
import JFPROTO.nav.Navigator;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
