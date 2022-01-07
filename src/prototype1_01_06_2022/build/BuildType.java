package prototype1_01_06_2022.build;

import battlecode.common.RobotType;

/**
 * What to build.
 *
 * We use this enum instead of RobotType to distinguish
 * between different variants of the same unit.
 */
public enum BuildType {
    BUILDER,
    MINER,
    SOLDIER,
    /**
     * Special case of SOLDIER that should only be built in archons
     * that are in danger.
     */
    DEFENSE_SOLDIER,
    WATCHTOWER,
    LABORATORY,
    SAGE;

    public RobotType getRobotType() {
        switch (this) {
            case BUILDER:
                return RobotType.BUILDER;
            case MINER:
                return RobotType.MINER;
            case SOLDIER:
            case DEFENSE_SOLDIER:
                return RobotType.SOLDIER;
            case WATCHTOWER:
                return RobotType.WATCHTOWER;
            case LABORATORY:
                return RobotType.LABORATORY;
            case SAGE:
                return RobotType.SAGE;
        }
        return null;
    }
}
