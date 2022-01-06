package prototype1_pre_defense.nav;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1_pre_defense.Robot;
import prototype1_pre_defense.Util;

public final class Navigator {
    private Robot robot;

    public Navigator(Robot robot) {
        this.robot = robot;
    }

    public void advanceToward(MapLocation location) throws GameActionException {
        int bestScore = Integer.MAX_VALUE;
        Direction bestDir = null;
        for (Direction dir : Util.DIRECTIONS) {
            MapLocation target = robot.getRc().getLocation().add(dir);
            if (target.x < 0 || target.y < 0
                    || target.x >= robot.getRc().getMapWidth()
                    || target.y >= robot.getRc().getMapHeight()) {
                continue;
            }
            int dist = target.distanceSquaredTo(location);
            int rubbleFactor = (int) (robot.getRc().senseRubble(target) * 0.2);
            int score = dist + rubbleFactor;
            if ((score < bestScore || bestDir == null) && robot.getRc().canMove(dir)) {
                bestDir = dir;
                bestScore = score;
            }
        }

        if (bestDir != null) {
            robot.getRc().move(bestDir);
        }
    }
}
