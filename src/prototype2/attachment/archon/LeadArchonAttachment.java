package prototype2.attachment.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype2.comms.CryForHelp;
import prototype2.*;
import prototype2.build.BuildTables;
import prototype2.build.GoldBuild;
import prototype2.build.LeadBuild;
import prototype2.comms.EnemySpottedLocation;

import java.util.ArrayList;
import java.util.List;


/**
 * Attachment for the lead archon - the archon
 * responsible for all building.
 */
public class LeadArchonAttachment extends Attachment {
    private final BuildTables buildTables = new BuildTables();
    int[] lastLeadAmounts;
    private int lastRoundLead;

    private int totalLeadCollected;

    private SymmetryType symmetry;

    public LeadArchonAttachment(Robot robot) {
        super(robot);
        lastLeadAmounts = new int[5];
    }

    @Override
    public void doTurn() throws GameActionException {
        protMiners();
        if (rc.getRoundNum() == 100) {
            rc.disintegrate();
        }

        updateStates();
        updateLeadAmounts();
        incrementBuildWeights();

        /*if (symmetry == null) {
            computeSymmetry();
        }*/

        robot.getComms().clearRobotCounts();
        setLastRoundLead();
        setStrategy();


        // rc.setIndicatorString("Lead Archon. Strategy: " + robot.getComms().getStrategy() + " / Lead Build: " + robot.getComms().getLeadBuild());
    }

    private void protMiners() throws GameActionException {
        if (robot.getComms().getNumRobots(RobotCategory.MINER) < 2) {
            robot.getComms().doNotMakeGold();
            rc.setIndicatorString("oops. there are " + robot.getComms().getNumRobots(RobotCategory.WATCHTOWER_L1));
        } else {

            robot.getComms().makeGold();
        }
    }

    // States reset each turn
    private int numMiners;
    private int numBuilders;
    private int numLabs;
    private int numWatchtowerL1s;
    private int numWatchtowerL2s;
    private boolean nearbyEnemies;
    private boolean areMinersSaturated;

    private void updateStates() throws GameActionException {
        numMiners = robot.getComms().getNumRobots(RobotCategory.MINER);
        numBuilders = robot.getComms().getNumRobots(RobotCategory.BUILDER);
        numLabs = robot.getComms().getNumRobots(RobotCategory.LABORATORY);
        numWatchtowerL1s = robot.getComms().getNumRobots(RobotCategory.WATCHTOWER_L1);
        numWatchtowerL2s = robot.getComms().getNumRobots(RobotCategory.WATCHTOWER_L2);

        nearbyEnemies = false;
        for (CryForHelp cry : robot.getComms().getCriesForHelp()) {
            if (cry != null && cry.enemyLoc.distanceSquaredTo(rc.getLocation()) <= 100
                    && rc.getRoundNum() - cry.roundNumber <= 2) {
                nearbyEnemies = true;
            }
        }
        for (EnemySpottedLocation loc : robot.getComms().getEnemySpottedLocations()) {
            if (loc != null && loc.loc.distanceSquaredTo(rc.getLocation()) <= 100
                    && rc.getRoundNum() - loc.roundNumber <= 2) {
                nearbyEnemies = true;
            }
        }

        int numLeadCollected = getRoundLead();
        areMinersSaturated = numLeadCollected / 2 >= numMiners;
    }

    private void incrementBuildWeights() throws GameActionException {
        Strategy strat = robot.getComms().getStrategy();
        boolean noStrat = strat == null;
        if (noStrat) strat = Strategy.RUSH;

        int weightSoldier = 0;
        int weightMiner = 0;
        int weightBuilder = 0;
        int weightWatchtower = 0;
        int weightLab = 0;
        int weightSage = 0;

        switch (strat) {
            case RUSH:
                weightSoldier = 20;
                weightMiner = 10;
                if (numBuilders < BotConstants.MIN_BUILDERS) {
                    weightBuilder = 10;
                }
                if (numLabs < BotConstants.MIN_LABS
                        && numBuilders > 0) {
                    weightLab = 6;
                }
                weightSage = 10;
                break;
            case TURTLE:
                if (nearbyEnemies) {
                    weightSoldier = 10;
                }
                weightBuilder = 10;
                if (nearbyEnemies) {
                    weightBuilder = 6;
                }
                if ((areMinersSaturated || (lastLeadAmounts[0] < 2 && lastLeadAmounts[1] < 2 && lastLeadAmounts[2] < 2))
                        && rc.getRoundNum() > 300) {
                    weightMiner = 15;
                }
                if (numBuilders > 0) {
                    weightWatchtower = 15;
                    if (rc.getTeamLeadAmount(rc.getTeam()) > 150) {
                        weightWatchtower = 20;
                    }
                }
                weightSage = 10;

                if (rc.getTeamLeadAmount(rc.getTeam()) > 500 && numLabs < BotConstants.MIN_LABS
                        && numBuilders > 0) {
                    weightLab = 20;
                }

                break;
        }

        if (noStrat) {
            weightLab = 0;
        }

        if (numMiners < BotConstants.MIN_MINERS) {
            weightMiner = 20;
        }

        buildTables.addWeight(LeadBuild.MINER, weightMiner);
        buildTables.addWeight(LeadBuild.SOLDIER, weightSoldier);
        buildTables.addWeight(LeadBuild.BUILDER, weightBuilder);
        buildTables.addWeight(LeadBuild.LABORATORY, weightLab);
        buildTables.addWeight(LeadBuild.WATCHTOWER, weightWatchtower);
        buildTables.addWeight(GoldBuild.SAGE, weightSage);

        // Prevent waiting to build watchtower/lab when we have no builders
        if (weightLab == 0) {
            clearWeight(LeadBuild.LABORATORY);
        }
        if (weightWatchtower == 0) {
            clearWeight(LeadBuild.WATCHTOWER);
        }

        if (robot.getComms().getLeadBuild() == null
                || buildTables.getWeight(buildTables.getHighestLeadWeight()) > 200) {
            LeadBuild build = buildTables.getHighestLeadWeight();
            robot.getComms().setLeadBuild(build);
            buildTables.clearWeight(build);
        }
        if (robot.getComms().getGoldBuild() == null) {
            GoldBuild build = buildTables.getHighestGoldWeight();
            robot.getComms().setGoldBuild(build);
            buildTables.clearWeight(build);
        }
    }

    private void clearWeight(LeadBuild build) throws GameActionException {
        buildTables.clearWeight(build);
        if (robot.getComms().getLeadBuild() == build) {
            robot.getComms().setLeadBuild(null);
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
            if (initialEnemyArchons.size() == rc.getArchonCount()) {
                // Multiple possible symmetry types, but they all work,
                // as we know the entire map.
                symmetry = possibleSymmetry.get(0);
            }
        }

        // Add enemy archons based on symmetry
        if (symmetry == null) return;
        for (MapLocation friendly : initialFriendlyArchons) {
            MapLocation enemy = symmetry.getSymmetryLocation(friendly, rc);
            if (!robot.getEnemyArchons().contains(enemy) && !destroyedFriendlyArchons.contains(enemy)) {
                robot.getComms().addEnemyArchon(enemy);
                robot.update();
            }
        }
    }

    private void updateLeadAmounts() throws GameActionException {
        System.arraycopy(lastLeadAmounts, 0, lastLeadAmounts, 1, 4);
        lastLeadAmounts[0] = getRoundLead();
    }

    public int getRoundLead() throws GameActionException {
        return robot.getComms().getTurnLeadAmount() - lastRoundLead;
    }

    public void setLastRoundLead() throws GameActionException {
        lastRoundLead = robot.getComms().getTurnLeadAmount();
        totalLeadCollected = lastRoundLead;
    }

    private void setStrategy() throws GameActionException {
        if (robot.getComms().getStrategy() != null) return;

        if (rc.getRoundNum() < 25) return;

        Strategy strat;
        if (totalLeadCollected / rc.getArchonCount() < 70 || rc.getMapWidth() * rc.getMapHeight() <= 1400) {
            strat = Strategy.TURTLE;
        } else {
            strat = Strategy.RUSH;
        }

        robot.getComms().setStrategy(strat);
    }
}
