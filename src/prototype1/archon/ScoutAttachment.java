package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class ScoutAttachment extends Attachment {
    private MapLocation predictedLocation;
    public ScoutAttachment (Robot robot) {

        super(robot);
    }
    @Override
    public void doTurn() throws GameActionException {

    }

}
