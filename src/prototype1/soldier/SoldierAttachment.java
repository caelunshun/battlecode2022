package prototype1.soldier;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {

    }
public void moveInRing() throws GameActionException {

}
}