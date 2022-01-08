package prototype1.builder;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;

public class SacrificeBuilderAttachment extends Attachment {
    private Navigator nav;

    public SacrificeBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (rc.senseLead(rc.getLocation()) == 0) {
            sacrificeForTheGreaterGood();
        } else {
            MapLocation closest = null;
            for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
                if (rc.senseLead(loc) == 0 && !rc.isLocationOccupied(loc)) {
                    if (closest == null
                        || rc.getLocation().distanceSquaredTo(loc)
                            < rc.getLocation().distanceSquaredTo(closest)) {
                        closest = loc;
                    }
                }
            }
            if (closest != null) {
                nav.advanceToward(closest);
            } else {
                nav.advanceToward(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            }
        }
    }

    private void sacrificeForTheGreaterGood() throws GameActionException {
        if (rc.senseLead(rc.getLocation()) == 0) {
            rc.disintegrate(); // RIP
        }
    }
}
