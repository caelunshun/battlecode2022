package prototype1_01_06_2022.generic;

import battlecode.common.*;
import prototype1_01_06_2022.Attachment;
import prototype1_01_06_2022.Robot;
import prototype1_01_06_2022.Util;
import prototype1_01_06_2022.nav.Navigator;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        robot.moveRandom();
    }

}
