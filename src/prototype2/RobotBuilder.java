package prototype2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype2.attachment.archon.BaseArchonAttachment;
import prototype2.attachment.builder.BuilderAttachment;
import prototype2.attachment.generic.*;
import prototype2.attachment.laboratory.LaboratoryAttachment;
import prototype2.attachment.miner.MinerAttachment;
import prototype2.attachment.miner.MinerDispersionAttachment;
import prototype2.attachment.sage.SageAttackAttachment;
import prototype2.attachment.sage.SageMicroAttachment;
import prototype2.attachment.soldier.SoldierMacroAttachment;
import prototype2.attachment.soldier.SoldierMicroAttachment;

public class RobotBuilder {
    private final RobotType type;

    public RobotBuilder(RobotType type) {
        this.type = type;
    }

    public Robot build(RobotController rc) throws GameActionException {
        Robot robot = new Robot(rc);
        addAttachments(robot);
        return robot;
    }

    private void addAttachments(Robot robot) throws GameActionException  {
        robot.addAttachment(new RobotCounterAttachment(robot));
        robot.addAttachment(new EnemySpotterAttachment(robot));
        if (robot.getRc().getType() != RobotType.MINER) {
            robot.addAttachment(new LeadSpotterAttachment(robot));
        }
        robot.addAttachment(new ArchonSpotterAttachment(robot));
        switch (type) {
            case ARCHON:
                robot.addAttachment(new BaseArchonAttachment(robot));
                break;
            case MINER:
                robot.addAttachment(new SacrificeWhenLowAttachment(robot));
                robot.addAttachment(new MinerAttachment(robot));
                robot.addAttachment(new MinerDispersionAttachment(robot));
                break;
            case SAGE:
                robot.addAttachment(new SacrificeWhenLowAttachment(robot));
                robot.addAttachment(new SageMicroAttachment(robot));
                robot.addAttachment(new SoldierMacroAttachment(robot));
                break;
            case SOLDIER:
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new SacrificeWhenLowAttachment(robot));
                robot.addAttachment(new SoldierMicroAttachment(robot));
                robot.addAttachment(new SoldierMacroAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                break;
            case BUILDER:
                robot.addAttachment(new BuilderAttachment(robot));
            case WATCHTOWER:
                robot.addAttachment(new AttackAttachment(robot));
            case LABORATORY:
                robot.addAttachment(new LaboratoryAttachment(robot));
        }
    }
}
