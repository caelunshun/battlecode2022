package prototype1.builder;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;
import java.util.*;

public class BuilderAttachment extends Attachment {
    private final Navigator nav;
    private MapLocation target;
    private Direction targetWhenThere;
    private boolean needToBuildWatchtower = false;

    public BuilderAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (wantsToBuildLab()) {
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
    }

    public boolean wantsToBuildLab() {
        if (rc.getTeamLeadAmount(rc.getTeam()) > 2400) {
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
}