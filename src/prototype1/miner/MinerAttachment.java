package prototype1.miner;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

import java.util.ArrayList;
import java.util.List;

public class MinerAttachment extends Attachment {
    private final Navigator nav;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        mine();
        if (!moveTowardCloseLead()) {
            disperse();
        }
    }

    private boolean mine() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().actionRadiusSquared)) {
            if (!rc.isActionReady()) {
                return true;
            }
            while (rc.canMineGold(loc)) {
                rc.mineGold(loc);
                rc.setIndicatorString("Mined gold");
            }
            while (rc.senseLead(loc) > 1 && rc.canMineLead(loc)) {
                rc.mineLead(loc);
                rc.setIndicatorString("Mined lead");
            }
        }
        return false;
    }

    private boolean moveTowardCloseLead() throws GameActionException {
        MapLocation bestLead = null;
        int bestScore = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            int lead = rc.senseLead(loc);
            int gold = rc.senseGold(loc);
            if (lead > 1 || gold > 0) {
                int score = rc.getLocation().distanceSquaredTo(loc);
                score -= lead * 5;
                score -= gold * 50;

                if (bestLead == null || score < bestScore) {
                    bestLead = loc;
                    bestScore = score;
                }
            }

            if (Clock.getBytecodesLeft() < 1000) {
                break;
            }
        }

        if (bestLead != null) {
            nav.advanceToward(bestLead);
            return true;
        }
        return false;
    }

    private void disperse() throws GameActionException {
        double vx = 0, vy = 0;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            double weight = 5 / Math.sqrt(info.getLocation().distanceSquaredTo(rc.getLocation()));
            if (info.team != rc.getTeam()) {
                weight *= 1.5;
                if (info.type.canAttack()) weight *= 2;
            }

            double dx = info.location.x - rc.getLocation().x;
            double dy = info.location.y - rc.getLocation().y;
            double len = Math.hypot(dx, dy);
            dx /= len;
            dy /= len;
            vx -= dx * weight;
            vy -= dy * weight;
        }

        if (rc.getMapWidth() - rc.getLocation().x < 5) {
            vx -= 40;
        } else if (rc.getLocation().x < 5) {
            vx += 40;
        }
        if (rc.getMapHeight() - rc.getLocation().y < 5) {
            vy -= 40;
        } else if (rc.getLocation().y < 5) {
            vy += 40;
        }

        MapLocation home = robot.getHomeArchon().location;
        double len = Math.hypot(rc.getLocation().x - home.x, rc.getLocation().y - home.y);
        vx += (rc.getLocation().x - home.x) / len;
        vy += (rc.getLocation().y - home.y) / len;

        double targetTheta = Math.atan2(vy, vx);

        Direction dir = Util.getDirFromAngle(targetTheta);
        int tries = 0;
        while (!rc.canMove(dir) && tries++ < 8) dir = dir.rotateLeft();

        rc.setIndicatorString("Theta = " + Math.toDegrees(targetTheta) + ", dir = " + dir);
        if (dir != null && rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
