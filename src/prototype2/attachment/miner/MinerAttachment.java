package prototype2.attachment.miner;

import battlecode.common.*;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.Strategy;
import prototype2.Util;
import prototype2.comms.Archon;
import prototype2.comms.CryForHelp;
import prototype2.nav.Navigator;

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
        } else if (robot.getComms().getStrategy() == Strategy.TURTLE) {
            if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) > 4) {
                nav.advanceToward(robot.getHomeArchon());
            } else {
                robot.moveRandom();
            }
        }
    }

    private boolean flee() throws GameActionException {
        if (!rc.isMovementReady()) return false;
        if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) <= 25) {
            return false;
        }

        double vx = 0;
        double vy = 0;
        int numEnemies = 0;
        int numFriendlies = 0;
        for (RobotInfo nearby : rc.senseNearbyRobots()) {
            if (nearby.type.canAttack()) {
                if (nearby.team == rc.getTeam()) {
                    ++numFriendlies;
                } else {
                    double dx = rc.getLocation().x - nearby.location.x;
                    double dy = rc.getLocation().y - nearby.location.y;
                    double len = Math.hypot(dx, dy);
                    dx /= len;
                    dy /= len;
                    vx += dx;
                    vy += dy;
                    ++numEnemies;
                }
            }
        }

        if (numFriendlies > numEnemies) return false;
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

            if ((bestLead == null || score < bestScore) && !isLeadOccupied(leadLoc)) {
                bestLead = leadLoc;
                bestScore = score;
            }
        }
        for (MapLocation goldLoc : gold) {
            bestLead = goldLoc;
            nav.advanceToward(bestLead);
            return true;
        }
        if(bestLead != null && bestLead.distanceSquaredTo(rc.getLocation()) <= rc.getType().actionRadiusSquared){
            avoidRubble();
        }
        if (bestLead != null) {
            nav.advanceToward(bestLead);
            return true;
        }
        return false;
    }
    private boolean isLeadOccupied(MapLocation loc){
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        RobotInfo closest = null;
        int bestDistanceSquared = 1000;

        for (int i = 0; i < nearbyRobots.length; i++){
            int currentRobotDistance = nearbyRobots[i].getLocation().distanceSquaredTo(loc);
            if (bestDistanceSquared >= currentRobotDistance && nearbyRobots[i].getType() == RobotType.MINER){
                bestDistanceSquared = currentRobotDistance;
                closest = nearbyRobots[i];
            }
        }
       if (rc.getLocation().distanceSquaredTo(loc) < bestDistanceSquared){
           return false;
       }
       if (rc.getLocation().distanceSquaredTo(loc) == bestDistanceSquared && rc.getID() < closest.getID()){
           return false;
       }
       return true;
    }
    public boolean closerToEnemy(){
       List<MapLocation> enemy = robot.getEnemyArchons();
       List<Archon> teamArchon = robot.getFriendlyArchons();
       if(enemy.size() == 0){
           return false;
       }
       int minDistance =  rc.getLocation().distanceSquaredTo(enemy.get(0));
       int minDistanceTeam = rc.getLocation().distanceSquaredTo(teamArchon.get(0).loc);
       for(int i = 0; i < enemy.size(); i++){
           int dist = rc.getLocation().distanceSquaredTo(enemy.get(i));
           if(dist < minDistance){
               minDistance = dist;
           }
       }

        for(int i = 0; i < teamArchon.size(); i++){
            int dist = rc.getLocation().distanceSquaredTo(teamArchon.get(i).loc);
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

    private void issueCryForHelp() throws GameActionException {
        int numEnemies = 0;
        MapLocation nearestEnemy = null;
        int numFriendlies = 0;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.type.canAttack()) {
                if (info.team == rc.getTeam()) {
                    ++numEnemies;
                    if (nearestEnemy == null || rc.getLocation().distanceSquaredTo(nearestEnemy) > rc.getLocation().distanceSquaredTo(info.location)) {
                        nearestEnemy = info.location;
                    }
                }
            }
        }
        if (nearestEnemy == null) return;
        for (RobotInfo info : rc.senseNearbyRobots(nearestEnemy.distanceSquaredTo(rc.getLocation()), rc.getTeam())) {
            if (info.type.canAttack()) {
                ++numFriendlies;
            }
        }

        if (numFriendlies > numEnemies) {
            CryForHelp cry = new CryForHelp(nearestEnemy, numEnemies,rc.getRoundNum());
            robot.getComms().addCryForHelp(cry);
        }
    }
    public void avoidRubble() throws GameActionException {
        rc.setIndicatorString("Im avoiding rubble");
        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), RobotType.MINER.actionRadiusSquared);
        int highestLead = 1;
        MapLocation bestLead = null;
        for (int i = 0; i < locs.length; i++) {
            int lead = rc.senseLead(locs[i]);
            if (lead > highestLead) {
                highestLead = lead;
                bestLead = locs[i];
            }
        }
        if(bestLead == null){
            return;
        }
        rc.setIndicatorString("" + bestLead);
        MapLocation[] locsNextToLeadLocation = rc.getAllLocationsWithinRadiusSquared(bestLead, RobotType.MINER.actionRadiusSquared);
        int lowestRubble = rc.senseRubble(bestLead);
        MapLocation locWithLowestRubble = bestLead;
        takeOutRobotLocLocations(locsNextToLeadLocation);
        for (int i = 0; i < locsNextToLeadLocation.length; i++) {
            if(locsNextToLeadLocation[i] == null) continue;
            int rub = rc.senseRubble(locsNextToLeadLocation[i]);
            if (rub < lowestRubble) {
                lowestRubble = rub;
                locWithLowestRubble = locsNextToLeadLocation[i];
            }
        }
        if(rc.canMove(rc.getLocation().directionTo(locWithLowestRubble))) rc.move(rc.getLocation().directionTo(locWithLowestRubble));
    }
    public void takeOutRobotLocLocations(MapLocation[] locs) throws GameActionException{
        for(int i = 0; i < locs.length; i++){
            if(rc.senseRobotAtLocation(locs[i]) != null ){
                locs[i] = null;
            }
        }
    }
    }
