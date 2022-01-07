package prototype1_01_06_2022.generic;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public enum SymmetryType {
    HORIZONTAL,VERTICAL,ROTATIONAL;
    public MapLocation getSymmetryLocation(MapLocation loc, RobotController rc){
        if(this == SymmetryType.HORIZONTAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            yCoordinate = rc.getMapHeight() - yCoordinate - 1;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(this == SymmetryType.VERTICAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            xCoordinate = rc.getMapWidth() - xCoordinate - 1;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        if(this == SymmetryType.ROTATIONAL){
            int xCoordinate = loc.x;
            int yCoordinate = loc.y;
            xCoordinate = rc.getMapWidth() - xCoordinate - 1;
            yCoordinate = rc.getMapHeight() - yCoordinate - 1;
            MapLocation enemyToCheck = new MapLocation(xCoordinate, yCoordinate);
            return enemyToCheck;
        }
        return null;
    }
    public SymmetryType getNextSymmetryType(){
        if(this == HORIZONTAL){
            return ROTATIONAL;
        }
        if(this == ROTATIONAL){
            return VERTICAL;
        }
        return HORIZONTAL;
    }
}

