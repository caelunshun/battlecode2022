package JFPROTO.prototype1.builder;

import JFPROTO.prototype1.Attachment;
import JFPROTO.prototype1.Robot;
import JFPROTO.prototype1.Util;
import JFPROTO.prototype1.archon.ArchonAttachment;
import JFPROTO.prototype1.nav.Navigator;
import battlecode.common.*;

/**
 * Attachment for rushing builders - i.e., builders
 * that spam watchtowers around the enemy Archons.
 */
public class RushBuilderAttachment extends Attachment {
    private MapLocation targetArchon;
    private final Navigator nav;

    public RushBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        boolean healedTowers = healWatchtowers();
        if(rc.getRoundNum() == ArchonAttachment.tiebreakerRound){
            robot.addAttachment(new LabBuilderAttachment(robot));
        }
        if(rc.getRoundNum() >= ArchonAttachment.tiebreakerRound){
            return;
        }
        if (!robot.getEnemyArchons().contains(targetArchon)) {
            targetArchon = null;
        }
        if (targetArchon == null) {
            findTargetArchon();
        }

        if (targetArchon != null) {
            rush();
        }

        if (targetArchon == null && !healedTowers) {
            robot.moveRandom();
        }
    }

    private void findTargetArchon() throws GameActionException {
        MapLocation closest = null;
        for (MapLocation loc : robot.getEnemyArchons()) {
            if (closest == null || loc.distanceSquaredTo(rc.getLocation()) < closest.distanceSquaredTo(rc.getLocation())) {
                closest = loc;
            }
        }

        targetArchon = closest;
    }

    private void rush() throws GameActionException {
        buildWatchtower();
        advanceTowardArchon();
    }

    private void buildWatchtower() throws GameActionException {
        MapLocation target = findWatchtowerPlacementTarget();
        if (target == null) return;
        if (rc.getLocation().isAdjacentTo(target)) {
            Direction dir = rc.getLocation().directionTo(target);
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                rc.buildRobot(RobotType.WATCHTOWER, dir);
            }
        } else {
            nav.advanceToward(target);
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

    private MapLocation findWatchtowerPlacementTarget() throws GameActionException {
        for (Direction dir : Util.DIRECTIONS) {
            MapLocation loc = targetArchon.add(dir);
            if (rc.canSenseLocation(loc)) {
                if (rc.senseRobotAtLocation(loc) == null) {
                    return loc;
                }
            }
        }
        return null;
    }

    private void advanceTowardArchon() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(targetArchon) <= 8) {
            return;
        }

        nav.advanceToward(targetArchon);
    }
}
