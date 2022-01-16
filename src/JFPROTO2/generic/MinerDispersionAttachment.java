package JFPROTO2.generic;

import JFPROTO2.Attachment;
import JFPROTO2.Robot;
import JFPROTO2.nav.Navigator;
import battlecode.common.GameActionException;

public class MinerDispersionAttachment extends Attachment {
    private Navigator nav;

    public MinerDispersionAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
    }

}