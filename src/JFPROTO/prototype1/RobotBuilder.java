package JFPROTO.prototype1;

import JFPROTO.prototype1.archon.ArchonAttachment;
import JFPROTO.prototype1.builder.DefenseBuilderAttachment;
import JFPROTO.prototype1.builder.LabBuilderAttachment;
import JFPROTO.prototype1.builder.SacrificeBuilderAttachment;
import JFPROTO.prototype1.generic.ArchonSpotterAttachment;
import JFPROTO.prototype1.generic.AttackAttachment;
import JFPROTO.prototype1.generic.DispersionAttachment;
import JFPROTO.prototype1.generic.LeadSpotterAttachment;
import JFPROTO.prototype1.laboratory.LaboratoryAttachment;
import JFPROTO.prototype1.miner.MinerAttachment;
import JFPROTO.prototype1.sage.SageAttachment;
import JFPROTO.prototype1.soldier.SwarmSoldierAttachment;
import JFPROTO.prototype1.watchtower.WatchtowerAttachment;
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
