package prototype1_pre_defense;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1_pre_defense.archon.*;
import prototype1_pre_defense.builder.BuilderAttachment;
import prototype1_pre_defense.builder.RushBuilderAttachment;
import prototype1_pre_defense.generic.*;
import prototype1_pre_defense.laboratory.LaboratoryAttachment;
import prototype1_pre_defense.miner.MinerAttachment;
import prototype1_pre_defense.soldier.SoldierAttachment;
import prototype1_pre_defense.watchtower.SageAttachment;
import prototype1_pre_defense.watchtower.WatchtowerAttachment;

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
        robot.addAttachment(new ArchonSpotterAttachment(robot));
        switch (type) {
            case ARCHON:
                robot.addAttachment(new ArchonAttachment(robot));
                break;
            case LABORATORY:
                robot.addAttachment(new LaboratoryAttachment(robot));
                break;
            case WATCHTOWER:
                robot.addAttachment(new WatchtowerAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                break;
            case MINER:
                robot.addAttachment(new MinerAttachment(robot));
                break;
            case BUILDER:
                if(robot.getRc().getRoundNum() < 1000) {
                    robot.addAttachment(new RushBuilderAttachment(robot));
                } else {
                    robot.addAttachment(new BuilderAttachment(robot));
                    robot.addAttachment(new RandomMovementAttachment(robot));
                }

                break;
            case SOLDIER:
                if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.ROTATIONAL));
                }
                if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET + 1) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.HORIZONTAL));
                }
                if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET + 2) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.VERTICAL));
                }
                robot.addAttachment(new SoldierAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
            case SAGE:
                robot.addAttachment(new SageAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
        }
    }
}
