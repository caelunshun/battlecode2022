package prototype2;

import battlecode.common.RobotController;

public class BotConstants {
    public static final int MIN_BUILDERS = 1;
    public static final int MIN_MINERS = 2;

    public static int getPregameMinersPerArchon(RobotController rc) {
        int totalMiners;
        if (rc.getMapHeight() * rc.getMapWidth() <= 1800) {
            totalMiners = 6;
        } else {
            totalMiners = 12;
        }
        return totalMiners / rc.getArchonCount();
    }
}
