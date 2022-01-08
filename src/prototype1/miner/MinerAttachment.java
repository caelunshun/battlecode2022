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
    private double disperseTheta;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
        disperseTheta = getRandomAngle();
    }

    @Override
    public void doTurn() throws GameActionException {
        if (flee()) {
            mine();
            return;
        };
        mine();
        if (!moveTowardCloseLead()) {
            disperse();
        }
    }

    private boolean flee() throws GameActionException {
        if (!rc.isMovementReady()) return false;

        double vx = 0;
        double vy = 0;
        for (RobotInfo nearby : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (nearby.type.canAttack()) {
                double dx = rc.getLocation().x - nearby.location.x;
                double dy = rc.getLocation().y - nearby.location.y;
                double len = Math.hypot(dx, dy);
                dx /= len;
                dy /= len;
                vx += dx;
                vy += dy;
            }
        }

        if (vx == 0 && vy == 0) return false;

        Direction dir = Util.getDirFromAngle(Math.atan2(vy, vx));
        int tries = 0;
        while (!rc.canMove(dir) && tries++ < 8) dir = dir.rotateLeft();
        if (rc.canMove(dir)) {
            rc.move(dir);
            rc.setIndicatorString("Fled");
        } else {
            rc.setIndicatorString("Flee Failed - " + dir);
        }

        return true;
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

        MapLocation[] lead = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared);
        MapLocation[] gold = rc.senseNearbyLocationsWithGold(rc.getType().visionRadiusSquared);

        for (MapLocation leadLoc : lead) {
            int leadCount = rc.senseLead(leadLoc);
            if (leadCount <= 1) continue;

            if (rc.getRoundNum() < 50 && leadCount < 10 && leadLoc.distanceSquaredTo(robot.getHomeArchon()) <= 20) {
                continue;
            }

            int score = rc.getLocation().distanceSquaredTo(leadLoc) - leadCount;

            if (bestLead == null || score < bestScore) {
                bestLead = leadLoc;
                bestScore = score;
            }
        }
        for (MapLocation goldLoc : gold) {
            bestLead = goldLoc;
        }

        if (bestLead != null) {
            nav.advanceToward(bestLead);
            return true;
        }
        return false;
    }

    private void disperse() throws GameActionException {
        rc.setIndicatorString("Dispersing");
        if (!rc.isMovementReady()) return;
        int tries = 0;
        while (!checkCurrentDisperseTheta() && tries++ < 5) {
            resetDisperseTheta();
        }

        Direction dir = Util.getDirFromAngle(disperseTheta);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        rc.setIndicatorString("Dispersing: Theta = " + Math.toDegrees(disperseTheta));
    }

    private boolean checkCurrentDisperseTheta() {
        Direction dir = Util.getDirFromAngle(disperseTheta);
        if (!rc.canMove(dir)) {
            return false;
        }

        // Ray-trace along the current disperse theta.
        // If we will hit the edge of the map, then stop.
        double dy = Math.sin(disperseTheta);
        double dx = Math.cos(disperseTheta);
        MapLocation edgeLoc = rc.getLocation().translate((int) (dx * 5), (int) (dy * 5));
        if (!Util.isOnTheMap(edgeLoc, rc)) {
            return false;
        }

        return true;
    }

    private void resetDisperseTheta() {
        disperseTheta = getRandomAngle();
    }

    private double getRandomAngle() {
        MapLocation diff = rc.getLocation().translate(-robot.getHomeArchon().x, -robot.getHomeArchon().y);
        double angleToArchon = Math.atan2(diff.y, diff.x);
        return angleToArchon + (robot.getRng().nextDouble() * 2 - 1) * Math.PI * 2 / 3;
    }
}
