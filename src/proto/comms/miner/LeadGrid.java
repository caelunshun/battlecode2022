package proto.comms.miner;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import proto.comms.Robot;

public class LeadGrid {
    private final LeadTile[] tiles;
    private final RobotController rc;

    public LeadGrid(Robot robot) {
        this.rc = robot.getRc();
        this.tiles = new LeadTile[rc.getMapWidth() * rc.getMapHeight()];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new LeadTile();
        }
    }

    public LeadTile getTile(MapLocation loc) {
        return tiles[loc.x + loc.y * rc.getMapWidth()];
    }

    public LeadTile[] getTiles() {
        return tiles;
    }

    public MapLocation getLocationFromTileIndex(int index) {
        return new MapLocation(index % rc.getMapWidth(), index / rc.getMapWidth());
    }
}