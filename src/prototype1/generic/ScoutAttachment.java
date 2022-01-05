package prototype1.generic;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

import java.util.List;

public class ScoutAttachment extends Attachment {
    private MapLocation predictedLocation = null;
    private SymmetryType type;
    private boolean alreadyFound = false;
    private Navigator nav;

    public ScoutAttachment(Robot robot, SymmetryType type) {
        super(robot);
        this.type = type;
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!alreadyFound) {
            if (predictedLocation == null) {
                predictedLocation = goToSpot(type);
            }
            if (predictedLocation != null) {
                nav.advanceToward(predictedLocation);
            }

            if (rc.getLocation().distanceSquaredTo(predictedLocation) < rc.getType().visionRadiusSquared) {
                alreadyFound = true;
            }
        }
        rc.setIndicatorString("Scout - " + type);
    }

    public MapLocation goToSpot(SymmetryType type) throws GameActionException {
        List<MapLocation> friendlyArchons = robot.getFriendlyArchons();
        MapLocation homeArchon = null;
        int min = friendlyArchons.get(0).distanceSquaredTo(rc.getLocation());
        for (MapLocation locations : friendlyArchons) {
            if (locations.distanceSquaredTo(rc.getLocation()) <= min) {
                min = locations.distanceSquaredTo(rc.getLocation());
                homeArchon = locations;
            }
        }
        return type.getSymmetryLocation(homeArchon, rc);
    }

}
