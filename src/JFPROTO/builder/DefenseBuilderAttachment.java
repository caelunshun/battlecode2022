package JFPROTO.builder;

import JFPROTO.Attachment;
import JFPROTO.Robot;
import JFPROTO.Util;
import JFPROTO.archon.ArchonAttachment;
import JFPROTO.nav.Navigator;
import battlecode.common.*;

public class DefenseBuilderAttachment extends Attachment {
    private Navigator nav;

    public DefenseBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if(rc.getRoundNum() == ArchonAttachment.tiebreakerRound || rc.getTeamLeadAmount(rc.getTeam()) > 4000){
            robot.addAttachment(new LabBuilderAttachment(robot));
        }
        if(rc.getRoundNum() >= ArchonAttachment.tiebreakerRound){
            return;
        }
        if (!healWatchtowers()) {
            if (!buildWatchtowers()) {
                nav.advanceToward(Util.getCenterLocation(rc));
            }
        } else {
            rc.setIndicatorString("Healed");
        }
    }

    private boolean healWatchtowers() throws GameActionException {
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam())) {
            if (info.type == RobotType.WATCHTOWER && info.health != info.type.health) {
                if (rc.canRepair(info.location)) {
                    rc.repair(info.location);
                    rc.setIndicatorString("Healed at " + info.location);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean buildWatchtowers() throws GameActionException {
        if (rc.getTeamLeadAmount(rc.getTeam()) < 400) {
            return false;
        }

        for (Direction dir : Util.DIRECTIONS) {
            MapLocation loc = rc.getLocation().add(dir);
            if (isLegalWatchtowerLoc(loc)) {
                if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    rc.buildRobot(RobotType.WATCHTOWER, dir);
                    return true;
                }
            }
        }

        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (isLegalWatchtowerLoc(loc) && !rc.isLocationOccupied(loc)) {
                nav.advanceToward(loc);
                Direction dir = rc.getLocation().directionTo(loc);
                if (rc.getLocation().isAdjacentTo(loc) && rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                    rc.buildRobot(RobotType.WATCHTOWER, dir);
                }
                return true;
            }
        }

        return false;
    }

    private boolean isLegalWatchtowerLoc(MapLocation loc) {
        if (loc.distanceSquaredTo(robot.getHomeArchon()) <= 5) {
            return false;
        }

        return loc.x % 3 == 0 && loc.y % 3 == 0;
    }
}
