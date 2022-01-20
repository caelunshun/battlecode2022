package prototype2.comms;

import battlecode.common.MapLocation;

/**
 * An archon on our team.
 */
public class Archon {
    public final MapLocation loc;
    public final boolean isDestroyed;
    public final int numLeadLocations;

    public Archon(MapLocation loc, boolean isDestroyed, int numLeadLocations) {
        this.loc = loc;
        this.isDestroyed = isDestroyed;
        this.numLeadLocations = numLeadLocations;
    }
}
