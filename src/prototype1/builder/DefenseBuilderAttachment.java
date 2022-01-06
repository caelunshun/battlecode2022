package prototype1.builder;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

import java.util.ArrayList;
import java.util.List;

public class DefenseBuilderAttachment extends Attachment {
    private MapLocation loyalToArchon;
    private List<MapLocation> watchtowerPositions;
    private Navigator nav;

    public DefenseBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        chooseLoyalToArchon();
        if (!healWatchtowers()) {
            buildWatchtowers();
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

    private void buildWatchtowers() throws GameActionException {
        for (MapLocation candidate : watchtowerPositions) {
            if (rc.getLocation().isAdjacentTo(candidate)
                && rc.canBuildRobot(RobotType.WATCHTOWER, rc.getLocation().directionTo(candidate))) {
                rc.buildRobot(RobotType.WATCHTOWER, rc.getLocation().directionTo(candidate));
            }
        }

        while (!watchtowerPositions.isEmpty()) {
            MapLocation target = watchtowerPositions.get(0);
            if (rc.canSenseRobotAtLocation(target) && rc.senseRobotAtLocation(target) != null) {
                watchtowerPositions.remove(0);
            }

            if (!rc.getLocation().isAdjacentTo(target)) {
                nav.advanceToward(target);
                break;
            }
        }
    }

    private void chooseLoyalToArchon() {
        MapLocation prev = loyalToArchon;
        loyalToArchon = Util.getClosest(rc.getLocation(), robot.getFriendlyArchons());
        if (!loyalToArchon.equals(prev)) {
            chooseWatchtowerPositions();
        }
    }

    private void chooseWatchtowerPositions() {
        watchtowerPositions = new ArrayList<>(12);
        for (int dx = -1; dx <= 1; dx++) {
            if (dx == 0) continue;
            for (int dy = -1; dy <= 1; dy++) {
                if (dy == 0) continue;

                MapLocation initial = loyalToArchon.translate(dx * 3, dy * 3);
                while (!initial.equals(loyalToArchon)) {
                    watchtowerPositions.add(initial);
                    initial = initial.add(initial.directionTo(loyalToArchon));
                }
            }
        }
    }
}
