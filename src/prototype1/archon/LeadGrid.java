package prototype1.archon;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype1.Robot;

public class LeadGrid {
    private final LeadTile[] tiles;
    private final RobotController rc;
    private static final int TILE_WIDTH = 6;

    public LeadGrid(Robot robot) {
        this.rc = robot.getRc();
        this.tiles = new LeadTile[(rc.getMapWidth() + TILE_WIDTH - 1) / TILE_WIDTH
                * (rc.getMapHeight() + TILE_WIDTH - 1) / TILE_WIDTH];
        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new LeadTile();
        }
    }

    public LeadTile getTile(MapLocation loc) {
        return tiles[loc.x / TILE_WIDTH + loc.y / TILE_WIDTH * (rc.getMapWidth() + TILE_WIDTH - 1) / TILE_WIDTH];
    }

    public LeadTile[] getTiles() {
        return tiles;
    }

    public MapLocation getLocationFromTileIndex(int index) {
        return new MapLocation(index % ((rc.getMapWidth() + TILE_WIDTH - 1) / TILE_WIDTH)
                * TILE_WIDTH, index / ((rc.getMapWidth() + TILE_WIDTH - 1) / TILE_WIDTH) * TILE_WIDTH);
    }
}
