package JFPROTO.prototype1.generic;

import JFPROTO.prototype1.Attachment;
import JFPROTO.prototype1.Robot;
import JFPROTO.prototype1.nav.Navigator;
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