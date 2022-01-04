package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class RandomMovementAttachment extends Attachment {
    public RandomMovementAttachment(Robot robot) {
        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        moveRandom();
    }
    public void moveRandom() throws GameActionException{
        Util.shuffle(Util.DIRECTIONS);
        for(Direction dir : Util.DIRECTIONS) {
            if(rc.canMove(dir)){
                rc.move(dir);
                break;
            }

        }
    }
}
