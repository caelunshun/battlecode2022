package prototype1.miner;

import battlecode.common.MapLocation;

import java.util.ArrayList;
import java.util.List;

public class LeadTile {
    public List<MapLocation> lead;
    public int roundLastVisited = -100;

    public void addLead(MapLocation loc) {
        if (lead == null) lead = new ArrayList<>(1);
        if (!lead.contains(loc)) {
            lead.add(loc);
        }
    }
}
