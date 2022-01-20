package prototype2.attachment.archon;

import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import prototype2.Attachment;
import prototype2.Robot;
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
        incrementBuildWeights();
        build();
    }

    private void incrementBuildWeights() throws GameActionException {
        buildTables.addWeight(LeadBuild.SOLDIER, 30);
        buildTables.addWeight(LeadBuild.MINER, 10);

        buildTables.addWeight(GoldBuild.SAGE, 10);

        robot.getComms().setLeadBuild(buildTables.getHighestLeadWeight());
        robot.getComms().setGoldBuild(buildTables.getHighestGoldWeight());
    }

    private void build() throws GameActionException {
        GoldBuild goldBuild = buildTables.getHighestGoldWeight();
        RobotType goldType = goldBuild.getType();
        if (goldType != null && rc.getType().canBuild(goldType)) {
            if (robot.getAttachment(BaseArchonAttachment.class).tryBuild(goldType)) {
                buildTables.clearWeight(goldBuild);
            }
        }

        LeadBuild leadBuild = buildTables.getHighestLeadWeight();
        RobotType leadType = leadBuild.getType();
        if (leadType != null && rc.getType().canBuild(leadType)) {
            if (robot.getAttachment(BaseArchonAttachment.class).tryBuild(leadType)) {
                buildTables.clearWeight(leadBuild);
            }
        }
    }
}
