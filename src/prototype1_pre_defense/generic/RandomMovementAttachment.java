package prototype1_pre_defense.generic;

import battlecode.common.*;
import prototype1_pre_defense.Attachment;
import prototype1_pre_defense.Robot;
import prototype1_pre_defense.Util;
import prototype1_pre_defense.nav.Navigator;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
