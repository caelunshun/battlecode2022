package prototype1.comms;

import battlecode.common.MapLocation;

public class EnemySpottedLocation {
    public final MapLocation loc;
    public final int roundNumber;

    public EnemySpottedLocation(MapLocation loc, int roundNumber) {
        this.loc = loc;
        this.roundNumber = roundNumber;
    }
}
