package prototype1.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;
import java.util.*;

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