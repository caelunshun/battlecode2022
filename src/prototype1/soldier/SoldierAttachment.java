package prototype1.soldier;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    private int waitingTime = 0;
    private MapLocation latticeLocation;

    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        moveToLatticePosition();
    }

    public void moveToLatticePosition() throws GameActionException {
        if (rc.getLocation().equals(latticeLocation)) {
            rc.setIndicatorString("AT POSITION");
            return;

        }
        if (latticeLocation != null && !rc.canSenseRobotAtLocation(latticeLocation)) {
            nav.advanceToward(latticeLocation);
            waitingTime = 0;
        } else {
            latticeLocation = findLatticePosition(waitingTime + 100);
            waitingTime += 20;
            rc.setIndicatorString("LOOKING FOR LATTICE LOCATION");
        }

    }

    public MapLocation findLatticePosition(int sizeOfSearch) throws GameActionException {
        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(robot.getHomeArchon().getLocation(), sizeOfSearch);
        for (MapLocation loc : locs) {
            if ((loc.x % 2) != (loc.y % 2)) {
                if (!rc.canSenseRobotAtLocation(loc) && robot.getHomeArchon().getLocation().distanceSquaredTo(loc) >= 5) {
                    return loc;
                }
            }
        }
        return null;
    }

}