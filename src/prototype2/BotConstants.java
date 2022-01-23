package prototype2;

import battlecode.common.RobotController;

public class BotConstants {
    public static final int MIN_BUILDERS = 1;
    public static final int MAX_BUILDERS = 5;
    public static final int MIN_MINERS = 1;
    public static final int MIN_LABS = 2;

    public static int getPregameMinersPerArchon(RobotController rc) {
        int totalMiners;
        if (rc.getMapHeight() * rc.getMapWidth() <= 1800) {
            totalMiners = 4;
        } else {
            totalMiners = 12;
        }
        return totalMiners / rc.getArchonCount();
    }
}
