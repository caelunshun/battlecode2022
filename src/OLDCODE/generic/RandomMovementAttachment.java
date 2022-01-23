package OLDCODE.generic;

import battlecode.common.*;
import OLDCODE.Attachment;
import OLDCODE.Robot;
import OLDCODE.Util;
import OLDCODE.nav.Navigator;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
