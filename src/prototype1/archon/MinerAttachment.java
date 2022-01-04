package prototype1.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinerAttachment extends Attachment {
    private final Navigator nav;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!mine()) {
            lookForMetals();
        }
    }

    private boolean mine() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().actionRadiusSquared)) {
            if (!rc.isActionReady()) {
                return true;
            }
            while (rc.canMineGold(loc)) {
                rc.mineGold(loc);
                rc.setIndicatorString("Mined gold");
            }
            while (rc.senseLead(loc) > 1 && rc.canMineLead(loc)) {
                rc.mineLead(loc);
                rc.setIndicatorString("Mined lead");
            }
        }
        return false;
    }

    private void lookForMetals() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > 1 || rc.senseGold(loc) > 0) {
                nav.advanceToward(loc);
                return;
            }
        }
    }
}
