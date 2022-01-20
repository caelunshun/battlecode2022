package prototype2.attachment.generic;

import battlecode.common.GameActionException;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.RobotCategory;

/**
 * Updates robot counters in the communications array.
 *
 * We increment the counter for the robot's corresponding
 * category when it is created. When it is about to die, we
 * decrement the counter.
 *
 * We can only predict when a robot is about to die.
 */
public class RobotCounterAttachment extends Attachment {
    private RobotCategory registeredCategory;
    private boolean registeredDeath = false;

    public RobotCounterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        RobotCategory category = getCategory();
        if (category == null) return;

        if (category != registeredCategory) {
            robot.getComms().incrementNumRobots(category);
            registeredCategory = category;
        }

        if (isAboutToDie() && !registeredDeath) {
            robot.getComms().decrementNumRobots(category);
            registeredDeath = true;
        }
    }

    private boolean isAboutToDie() {
        return rc.getHealth() <= 10;
    }

    private RobotCategory getCategory() {
        switch (rc.getType()) {
            case LABORATORY:
                return RobotCategory.LABORATORY;
            case WATCHTOWER:
                switch (rc.getLevel()) {
                    case 1:
                        return RobotCategory.WATCHTOWER_L1;
                    case 2:
                        return RobotCategory.WATCHTOWER_L2;
                    case 3:
                        return RobotCategory.WATCHTOWER_L3;
                    default:
                        throw new RuntimeException("invalid mutation level?");
                }
            case MINER:
                return RobotCategory.MINER;
            case BUILDER:
                return RobotCategory.BUILDER;
            case SOLDIER:
                return RobotCategory.SOLDIER;
            case SAGE:
                return RobotCategory.SAGE;
            default:
                return null;
        }
    }
}
