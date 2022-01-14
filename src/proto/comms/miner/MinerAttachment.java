package proto.comms.miner;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import proto.comms.Attachment;
import proto.comms.Robot;
import proto.comms.Util;
import proto.comms.nav.Navigator;

import java.util.List;

public class MinerAttachment extends Attachment {
    private final Navigator nav;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (flee()) {
            mine();
            return;
        };
        mine();
        if (moveTowardCloseLead()) {
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
}
