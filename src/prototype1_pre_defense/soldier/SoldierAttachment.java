package prototype1_pre_defense.soldier;

import battlecode.common.*;
import prototype1_pre_defense.Attachment;
import prototype1_pre_defense.Robot;
import prototype1_pre_defense.Util;
import prototype1_pre_defense.nav.Navigator;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {

    }

}