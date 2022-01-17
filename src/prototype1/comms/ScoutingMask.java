package prototype1.comms;

import battlecode.common.MapLocation;
import prototype1.Robot;
import prototype1.generic.SymmetryType;

import java.util.ArrayList;
import java.util.List;

/**
 * For each of the up to 12 possible locations of enemy
 * archons (3 types of symmetry for 4 archons), we store
 * whether or not we've spotted enemies nearby.
 */
public class ScoutingMask {
    private int bitmask;

    public ScoutingMask() {
        this(0);
    }

    public ScoutingMask(int bitmask) {
        this.bitmask = bitmask;
    }

    public boolean isEnemySpotted(int archonIndex, SymmetryType symmetry) {
        int index = archonIndex * 3 + symmetry.ordinal();
        return ((bitmask >> index) & 1) == 1;
    }

    public void markEnemySpotted(int archonIndex, SymmetryType symmetry) {
        int index = archonIndex * 3 + symmetry.ordinal();
        bitmask |= 1 << index;
    }

    public List<MapLocation> getEnemySpottedLocations(Robot robot) {
        List<MapLocation> result = new ArrayList<>();
        for (int archonIndex = 0; archonIndex < 4; archonIndex++) {
            for (SymmetryType symm : SymmetryType.values()) {
                if (isEnemySpotted(archonIndex, symm)) {
                    result.add(symm.getSymmetryLocation(robot.getFriendlyArchons().get(archonIndex), robot.getRc()));
                }
            }
        }
        return result;
    }

    public static List<MapLocation> getAllLocations(Robot robot) {
        List<MapLocation> result = new ArrayList<>();
        for (int archonIndex = 0; archonIndex < robot.getFriendlyArchons().size(); archonIndex++) {
            for (SymmetryType symm : SymmetryType.values()) {
                result.add(symm.getSymmetryLocation(robot.getFriendlyArchons().get(archonIndex), robot.getRc()));
            }
        }
        return result;
    }

    public int getBitmask() {
        return bitmask;
    }
}
