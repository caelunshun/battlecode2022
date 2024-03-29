package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.BotConstants;
import prototype1.Robot;
import prototype1.Util;
import prototype1.build.BuildType;
import prototype1.build.BuildWeightTable;
import prototype1.comms.BecomeSwarmLeader;
import prototype1.generic.SymmetryType;
import prototype1.nav.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArchonAttachment extends Attachment {
    private int lastBuiltIndex = -1;

    public static int SOLDIER_BUILDING_OFFSET = 3;

    private final int numArchons;

    private boolean isInDanger = false;

    private int lastRoundLead;

    private int[] lastLeadAmounts;

    public static final int tiebreakerRound = 1850;

    private BuildWeightTable buildWeights = new BuildWeightTable();

    private boolean isLead;

    private boolean isMapTestSmall;
    private boolean isMovingAwayFromRubble = false;
    private Navigator nav;
    private MapLocation idealSpot;

    public ArchonAttachment(Robot robot) throws GameActionException {
        super(robot);

        this.nav = new Navigator(robot);
        idealSpot = new MapLocation(rc.getLocation().x, rc.getLocation().y);
        robot.getComms().addFriendlyArchon(rc.getLocation());
        numArchons = rc.getArchonCount();

        isMapTestSmall = rc.senseNearbyLocationsWithLead(rc.getType().visionRadiusSquared).length
                > 40;
        lastRoundLead = 0;
        lastLeadAmounts = new int[5];
    }

    @Override
    public void doTurn() throws GameActionException {
        /*if(rc.getRoundNum() == 1){
            moveAway();
        }
        if(isMovingAwayFromRubble && rc.getRoundNum() != 1){
            moveAway();
        }*/
        System.arraycopy(lastLeadAmounts, 0,lastLeadAmounts,1,4);
        lastLeadAmounts[0] = getRoundLead();
        isLead = robot.getFriendlyArchons().indexOf(rc.getLocation()) == 0;
        isInDanger = isInDanger();
        robot.getComms().setArchonInDanger(robot.getFriendlyArchons().indexOf(rc.getLocation()), isInDanger);

        incrementBuildWeights();

        if (rc.getRoundNum() < tiebreakerRound) {
            build();
        } else {
            tiebreakerMode();
        }
        repair();
        computeSymmetry();

        if (rc.getRoundNum() == 2) {
            initialFriendlyArchons.addAll(robot.getFriendlyArchons());
        }

        updateDispersionAngle();

        if (isInDanger) {
            rc.setIndicatorString("In Danger");
        }
        rc.setIndicatorString(Arrays.toString(lastLeadAmounts));
        setLastRoundLead();
    }

    private void incrementBuildWeights() throws GameActionException {
        // Increment the weights in the build table based on priorities.
        boolean noLeadCollected = noLeadCollected();
        if (rc.getRoundNum() < 60) {
            if ((rc.getMapHeight() * rc.getMapWidth()) < 1800){
                if(noLeadCollected) {
                    buildWeights.addWeight(prototype1.build.BuildType.MINER, 5);
                    buildWeights.addWeight(prototype1.build.BuildType.SOLDIER, 15);
                } else {
                    buildWeights.addWeight(prototype1.build.BuildType.MINER, 25);
                    buildWeights.addWeight(prototype1.build.BuildType.SOLDIER, 25);
                }
            } else {
                if(noLeadCollected){
                    buildWeights.addWeight(prototype1.build.BuildType.MINER, 5);
                    buildWeights.addWeight(prototype1.build.BuildType.SOLDIER, 15);
                } else {
                    buildWeights.addWeight(prototype1.build.BuildType.MINER, 60);
                    buildWeights.addWeight(BuildType.SOLDIER, 20);
                }

            }
        } else {
            if(noLeadCollected){
                buildWeights.addWeight(BuildType.MINER, 1);
            } else {
                buildWeights.addWeight(prototype1.build.BuildType.MINER, 5);
            }
        }


        if (isInDanger()) {
            buildWeights.addWeight(BuildType.DEFENSE_SOLDIER, 200);
        } else if (rc.getRoundNum() >= 60) {
            buildWeights.addWeight(BuildType.SOLDIER, 30);
        }

        if (rc.getRoundNum() > BotConstants.DEFENSE_MODE_TURN) {
            int weight;
            if (isMapTestSmall) {
                if (rc.getTeamLeadAmount(rc.getTeam()) > 1000) {
                    weight = 5;
                } else {
                    weight = 2;
                }
            } else {
                weight = 50;
            }
            buildWeights.addWeight(BuildType.BUILDER, weight);
        }
    }
    public boolean noLeadCollected(){
        if(rc.getRoundNum() <= 12){
            return false;
        }
        for(int lead : lastLeadAmounts){
            if(lead != 0){
                return false;
            }
        }
        return true;
    }
    private void build() throws GameActionException {
        if (rc.getTeamGoldAmount(rc.getTeam()) >= 20) {
            tryBuild(RobotType.SAGE);
        }
        int currentBuildIndex = robot.getComms().getBuildIndex();
        if (currentBuildIndex - lastBuiltIndex < rc.getArchonCount() - 1 && !isInDanger) {
            // No need to balance builds if we have tons of lead.
            if (rc.getTeamLeadAmount(rc.getTeam()) < 1000 && rc.getRoundNum() > 2) {
                return;
            }
        }

        BuildType buildType = buildWeights.getHighestWeight();

        rc.setIndicatorString(buildType.toString());

        if (buildType == BuildType.DEFENSE_SOLDIER && !isInDanger && robot.isAnyArchonInDanger()) {
            return;
        }

        if (tryBuild(buildType.getRobotType())) {
            ++currentBuildIndex;
            robot.getComms().setBuildIndex(currentBuildIndex);
            lastBuiltIndex = currentBuildIndex;
            buildWeights.clearWeight(buildType);

            if (robot.getFriendlyArchons().size() == 1) {
                // We can build again if we have more lead.
                build();
            }
        }
    }

    private boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = getAvailableBuildDirection(type);
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Built a " + type);
            return true;
        }
        return false;
    }



    private Direction getAvailableBuildDirection(RobotType type) throws GameActionException {
        if(type == RobotType.MINER){
            int bestLeadScore = -1;
            Direction bestDir = Direction.CENTER;
            Direction[] list = Arrays.copyOf(Util.DIRECTIONS, Util.DIRECTIONS.length);
            Util.shuffle(list);
            for(int i = 0; i < list.length; i++){
                MapLocation loc = rc.getLocation().add(list[i]);
                if (!rc.canSenseLocation(loc)) continue;
                if(rc.senseRobotAtLocation(loc) == null){
                    int leadScore = getLeadScore(rc.getLocation().add(list[i]));
                    if(leadScore > bestLeadScore || (leadScore == bestLeadScore && rc.senseRubble(rc.getLocation().add(list[i])) < rc.senseRubble(rc.getLocation().add(bestDir)) ) ){
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
                MapLocation loc = rc.getLocation().add(list[i]);
                if (!rc.canSenseLocation(loc)) continue;
                int testScore = rc.senseRubble(loc);
                if (rc.senseRobotAtLocation(loc) == null) {
                    if (testScore < 40) {
                        lessThanForty.add(list[i]);
                    }
                    bestDir = list[i];
                }
            }
            if(lessThanForty.size() == 0) {
                return bestDir;
            }
            return closestDirectionToCenter(lessThanForty);
        }

    }
    private Direction closestDirectionToCenter(ArrayList<Direction> dirs){
        int bestDist = Integer.MAX_VALUE;
        Direction bestDir = null;
        for(int i = 0; i < dirs.size(); i++){
            int newDist = rc.getLocation().add(dirs.get(i)).distanceSquaredTo(Util.getCenterLocation(rc));
            if( newDist < bestDist ){
                bestDist = newDist;
                bestDir = dirs.get(i);
            }
        }
        return bestDir;
    }
    private int getLeadScore(MapLocation loc) throws GameActionException{
        MapLocation[] nearby = rc.senseNearbyLocationsWithLead(loc, RobotType.MINER.visionRadiusSquared, 2);
        int score = 0;
        if(nearby.length == 0){
            return 0;//if the code is cleaner this line could also be "return score;"
        }
        for(int i = 0; i < nearby.length; i++){
            if(loc.distanceSquaredTo(nearby[i]) <= RobotType.MINER.actionRadiusSquared){
                score += ( (rc.senseLead(nearby[i]) * 3 ) / (loc.distanceSquaredTo(nearby[i]) + 1) );
            } else{
                score += ( ( ( rc.senseLead(nearby[i]) ) * 2 ) / (loc.distanceSquaredTo(nearby[i]) + 1 ) );
            }

        }
        return score;
    }

    private void repair() throws GameActionException {
        if (!rc.isActionReady()) {
            return;
        }
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        for (RobotInfo robs : nearbyRobots) {
            if (robs.getHealth() < robs.getType().health) {
                if (rc.canRepair(robs.getLocation())) {
                    rc.repair(robs.getLocation());
                    return;
                }
            }
        }
    }

    List<MapLocation> initialEnemyArchons = new ArrayList<>();
    List<MapLocation> initialFriendlyArchons = new ArrayList<>();
    List<MapLocation> destroyedFriendlyArchons = new ArrayList<>();

    private void computeSymmetry() throws GameActionException {
        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (!initialEnemyArchons.contains(enemy)) {
                initialEnemyArchons.add(enemy);
            }
        }
        for (MapLocation initialEnemy : initialEnemyArchons) {
            if (!robot.getEnemyArchons().contains(initialEnemy)
                    && !destroyedFriendlyArchons.contains(initialEnemy)) {
                destroyedFriendlyArchons.add(initialEnemy);
            }
        }

        List<SymmetryType> possibleSymmetry = new ArrayList<>();
        for (SymmetryType symmetry : SymmetryType.values()) {
            boolean symmetryWorks = true;
            List<MapLocation> usedReflections = new ArrayList<>();
            for (MapLocation enemyLoc : initialEnemyArchons) {
                boolean works = false;
                for (MapLocation ourLoc : initialFriendlyArchons) {
                    if (usedReflections.contains(ourLoc)) continue;
                    if (symmetry.getSymmetryLocation(ourLoc, rc).equals(enemyLoc)) {
                        works = true;
                        usedReflections.add(ourLoc);
                        break;
                    }
                }
                if (!works) symmetryWorks = false;
            }
            if (symmetryWorks) {
                possibleSymmetry.add(symmetry);
            }
        }

        if (possibleSymmetry.size() == 1) {
            robot.getComms().setSymmetryType(possibleSymmetry.get(0));
        } else if (!possibleSymmetry.isEmpty()) {
            if (initialEnemyArchons.size() == numArchons) {
                // Multiple possible symmetry types, but they all work,
                // as we know the entire map.
                robot.getComms().setSymmetryType(possibleSymmetry.get(0));
            }
        }

        // Add enemy archons based on symmetry
        SymmetryType symmetry = robot.getComms().getSymmetryType();
        if (symmetry == null) return;
        for (MapLocation friendly : initialFriendlyArchons) {
            MapLocation enemy = symmetry.getSymmetryLocation(friendly, rc);
            if (!robot.getEnemyArchons().contains(enemy) && !destroyedFriendlyArchons.contains(enemy)) {
                robot.getComms().addEnemyArchon(enemy);
                robot.update();
            }
        }
    }

    private void tiebreakerMode() throws GameActionException {
        if (rc.getRoundNum() % 50 == 0 && rc.getTeamGoldAmount(rc.getTeam()) > 2000) {
            if (rc.canBuildRobot(RobotType.BUILDER, getAvailableBuildDirection(RobotType.BUILDER))) {
                rc.buildRobot(RobotType.BUILDER, getAvailableBuildDirection(RobotType.BUILDER));
            }
        }
    }

    int inDangerTurns = 0;

    private boolean isInDanger() throws GameActionException {
        // Check if the total health of nearby enemy robots (that can attack)
        // is greater than the total health of our nearby robots (that can attack).
        double ourHealth = 0;
        double enemyHealth = 0;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.ID == rc.getID()) continue;
            if (info.type.canAttack()) {
                if (info.team == rc.getTeam()) {
                    ourHealth += info.health;
                } else {
                    enemyHealth += info.health;
                }
            }
        }

        boolean inDanger = enemyHealth * 1.6 > ourHealth;

        if (inDanger) inDangerTurns = 20;
        if (inDangerTurns-- > 0) {
            inDanger = true;
        }
        return inDanger;
    }

    int lastRushTurn = -1;

    private void initiateRush() throws GameActionException {
        // robot.getComms().setRushingArchon(null);

        if (rc.getRoundNum() < 1200) {
            return;
        }

        if (rc.getRoundNum() - lastRushTurn < 200) {
            return;
        }
        rc.setIndicatorString(robot.getEnemyArchons().toString());

        if (robot.getEnemyArchons().isEmpty()) return;

        robot.getComms().setRushingArchon(Util.getClosest(rc.getLocation(), robot.getEnemyArchons()));
        lastRushTurn = rc.getRoundNum();
    }

    public void moveAway() throws GameActionException {
        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        int spot = -1;
        if (locs.length == 0) {
            isMovingAwayFromRubble = false;
            return;
        }
        if (rc.senseRubble(rc.getLocation()) < 50 && rc.getMode() == RobotMode.TURRET) {

            isMovingAwayFromRubble = false;
            return;
        }
        isMovingAwayFromRubble = true;
        double minRubble = rc.senseRubble(rc.getLocation());
        for (int i = 0; i < locs.length; i++) {
            double rub = rc.senseRubble(locs[i]) + Math.pow(rc.getLocation().distanceSquaredTo(locs[i]), 0.2);
            if ((rub + Math.pow(rc.getLocation().distanceSquaredTo(locs[i]), 0.2)) < minRubble) {
                minRubble = rub;
                spot = i;
            }
        }
        if (spot == -1) {
            if (rc.getMode() == RobotMode.TURRET) {
                isMovingAwayFromRubble = false;
                return;
            }
            if (rc.canTransform()) {
                rc.transform();
            }
        } else {
            idealSpot = locs[spot];
        }
        moveToLoc(idealSpot);
    }

    public void moveToLoc(MapLocation toGo) throws GameActionException {
        if (rc.getMode() == RobotMode.TURRET) {
            if (rc.canTransform()) {
                rc.transform();
            }
            return;
        }
        if (rc.getLocation().equals(toGo)) {
            if (rc.getMode() == RobotMode.TURRET) {
                return;
            }
            if (rc.canTransform()) {
                rc.transform();
                isMovingAwayFromRubble = false;
            }
            robot.getComms().addFriendlyArchon(rc.getLocation());
            return;
        }
        nav.advanceToward(toGo);
    }

    private List<Double> usedDispersionAngles = new ArrayList<>();
    private double dispersionAngleThreshold = Math.PI / 2;

    private void updateDispersionAngle() throws GameActionException {
        int archonIndex = robot.getFriendlyArchons().indexOf(rc.getLocation());
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
        rc.setIndicatorString("Dispersion Angle: " + Math.toDegrees(angle));
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

    public int getRoundLead() throws GameActionException{
        return robot.getComms().getTurnLeadAmount() - lastRoundLead;
    }
    public void setLastRoundLead() throws GameActionException{
        lastRoundLead = robot.getComms().getTurnLeadAmount();
    }
}
