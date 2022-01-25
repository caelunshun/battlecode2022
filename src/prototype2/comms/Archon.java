package prototype2.comms;

import battlecode.common.MapLocation;

/**
 * An archon on our team.
 */
public class Archon {
    public MapLocation loc;
    public boolean isDestroyed;
    public int numLeadLocations;
    public boolean isLead;
    public boolean inPregame;

    public Archon(MapLocation loc, boolean isDestroyed, int numLeadLocations, boolean isLead, boolean inPregame) {
        this.loc = loc;
        this.isDestroyed = isDestroyed;
        this.numLeadLocations = numLeadLocations;
        this.isLead = isLead;
        this.inPregame = inPregame;
    }
}
