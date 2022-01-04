package prototype1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1.archon.*;
import prototype1.builder.BuilderAttachment;
import prototype1.generic.ArchonSpotterAttachment;
import prototype1.generic.AttackAttachment;
import prototype1.generic.RandomMovementAttachment;
import prototype1.generic.ScoutAttachment;
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
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
            case MINER:
                robot.addAttachment(new MinerAttachment(robot));
                break;
            case BUILDER:
                robot.addAttachment(new BuilderAttachment(robot));
                break;
            case SOLDIER:
                if(robot.getRc().getRoundNum() < 10){
                    robot.addAttachment(new ScoutAttachment(robot));
                    robot.addAttachment(new SoldierAttachment(robot));
                    robot.addAttachment(new AttackAttachment(robot));
                    robot.addAttachment(new RandomMovementAttachment(robot));
                } else {
                    robot.addAttachment(new SoldierAttachment(robot));
                    robot.addAttachment(new AttackAttachment(robot));
                    robot.addAttachment(new RandomMovementAttachment(robot));
                }
                break;
            case SAGE:
                break;
        }
    }
}
