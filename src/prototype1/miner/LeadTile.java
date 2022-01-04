package prototype1.miner;

import battlecode.common.MapLocation;

public class LeadTile {
    public MapLocation location;
    public int lastExhaustedRound = Integer.MIN_VALUE;

    public LeadTile(MapLocation location) {
        this.location = location;
    }
}
