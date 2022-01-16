package JFPROTO.prototype1.builder;

import battlecode.common.MapLocation;

public class MapLocationAndBoolean {
    MapLocation loc;
    Boolean there;
    int robotID = 0;
    public MapLocationAndBoolean(MapLocation loc, Boolean there, int robotID){
        this.loc = loc;
        this.there = there;
        this.robotID = robotID;
    }
    public MapLocation getLoc(){
        return loc;
    }
    public boolean getIfThere(){
        return there;
    }
    public int getRobotID(){
        return  robotID;
    }
}
