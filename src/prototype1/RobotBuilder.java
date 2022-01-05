package prototype1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1.archon.*;
import prototype1.builder.BuilderAttachment;
import prototype1.builder.RushBuilderAttachment;
import prototype1.generic.*;
import prototype1.laboratory.LaboratoryAttachment;
import prototype1.miner.MinerAttachment;
import prototype1.soldier.SoldierAttachment;
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
                robot.addAttachment(new RushBuilderAttachment(robot));
                /* robot.addAttachment(new BuilderAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot)); */
                break;
            case SOLDIER:
                if(robot.getComms().getBuildIndex() == 2) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.ROTATIONAL));
                }
                if(robot.getComms().getBuildIndex() == 3) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.HORIZONTAL));
                }
                if(robot.getComms().getBuildIndex() == 4) {
                    robot.addAttachment(new ScoutAttachment(robot, SymmetryType.VERTICAL));
                }
                robot.addAttachment(new SoldierAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
            case SAGE:
                break;
        }
    }
}
