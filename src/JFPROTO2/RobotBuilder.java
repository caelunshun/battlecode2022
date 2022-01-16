package JFPROTO2;

import JFPROTO2.archon.ArchonAttachment;
import JFPROTO2.builder.DefenseBuilderAttachment;
import JFPROTO2.builder.LabBuilderAttachment;
import JFPROTO2.builder.SacrificeBuilderAttachment;
import JFPROTO2.generic.ArchonSpotterAttachment;
import JFPROTO2.generic.AttackAttachment;
import JFPROTO2.generic.DispersionAttachment;
import JFPROTO2.generic.LeadSpotterAttachment;
import JFPROTO2.laboratory.LaboratoryAttachment;
import JFPROTO2.miner.MinerAttachment;
import JFPROTO2.sage.SageAttachment;
import JFPROTO2.soldier.SwarmSoldierAttachment;
import JFPROTO2.watchtower.WatchtowerAttachment;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

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
                robot.addAttachment(new LeadSpotterAttachment(robot));
                break;
            case SAGE:
                robot.addAttachment(new SageAttachment(robot));
                robot.addAttachment(new DispersionAttachment(robot));
                break;
        }
    }
}
