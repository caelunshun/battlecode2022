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
    public ScoutAttachment (Robot robot, SymmetryType type) {
        super(robot);
        this.type = type;
        this.nav = new Navigator(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        if(!alreadyFound) {
            if (predictedLocation == null) {
                predictedLocation = goToSpot(type);
            } else {
                nav.advanceToward(predictedLocation);
            }

            if (rc.getLocation().distanceSquaredTo(predictedLocation) < rc.getType().visionRadiusSquared) {
                alreadyFound = true;
            }
        }


    }
public MapLocation goToSpot(SymmetryType type) throws GameActionException{
    List<MapLocation> friendlyArchons = robot.getFriendlyArchons();
    MapLocation homeArchon = null;
    int min = friendlyArchons.get(0).distanceSquaredTo(rc.getLocation());
    for(MapLocation locations : friendlyArchons){
        if(locations.distanceSquaredTo(rc.getLocation()) <= min){
            min = locations.distanceSquaredTo(rc.getLocation());
            homeArchon = locations;
        }
    }
    return getSymmetryLocation(homeArchon, type);
}
public MapLocation getSymmetryLocation(MapLocation homeArchon, SymmetryType typeToCheck){
        if(typeToCheck == SymmetryType.HORIZONTAL){
            int xCoordinate = homeArchon.x;
            int yCoordinate = homeArchon.y;
            yCoordinate = rc.getMapHeight() - yCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(typeToCheck == SymmetryType.VERTICAL){
            int xCoordinate = homeArchon.x;
            int yCoordinate = homeArchon.y;
            xCoordinate = rc.getMapWidth() - xCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(typeToCheck == SymmetryType.ROTATIONAL){
            int xCoordinate = homeArchon.x;
            int yCoordinate = homeArchon.y;
            xCoordinate = rc.getMapWidth() - xCoordinate;
            yCoordinate = rc.getMapHeight() - yCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
      }
    return null;
}
}
