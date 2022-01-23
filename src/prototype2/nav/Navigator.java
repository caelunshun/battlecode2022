package prototype2.nav;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype2.Robot;
import prototype2.Util;

public final class Navigator {
    private Robot robot;

    // Ring buffer of recently visited locations to avoid getting stuck
    private MapLocation[] visited = new MapLocation[3];
    private int visitedCursor = 0;

    private MapLocation target;

    private int rubbleThreshold = 50;

    public Navigator(Robot robot) {
        this.robot = robot;
    }

    public void advanceToward(MapLocation location) throws GameActionException {
        if (!robot.getRc().isMovementReady()) return;

        if (!location.equals(target)) {
            reset(location);
        }

        int bestScore = Integer.MAX_VALUE;
        Direction bestDir = null;
        outer: for (Direction dir : Util.DIRECTIONS) {
            MapLocation target = robot.getRc().getLocation().add(dir);
            if (target.x < 0 || target.y < 0
                    || target.x >= robot.getRc().getMapWidth()
                    || target.y >= robot.getRc().getMapHeight()) {
                continue;
            }

            for (MapLocation loc : visited) {
                if (target.equals(loc)) continue outer;
            }

            int dist = target.distanceSquaredTo(location);
            int rubble = robot.getRc().senseRubble(target);
            if (rubble > rubbleThreshold) continue;
            int rubbleFactor = (int) (rubble * 0.15);
            int score = dist + rubbleFactor;
            if ((score < bestScore || bestDir == null) && robot.getRc().canMove(dir)) {
                bestDir = dir;
                bestScore = score;
            }

            if (robot.getRc().canMove(dir) && target.equals(location)) {
                bestDir = dir;
                break;
            }
        }

        if (bestDir != null) {
            robot.getRc().move(bestDir);

            MapLocation loc = robot.getRc().getLocation();
            visited[visitedCursor++] = loc;
            if (visitedCursor == visited.length) {
                visitedCursor = 0;
            }
        } else {
            rubbleThreshold += 10;
        }
    }

    private void reset(MapLocation loc) {
        target = loc;
        for (int i = 0; i < visited.length; i++) {
            visited[i] = null;
        }
        visitedCursor = 0;
        rubbleThreshold = 50;
    }
}
