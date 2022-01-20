package prototype1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1.archon.*;
import prototype1.builder.DefenseBuilderAttachment;
import prototype1.builder.LabBuilderAttachment;
import prototype1.builder.SacrificeBuilderAttachment;
import prototype1.generic.*;
import prototype1.laboratory.LaboratoryAttachment;
import prototype1.miner.MinerAttachment;
import prototype1.soldier.SoldierMacroAttachment;
import prototype1.soldier.SoldierMicroAttachment;
import prototype1.sage.SageAttachment;
import prototype1.watchtower.WatchtowerAttachment;

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
      //  robot.addAttachment(new EnemySpotterAttachment(robot));
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
                robot.addAttachment(new MinerDispersionAttachment(robot));
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
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new SoldierMicroAttachment(robot));
                robot.addAttachment(new SoldierMacroAttachment(robot));
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
