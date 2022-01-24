package prototype2.attachment.archon;

import battlecode.common.*;
import prototype1.Util;
import prototype2.*;
import prototype2.build.GoldBuild;
import prototype2.build.LeadBuild;
import prototype2.comms.Archon;
import prototype2.nav.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base functionality attached to every archon.
 * <p>
 * Most archon logic happens in the LeadArchonAttachment—for example,
 * building units.
 */
public class BaseArchonAttachment extends Attachment {
    private Navigator nav;
    private int archonIndex;

    private boolean inPregame = true;
    private int minersBuilt;

    public BaseArchonAttachment(Robot robot) throws GameActionException {
        super(robot);
        this.nav = new Navigator(robot);
        registerInComms();
    }

    private void registerInComms() throws GameActionException {
        int numLead = countLeadLocations();
        archonIndex = robot.getComms().addFriendlyArchon(rc.getLocation(), numLead);
    }

    private int countLeadLocations() throws GameActionException {
        return rc.senseNearbyLocationsWithLead().length;
    }

    @Override
    public void doTurn() throws GameActionException {
        updateComms();
        build();
        healRobots();
        updateDispersionAngle();
        if (inPregame) {
            doPregame();
        }
        if (!turtled && robot.getComms().getStrategy() == Strategy.TURTLE) {
            doTurtle();
        }

        if (inPregame) {
            robot.getComms().clearRobotCounts();
        }
    }

    private int lastBuiltIndex = -100;
    private boolean isLead = false;

    private void build() throws GameActionException {


        int currentBuildIndex = robot.getComms().getBuildIndex();
        if (currentBuildIndex - lastBuiltIndex < rc.getArchonCount() - 1) {
            // No need to balance builds if we have tons of lead.
            if (rc.getTeamLeadAmount(rc.getTeam()) < 1000 && rc.getRoundNum() > 2
                    && !(robot.getComms().getStrategy() == Strategy.TURTLE && isLead)) {
              //  rc.setIndicatorString("Not Building");
                return;
            }
        }




        if (robot.getComms().getStrategy() == Strategy.TURTLE && !isLead) {
            rc.setIndicatorString("Not Building");
            return;
        }

        GoldBuild goldBuild = robot.getComms().getGoldBuild();
        if (goldBuild != null) {
            RobotType goldType = goldBuild.getType();
            if (goldType != null && rc.getType().canBuild(goldType)) {
                if (tryBuild(goldType)) {
                    robot.getComms().setGoldBuild(null);
                    ++currentBuildIndex;
                    robot.getComms().setBuildIndex(currentBuildIndex);
                    lastBuiltIndex = currentBuildIndex;
                }
            }
        }

        LeadBuild leadBuild = robot.getComms().getLeadBuild();
        if (leadBuild != null) {
            RobotType leadType = leadBuild.getType();
            if (leadType != null && rc.getType().canBuild(leadType)) {
                if (tryBuild(leadType)) {
                    robot.getComms().setLeadBuild(null);
                    ++currentBuildIndex;
                    robot.getComms().setBuildIndex(currentBuildIndex);
                    lastBuiltIndex = currentBuildIndex;
                }
            }
        }
    }

    private void doPregame() throws GameActionException {
        if (minersBuilt < BotConstants.getPregameMinersPerArchon(rc)) {
            if (tryBuild(RobotType.MINER)) {
                ++minersBuilt;
            }
        } else {
            inPregame = false;
            MapLocation targetArchon = getUnionArchon();
            if (targetArchon.equals(rc.getLocation())) {
                promoteToLeader();
            }
        }
    }

    private boolean turtled = false;

    private void doTurtle() throws GameActionException {
        MapLocation targetArchon = getUnionArchon();
        if (targetArchon.equals(rc.getLocation())) {
            turtled = true;
        } else if (rc.getLocation().distanceSquaredTo(targetArchon) <= 15) {
            if (rc.getMode() == RobotMode.TURRET || tryTransform()) {
                turtled = true;
            }
        } else {
            if (rc.getMode() == RobotMode.TURRET) {
                tryTransform();
            }
            nav.advanceToward(targetArchon);
        }
    }

    private boolean tryTransform() throws GameActionException {
        if (rc.canTransform()) {
            rc.transform();
            return true;
        }
        return false;
    }

    private void promoteToLeader() {
        robot.addAttachment(0, new LeadArchonAttachment(robot));
        isLead = true;
    }

    private MapLocation getUnionArchon() {
        Archon best = null;
        for (Archon archon : robot.getFriendlyArchons()) {
            if (best == null || archon.numLeadLocations > best.numLeadLocations) {
                best = archon;
            }
        }
        return best.loc;
    }

    private void updateComms() throws GameActionException {
        Archon archon = robot.getFriendlyArchons().get(archonIndex);
        archon.loc = rc.getLocation();
        archon.isLead = isLead;
        robot.getComms().updateFriendlyArchon(archonIndex, archon);
    }

    private void healRobots() throws GameActionException {
        RobotInfo lowest = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam())) {
            if (info.health == info.getType().getMaxHealth(info.level)) continue;
            if (lowest == null || info.health < lowest.health) {
                lowest = info;
            }
        }

        if (lowest != null && rc.canRepair(lowest.location)) {
            rc.repair(lowest.location);
        }
    }

    public boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = getAvailableBuildDirection(type);
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            //rc.setIndicatorString("Built a " + type);
            return true;
        }
        return false;
    }


    private Direction getAvailableBuildDirection(RobotType type) throws GameActionException {
        if (type == RobotType.MINER) {
            int bestLeadScore = -1;
            Direction bestDir = Direction.CENTER;
            Direction[] list = Arrays.copyOf(Util.DIRECTIONS, Util.DIRECTIONS.length);
            Util.shuffle(list);
            for (int i = 0; i < list.length; i++) {
                if (rc.senseRobotAtLocation(rc.getLocation().add(list[i])) == null) {
                    int leadScore = getLeadScore(rc.getLocation().add(list[i]));
                    if (leadScore > bestLeadScore || (leadScore == bestLeadScore && rc.senseRubble(rc.getLocation().add(list[i])) < rc.senseRubble(rc.getLocation().add(bestDir)))) {
                        bestLeadScore = leadScore;
                        bestDir = list[i];
                    }
                }
            }
            return bestDir;
        } else {
            Direction[] list = Arrays.copyOf(Util.DIRECTIONS, Util.DIRECTIONS.length);
            Util.shuffle(list);
            ArrayList<Direction> lessThanForty = new ArrayList<Direction>();
            //lower score is better
            Direction bestDir = Direction.CENTER;
            for (int i = 0; i < list.length; i++) {
                int testScore = rc.senseRubble(rc.getLocation().add(list[i]));
                if (rc.senseRobotAtLocation(rc.getLocation().add(list[i])) == null) {
                    if (testScore < 40) {
                        lessThanForty.add(list[i]);
                    }
                    bestDir = list[i];
                }
            }
            if (lessThanForty.size() == 0) {
                return bestDir;
            }
            return closestDirectionToCenter(lessThanForty);
        }

    }

    private Direction closestDirectionToCenter(ArrayList<Direction> dirs) {
        int bestDist = Integer.MAX_VALUE;
        Direction bestDir = null;
        for (int i = 0; i < dirs.size(); i++) {
            int newDist = rc.getLocation().add(dirs.get(i)).distanceSquaredTo(Util.getCenterLocation(rc));
            if (newDist < bestDist) {
                bestDist = newDist;
                bestDir = dirs.get(i);
            }
        }
        return bestDir;
    }

    private int getLeadScore(MapLocation loc) throws GameActionException {
        MapLocation[] nearby = rc.senseNearbyLocationsWithLead(loc, RobotType.MINER.visionRadiusSquared, 2);
        int score = 0;
        if (nearby.length == 0) {
            return 0;//if the code is cleaner this line could also be "return score;"
        }
        for (int i = 0; i < nearby.length; i++) {
            if (loc.distanceSquaredTo(nearby[i]) <= RobotType.MINER.actionRadiusSquared) {
                score += ((rc.senseLead(nearby[i]) * 3) / (loc.distanceSquaredTo(nearby[i]) + 1));
            } else {
                score += (((rc.senseLead(nearby[i])) * 2) / (loc.distanceSquaredTo(nearby[i]) + 1));
            }

        }
        return score;
    }


    private List<Double> usedDispersionAngles = new ArrayList<>();
    private double dispersionAngleThreshold = Math.PI / 2;

    private void updateDispersionAngle() throws GameActionException {
        if (robot.getComms().getDispersionAngles()[archonIndex] != null) return;

        int tries = 0;
        Double angle = null;
        while (angle == null) {
            double theta = robot.getRng().nextDouble() * Math.PI * 2;
            if (isGoodDispersionAngle(theta)) {
                angle = theta;
            }
            ++tries;

            if (tries >= 20) {
                dispersionAngleThreshold /= 2;
                tries = 0;
            }
        }

        robot.getComms().setDispersionAngle(archonIndex, angle);
        usedDispersionAngles.add(angle);
    }

    private boolean isGoodDispersionAngle(double angle) {
        for (double used : usedDispersionAngles) {
            if (Math.abs(Util.cmpAngles(used, angle)) < dispersionAngleThreshold) {
                return false;
            }
        }

        int x = (int) (8 * Math.cos(angle));
        int y = (int) (8 * Math.sin(angle));
        MapLocation loc = rc.getLocation().translate(x, y);
        if (!Util.isOnTheMap(loc, rc)) {
            return false;
        }

        return true;
    }
}
