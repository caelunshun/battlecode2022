package prototype1.comms;

import battlecode.common.MapLocation;

public final class LeadCluster {
    public MapLocation loc;
    public int numClaims;

    public static final int MAX_CLAIMS = 3;

    public LeadCluster(MapLocation loc, int numClaims) {
        this.loc = loc;
        this.numClaims = numClaims;
    }
}
