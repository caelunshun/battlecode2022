package prototype1.comms;

import battlecode.common.MapLocation;

public final class LeadCluster {
    public MapLocation loc;
    public int numClaims;

    public LeadCluster(MapLocation loc, int numClaims) {
        this.loc = loc;
        this.numClaims = numClaims;
    }

    @Override
    public String toString() {
        return loc.toString();
    }
}
