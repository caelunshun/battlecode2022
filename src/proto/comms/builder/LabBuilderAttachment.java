package proto.comms.builder;

import battlecode.common.*;
import proto.comms.Attachment;
import proto.comms.Robot;
import proto.comms.Util;
import proto.comms.nav.Navigator;

import java.util.List;

public class LabBuilderAttachment extends Attachment {
    private final Navigator nav;
    private MapLocation target;
    private Direction targetWhenThere;
    private MapLocation prototypeToWorkOn;
    private int prototypeToWorkOnID;
    private boolean needToBuildWatchtower = false;

    public LabBuilderAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);

    }

    @Override
    public void doTurn() throws GameActionException {

        if (wantsToBuildLab()) {
            rc.setIndicatorString("I WANT TO BUILD A LAB");
            //find a better way to choose location of this.
            if(target==null){
                target = chooseLocation();
            }
            if (target == null) {
                // need to move closer to possible targets
                robot.moveRandom();
            }
            if(target!=null && !rc.getLocation().isAdjacentTo(target)) {
                nav.advanceToward(target);
            }
            if(rc.getLocation().equals(target)){
                robot.moveRandom();
            }
        }
        //edgecase: already adjacent
        if(target != null) {
            if (rc.getLocation().isAdjacentTo(target)) {
                targetWhenThere = rc.getLocation().directionTo(target);
                target = null;
            }
        }
        if(targetWhenThere != null){
            if(rc.canBuildRobot(RobotType.LABORATORY, targetWhenThere)){
                rc.buildRobot(RobotType.LABORATORY, targetWhenThere);
                targetWhenThere = null;
                needToBuildWatchtower = true;
            }
        }
        if(needToBuildWatchtower){
            for(Direction dir : Util.DIRECTIONS){
                if(rc.canBuildRobot(RobotType.WATCHTOWER, dir)){
                    rc.buildRobot(RobotType.WATCHTOWER, dir);
                    needToBuildWatchtower = false;
                }
            }
        }
        if(!healRobots() ){
            MapLocationAndBoolean info = findPrototypes();
            if(info.getIfThere()) {
                prototypeToWorkOn = info.getLoc();
                prototypeToWorkOnID = info.getRobotID();
            }
        }
        if(rc.canSenseRobot(prototypeToWorkOnID)){
            if(rc.senseRobot(prototypeToWorkOnID).getMode() != RobotMode.PROTOTYPE ){
                prototypeToWorkOnID = 0;
                prototypeToWorkOn = null;
            }
        } else{
            prototypeToWorkOnID = 0;
            prototypeToWorkOn = null;
        }
        if(prototypeToWorkOn != null) {
            nav.advanceToward(prototypeToWorkOn);
        }
        rc.setIndicatorString("LAB BUILDER");
    }


    public boolean wantsToBuildLab() {
        if (rc.getTeamLeadAmount(rc.getTeam()) > 1200) {
            return true;
        }
        return false;
    }

    public MapLocation chooseLocation() throws GameActionException {
        List<MapLocation> friendlyArchons = robot.getFriendlyArchons();
        outer: for (MapLocation checks : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 400)) {
            for (MapLocation archons : friendlyArchons) {
                if (archons.distanceSquaredTo(checks) < 100) {
                    continue outer;
                }
            }
            return checks;
        }
        return null;
    }
    public boolean healRobots() throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        for(int i = robots.length - 1; i >= 0; i--){
            if(robots[i].getMode() == RobotMode.PROTOTYPE && (robots[i].getType() == RobotType.LABORATORY || robots[i].getType() == RobotType.WATCHTOWER)){
                if(rc.canRepair(robots[i].getLocation())) {
                    rc.repair(robots[i].getLocation());
                    return true;
                }
            }
        }
        for(int i = robots.length - 1; i >= 0; i--){
            if((robots[i].getType() == RobotType.LABORATORY || robots[i].getType() == RobotType.WATCHTOWER)){
                if(rc.canRepair(robots[i].getLocation())) {
                    rc.repair(robots[i].getLocation());
                    return false;
                }
            }
        }
        return false;
    }
    public MapLocationAndBoolean findPrototypes(){
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        for(RobotInfo robot : robots){
            if(robot.getMode() == RobotMode.PROTOTYPE){
                return new MapLocationAndBoolean(robot.getLocation(), true, robot.getID());
            }
        }
        return new MapLocationAndBoolean(null, false, 0);
    }
}