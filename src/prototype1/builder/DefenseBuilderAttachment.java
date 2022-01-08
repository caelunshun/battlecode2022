package prototype1.builder;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.archon.ArchonAttachment;
import prototype1.nav.Navigator;

import java.util.*;

public class DefenseBuilderAttachment extends Attachment {
    private MapLocation loyalToArchon;
    private List<MapLocation> watchtowerPositions;
    private Navigator nav;

    public DefenseBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    int age = 0;

    @Override
    public void doTurn() throws GameActionException {
        if(rc.getRoundNum() == ArchonAttachment.tiebreakerRound){
            robot.addAttachment(new LabBuilderAttachment(robot));
        }
        if(rc.getRoundNum() >= ArchonAttachment.tiebreakerRound){
            return;
        }
        chooseLoyalToArchon();
        if (!healWatchtowers()) {
            buildWatchtowers();
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
        for (MapLocation candidate : watchtowerPositions) {
            if (rc.getLocation().isAdjacentTo(candidate)
                && rc.canBuildRobot(RobotType.WATCHTOWER, rc.getLocation().directionTo(candidate))
                && new Random(robot.getComms().getBuildIndex()).nextFloat() < 0.3) {
                rc.buildRobot(RobotType.WATCHTOWER, rc.getLocation().directionTo(candidate));
                rc.setIndicatorString("Built");
                return true;
            }
        }

        while (!watchtowerPositions.isEmpty()) {
            MapLocation target = watchtowerPositions.get(0);
            if (rc.canSenseRobotAtLocation(target) && rc.senseRobotAtLocation(target) != null) {
                watchtowerPositions.remove(0);
                continue;
            }

            if (!rc.getLocation().isAdjacentTo(target)) {
                nav.advanceToward(target);
                rc.setIndicatorString("Advancing");
            }
            return true;
        }

        return false;
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
        for (int ring = 0; ring < 4; ring++) {
            int[] deltas = {-1, 1};
            for (int dx : deltas) {
                for (int dy : deltas) {
                    MapLocation loc = loyalToArchon.translate(dx * (ring + 1), dy * (ring + 1));
                    if (Util.isOnTheMap(loc, rc)) {
                        watchtowerPositions.add(loc);
                    }
                }
            }
        }
    }
}
