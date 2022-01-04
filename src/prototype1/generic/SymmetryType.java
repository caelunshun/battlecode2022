package prototype1.generic;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public enum SymmetryType {
    HORIZONTAL,VERTICAL,ROTATIONAL;
    public MapLocation getSymmetryLocation(MapLocation loc, RobotController rc){
        if(this == SymmetryType.HORIZONTAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            yCoordinate = rc.getMapHeight() - yCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(this == SymmetryType.VERTICAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            xCoordinate = rc.getMapWidth() - xCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(this == SymmetryType.ROTATIONAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            xCoordinate = rc.getMapWidth() - xCoordinate;
            yCoordinate = rc.getMapHeight() - yCoordinate;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        return null;
    }
}

