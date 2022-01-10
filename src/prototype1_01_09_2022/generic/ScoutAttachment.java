package prototype1_01_09_2022.generic;

import battlecode.common.*;
import prototype1_01_09_2022.Attachment;
import prototype1_01_09_2022.Robot;
import prototype1_01_09_2022.Util;
import prototype1_01_09_2022.nav.Navigator;

import java.util.List;

public class ScoutAttachment extends Attachment {
    private MapLocation predictedLocation = null;
    private SymmetryType type;
    private boolean alreadyFound = false;
    private boolean alreadyFoundAll = false;
    private Navigator nav;
    private SymmetryType nextType;

    public ScoutAttachment(Robot robot, SymmetryType type) {
        super(robot);
        this.type = type;
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if(alreadyFoundAll){
            return;
        }
        if (!alreadyFound) {
            if (predictedLocation == null) {
                predictedLocation = goToSpotNew(type, robot.getHomeArchon());
            }
            if (predictedLocation != null) {
                nav.advanceToward(predictedLocation);
            }

            if (rc.getLocation().distanceSquaredTo(predictedLocation) < rc.getType().visionRadiusSquared) {
                alreadyFound = true;
            }
        } else{
            if(nextType == null){
                nextType = type.getNextSymmetryType();
            } else {
                nextType = nextType.getNextSymmetryType();
            }
            if(nextType == type){
                alreadyFoundAll = true;
                return;
            }
            alreadyFound = false;
            predictedLocation = goToSpotNew(nextType, robot.getHomeArchon());
        }
        rc.setIndicatorString("Scout - " + nextType);
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
    public MapLocation goToSpotNew(SymmetryType type, MapLocation homeArchon) throws GameActionException {
        return type.getSymmetryLocation(homeArchon, rc);
    }

}
