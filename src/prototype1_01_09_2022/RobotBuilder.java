package prototype1_01_09_2022;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1_01_09_2022.archon.*;
import prototype1_01_09_2022.builder.DefenseBuilderAttachment;
import prototype1_01_09_2022.builder.LabBuilderAttachment;
import prototype1_01_09_2022.builder.RushBuilderAttachment;
import prototype1_01_09_2022.builder.SacrificeBuilderAttachment;
import prototype1_01_09_2022.generic.*;
import prototype1_01_09_2022.laboratory.LaboratoryAttachment;
import prototype1_01_09_2022.miner.MinerAttachment;
import prototype1_01_09_2022.soldier.SoldierAttachment;
import prototype1_01_09_2022.soldier.SwarmSoldierAttachment;
import prototype1_01_09_2022.watchtower.SageAttachment;
import prototype1_01_09_2022.watchtower.WatchtowerAttachment;

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
                robot.addAttachment(new DispersionAttachment(robot));
                break;
            case BUILDER:
                if(robot.getRc().getRoundNum() < ArchonAttachment.tiebreakerRound) {
                    if (robot.getRc().getTeamLeadAmount(robot.getRc().getTeam()) > 1000 && robot.getRng().nextFloat() < 0.3) {
                        robot.addAttachment(new DefenseBuilderAttachment(robot));
                    } else {
                        robot.addAttachment(new SacrificeBuilderAttachment(robot));
                    }
                } else {
                    robot.addAttachment(new LabBuilderAttachment(robot));
                }

                break;
            case SOLDIER:
                /*if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.ROTATIONAL));
                }
                if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET + 1) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.HORIZONTAL));
                }
                if(robot.getComms().getBuildIndex() == ArchonAttachment.SOLDIER_BUILDING_OFFSET + 2) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.VERTICAL));
                }*/
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new SwarmSoldierAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                break;
            case SAGE:
                robot.addAttachment(new SageAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
        }
    }
}
