package prototype2.attachment.builder;

import prototype2.Util;
import battlecode.common.*;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.build.LeadBuild;
import prototype2.nav.Navigator;

import java.util.Arrays;

public class BuilderAttachment extends Attachment {
    private final Navigator nav;
    private MapLocation watchtowerTarget;

    public BuilderAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!repairWatchtowers()) {
            if (!buildLaboratory() && !buildWatchtowers()) {
                repairBuildings();
                robot.moveRandom();
            }
        }
    }

    private boolean buildLaboratory() throws GameActionException {
        if (robot.getComms().getLeadBuild() != LeadBuild.LABORATORY) return false;

        MapLocation[] corners = new MapLocation[]{
            new MapLocation(0, 0),
            new MapLocation(rc.getMapWidth() - 1, 0),
                new MapLocation(rc.getMapWidth() - 1, rc.getMapHeight() - 1),
                new MapLocation(0, rc.getMapHeight() - 1),
        };
        MapLocation target = Util.getClosest(rc.getLocation(), Arrays.asList(corners));
        while (rc.canSenseLocation(target) && rc.canSenseRobotAtLocation(target)) {
            target = target.add(target.directionTo(Util.getCenterLocation(rc)));
        }

        if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) >= 64) {
            for (Direction dir : Util.DIRECTIONS) {
                MapLocation loc = rc.getLocation().add(dir);
                if (!rc.canSenseLocation(loc)) continue;
                if (rc.senseRubble(loc) <= 20) {
                    if (rc.canBuildRobot(RobotType.LABORATORY, dir)) {
                        rc.buildRobot(RobotType.LABORATORY, dir);
                        robot.getComms().setLeadBuild(null);
                        return true;
                    }
                }
            }
        }

        if (rc.getLocation().equals(target)) {
            robot.moveRandom();
        } else if (rc.getLocation().isAdjacentTo(target)) {
            Direction dir = rc.getLocation().directionTo(target);
            if (rc.canBuildRobot(RobotType.LABORATORY, dir)) {
                rc.buildRobot(RobotType.LABORATORY, dir);
                robot.getComms().setLeadBuild(null);
            }
        } else {
            nav.advanceToward(target);
        }

        return true;
    }

    private boolean buildWatchtowers() throws GameActionException {
        if (robot.getComms().getLeadBuild() != LeadBuild.WATCHTOWER) return false;

        if (!watchtowerTargetIsValid()) {
            if(getWatchtowerBuildLocation() == null){
                watchtowerTarget = getWatchtowerTarget();
            } else {
                watchtowerTarget = getWatchtowerBuildLocation();
            }
        }
        if (watchtowerTarget == null) {
            nav.advanceToward(Util.getCenterLocation(rc));
            return true;
        }

        if (rc.getLocation().isAdjacentTo(watchtowerTarget)) {
            Direction dir = rc.getLocation().directionTo(watchtowerTarget);
            rc.setIndicatorString("building in " + dir);
            if (rc.canBuildRobot(RobotType.WATCHTOWER, dir)) {
                rc.buildRobot(RobotType.WATCHTOWER, dir);
                robot.getComms().setLeadBuild(null); // completed build
            }
        } else if (rc.getLocation().equals(watchtowerTarget)) {
            // get off the target
            robot.moveRandom();
        } else {
            nav.advanceToward(watchtowerTarget);
        }

        return true;
    }

    private boolean watchtowerTargetIsValid() throws GameActionException {
        return watchtowerTarget != null && rc.canSenseLocation(watchtowerTarget)
                && rc.senseRobotAtLocation(watchtowerTarget) == null;
    }

    private MapLocation getWatchtowerTarget() throws GameActionException {
        MapLocation best = null;
        double bestScore = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (!isOnLattice(loc)) continue;
            if (rc.senseRobotAtLocation(loc) != null) continue;

            double score = 0;
            score += Math.sqrt(loc.distanceSquaredTo(robot.getHomeArchon()));
            score += 0.3 * Math.sqrt(loc.distanceSquaredTo(Util.getCenterLocation(rc)));
            score += 0.3 * rc.senseRubble(loc);
            if (loc.x < 4  || loc.y < 4 || loc.x > rc.getMapWidth() - 4 || loc.y > rc.getMapHeight() - 4) {
                score += 20;
            }

            if (best == null || score < bestScore) {
                best = loc;
                bestScore = score;
            }

            if (Clock.getBytecodesLeft() < 2000) {
                break;
            }
        }
        return best;
    }
    private MapLocation getWatchtowerBuildLocation() throws GameActionException{
        return robot.getComms().getWatchtowerBuildLocation();
    }

    private boolean isOnLattice(MapLocation loc) throws GameActionException {
        return loc.x % 2 == loc.y % 2;
    }

    private boolean repairWatchtowers() throws GameActionException {
        RobotInfo tower = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (!(tower == null || rc.getLocation().distanceSquaredTo(tower.location) > rc.getLocation().distanceSquaredTo(info.location))) {
                continue;
            }
            if (info.mode == RobotMode.PROTOTYPE) {
                tower = info;
            }
        }

        if (tower == null) {
            return false;
        } else {
            if (rc.canRepair(tower.location)) {
                rc.repair(tower.location);
            } else if (rc.getLocation().distanceSquaredTo(tower.location) > rc.getType().actionRadiusSquared) {
                nav.advanceToward(tower.location);
            }
            return true;
        }
    }

    private void repairBuildings() throws GameActionException {
        RobotInfo tower = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (!(tower == null || rc.getLocation().distanceSquaredTo(tower.location) > rc.getLocation().distanceSquaredTo(info.location))) {
                continue;
            }
            if (info.health < info.type.getMaxHealth(info.level)) {
                tower = info;
                break;
            }
        }

        if (tower != null) {
            if (rc.canRepair(tower.location)) {
                rc.repair(tower.location);
            } else if (rc.getLocation().distanceSquaredTo(tower.location) > rc.getType().actionRadiusSquared) {
                nav.advanceToward(tower.location);
            }
        }
    }
}
