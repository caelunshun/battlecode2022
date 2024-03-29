package OLDCODE.miner;

import battlecode.common.*;
import OLDCODE.Attachment;
import OLDCODE.Robot;
import OLDCODE.Util;
import OLDCODE.nav.Navigator;

import java.util.ArrayList;
import java.util.List;

public class MinerAttachment extends Attachment {
    private final Navigator nav;
    private int leadThisRound;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
        leadThisRound = 0;
    }

    @Override
    public void doTurn() throws GameActionException {
        leadThisRound = 0;
        if (flee()) {
            mine();
            robot.getComms().addTurnLeadAmount(leadThisRound);
            return;
        };
        mine();
        robot.getComms().addTurnLeadAmount(leadThisRound);
        if (moveTowardCloseLead() || moveTowardFarLead()) {
            robot.endTurn();
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
            while ((rc.senseLead(loc) >1 || closerToEnemy()) && rc.canMineLead(loc)) {
                rc.mineLead(loc);
                rc.setIndicatorString("Mined lead");
                leadThisRound++;
            }
        }
        return false;
    }

    private boolean moveTowardCloseLead() throws GameActionException {
        if (rc.senseLead(rc.getLocation()) > 1) return true;

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
    public boolean closerToEnemy(){
       List<MapLocation> enemy = robot.getEnemyArchons();
       List<MapLocation> teamArchon = robot.getFriendlyArchons();
       if(enemy.size() == 0){
           return false;
       }
       int minDistance =  rc.getLocation().distanceSquaredTo(enemy.get(0));
       int minDistanceTeam = rc.getLocation().distanceSquaredTo(teamArchon.get(0));
       for(int i = 0; i < enemy.size(); i++){
           int dist = rc.getLocation().distanceSquaredTo(enemy.get(i));
           if(dist < minDistance){
               minDistance = dist;
           }
       }

        for(int i = 0; i < teamArchon.size(); i++){
            int dist = rc.getLocation().distanceSquaredTo(teamArchon.get(i));
            if(dist < minDistanceTeam){
                minDistanceTeam = dist;
            }
        }
        if(minDistance < minDistanceTeam){
            return true;
        }
        return false;
    }

    private MapLocation farTarget;

    private boolean moveTowardFarLead() throws GameActionException {
        if (farTarget != null && rc.getLocation().distanceSquaredTo(farTarget) <= rc.getType().visionRadiusSquared) {
            farTarget = null;
        }

        if (farTarget == null) {
            MapLocation[] locs = robot.getComms().getLeadLocations();
            for (int i = 0; i < locs.length; i++) {
                MapLocation loc = locs[i];
                if (loc != null && Math.sqrt(rc.getLocation().distanceSquaredTo(loc)) <= 20) {
                    farTarget = loc;
                    robot.getComms().clearLeadLocation(i);
                    break;
                }
            }
        }

        if (farTarget != null) {
            nav.advanceToward(farTarget);
            rc.setIndicatorLine(rc.getLocation(), farTarget, 255, 255, 255);
            rc.setIndicatorString("Following far target");
            return true;
        }

        return false;
    }
}
