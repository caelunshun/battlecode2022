package prototype2.attachment.archon;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import prototype2.Attachment;
import prototype2.BotConstants;
import prototype2.Robot;
import prototype2.RobotCategory;
import prototype2.build.BuildTables;
import prototype2.build.GoldBuild;
import prototype2.build.LeadBuild;

/**
 * Attachment for the lead archon - the archon
 * responsible for all building.
 */
public class LeadArchonAttachment extends Attachment {
    private final BuildTables buildTables = new BuildTables();

    public LeadArchonAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        rc.setIndicatorString("Lead Archon");
        build();
        incrementBuildWeights();

        robot.getComms().clearRobotCounts();
    }

    private void incrementBuildWeights() throws GameActionException {
        buildTables.addWeight(LeadBuild.SOLDIER, 20);

        buildTables.addWeight(LeadBuild.MINER, 5);
        if (robot.getComms().getNumRobots(RobotCategory.MINER) < BotConstants.MIN_MINERS) {
            buildTables.addWeight(LeadBuild.MINER, 20);
        }

        if (robot.getComms().getNumRobots(RobotCategory.BUILDER) < BotConstants.MIN_BUILDERS) {
            buildTables.addWeight(LeadBuild.BUILDER, 10);
        }
        if (robot.getComms().getNumRobots(RobotCategory.BUILDER) > 0) {
            buildTables.addWeight(LeadBuild.WATCHTOWER, 10);
        } else {
            buildTables.clearWeight(LeadBuild.WATCHTOWER);
            if (robot.getComms().getLeadBuild() == LeadBuild.WATCHTOWER) {
                robot.getComms().setLeadBuild(null);
            }
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

    private void build() throws GameActionException {
        GoldBuild goldBuild = robot.getComms().getGoldBuild();
        if (goldBuild != null) {
            RobotType goldType = goldBuild.getType();
            if (goldType != null && rc.getType().canBuild(goldType)) {
                if (robot.getAttachment(BaseArchonAttachment.class).tryBuild(goldType)) {
                    robot.getComms().setGoldBuild(null);
                }
            }
        }

        LeadBuild leadBuild = robot.getComms().getLeadBuild();
        if (leadBuild != null) {
            RobotType leadType = leadBuild.getType();
            if (leadType != null && rc.getType().canBuild(leadType)) {
                if (robot.getAttachment(BaseArchonAttachment.class).tryBuild(leadType)) {
                    robot.getComms().setLeadBuild(null);
                }
            }
        }

        rc.setIndicatorString("Builds: " + leadBuild + " / " + goldBuild);
    }
}
