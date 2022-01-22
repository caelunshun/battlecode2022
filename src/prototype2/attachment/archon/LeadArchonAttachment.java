package prototype2.attachment.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import prototype2.SymmetryType;
import prototype2.Attachment;
import prototype2.BotConstants;
import prototype2.Robot;
import prototype2.RobotCategory;
import prototype2.build.BuildTables;
import prototype2.build.GoldBuild;
import prototype2.build.LeadBuild;

import java.util.ArrayList;
import java.util.List;

/**
 * Attachment for the lead archon - the archon
 * responsible for all building.
 */
public class LeadArchonAttachment extends Attachment {
    private final BuildTables buildTables = new BuildTables();

    private SymmetryType symmetry;

    public LeadArchonAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        rc.setIndicatorString("Lead Archon");
        incrementBuildWeights();

        if (symmetry == null) {
            computeSymmetry();
        }

        robot.getComms().clearRobotCounts();
    }

    private void incrementBuildWeights() throws GameActionException {
        buildTables.addWeight(LeadBuild.SOLDIER, 20);

        buildTables.addWeight(LeadBuild.MINER, 10);
        if (robot.getComms().getNumRobots(RobotCategory.MINER) < BotConstants.MIN_MINERS) {
            buildTables.addWeight(LeadBuild.MINER, 20);
        }

        if (robot.getComms().getNumRobots(RobotCategory.BUILDER) < BotConstants.MIN_BUILDERS) {
            buildTables.addWeight(LeadBuild.BUILDER, 10);
        }
        if (robot.getComms().getNumRobots(RobotCategory.BUILDER) > 0) {
            //buildTables.addWeight(LeadBuild.WATCHTOWER, 5);
        } else {
            buildTables.clearWeight(LeadBuild.WATCHTOWER);
            if (robot.getComms().getLeadBuild() == LeadBuild.WATCHTOWER) {
                robot.getComms().setLeadBuild(null);
            }
        }

        if (robot.getComms().getNumRobots(RobotCategory.LABORATORY) < BotConstants.MIN_LABS
            && robot.getComms().getNumRobots(RobotCategory.BUILDER) > 0) {
            buildTables.addWeight(LeadBuild.LABORATORY, 6);
        } else {
            buildTables.clearWeight(LeadBuild.LABORATORY);
        }

        buildTables.addWeight(GoldBuild.SAGE, 10);

        if (robot.getComms().getLeadBuild() == null) {
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
}
