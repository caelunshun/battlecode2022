package prototype1.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype1.BotConstants;
import prototype1.Util;
import prototype1.build.BuildWeightTable;
import prototype1.generic.SymmetryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles communications via the shared array.
 *
 * The shared array consists of 64 16-bit unsigned integers.
 * We split the array into slots, each of which contains two
 * unsigned integers (equal to 32 bits), for a total of 32 slots.
 *
 * A slot is "free" if it is set to 0. Otherwise, it's set to the
 * contents of the slot, plus 1, in big endian order.
 *
 * The meaning of a slot depends on its position in the array:
 * - first 4 slots: the positions of our archons
 * - next 4 slots: the positions of enemy archons
 * - next slot: archon danger statuses
 * - next slot: the archon getting rushed
 * - next 4 slots: cries for help
 */
public final class Communications {
    private final RobotController rc;

    private static final Range SEGMENT_FRIENDLY_ARCHONS = new Range(0, 4);
    private static final Range SEGMENT_ENEMY_ARCHONS = new Range(4, 8);
    private static final int ARCHON_DANGER = 8;
    private static final int RUSHING_ARCHON = 9;
    private static final Range SEGMENT_CRIES_FOR_HELP = new Range(10, 14);
    private static final Range SEGMENT_LEAD_LOCATIONS = new Range(14, 19);
    private static final Range SEGMENT_DISPERSION_ANGLES = new Range(20, 24);
    private static final int LEAD_COUNTER = 24;
    private static final int SCOUTING_MASK = 25;
    private static final Range ENEMY_SPOTTED_LOCATIONS = new Range(25, 28);

    public Communications(RobotController rc) {
        this.rc = rc;
    }
    public void addTurnLeadAmount(int amount) throws GameActionException{
        if(isSlotFree(LEAD_COUNTER)){
            writeSlot(LEAD_COUNTER,amount);
        } else {
            writeSlot(LEAD_COUNTER, readSlot(LEAD_COUNTER) + amount);
        }
    }
    public int getTurnLeadAmount() throws GameActionException{
        if(isSlotFree(LEAD_COUNTER)){
            return 0;
        }
        return readSlot(LEAD_COUNTER);
    }
    public List<MapLocation> readFriendlyArchons() throws GameActionException {
        List<MapLocation> res = new ArrayList<>(4);
        for (int i = SEGMENT_FRIENDLY_ARCHONS.start; i < SEGMENT_FRIENDLY_ARCHONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                res.add(dec.readMapLocation());
            }
        }
        return res;
    }

    public List<MapLocation> readEnemyArchons() throws GameActionException {
        List<MapLocation> res = new ArrayList<>(4);
        for (int i = SEGMENT_ENEMY_ARCHONS.start; i < SEGMENT_ENEMY_ARCHONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                res.add(dec.readMapLocation());
            }
        }
        return res;
    }

    public void addFriendlyArchon(MapLocation loc) throws GameActionException {
        int slot = getFreeSlot(SEGMENT_FRIENDLY_ARCHONS);
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(loc);
        writeSlot(slot, enc.finish());
    }

    public void addEnemyArchon(MapLocation loc) throws GameActionException {
        int slot = getFreeSlot(SEGMENT_ENEMY_ARCHONS);
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(loc);
        writeSlot(slot, enc.finish());
    }

    public void removeEnemyArchon(MapLocation loc) throws GameActionException {
        for (int i = SEGMENT_ENEMY_ARCHONS.start; i < SEGMENT_ENEMY_ARCHONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                if (dec.readMapLocation().equals(loc)) {
                    clearSlot(i);
                }
            }
        }
    }

    public void removeFriendlyArchon(MapLocation loc) throws GameActionException {
        for (int i = SEGMENT_FRIENDLY_ARCHONS.start; i < SEGMENT_FRIENDLY_ARCHONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                if (dec.readMapLocation().equals(loc)) {
                    clearSlot(i);
                }
            }
        }
    }

    public boolean isArchonInDanger(int archonIndex) throws GameActionException {
        int mask = readSlot(ARCHON_DANGER);
        return ((mask >>> archonIndex) & 1) == 1;
    }

    public void setArchonInDanger(int archonIndex, boolean inDanger) throws GameActionException {
        int mask = readSlot(ARCHON_DANGER);
        if (isSlotFree(ARCHON_DANGER)) mask = 0;
        if (inDanger) {
            mask |= 1 << archonIndex;
        } else {
            mask &= ~(1 << archonIndex);
        }
        writeSlot(ARCHON_DANGER, mask);
    }

    public MapLocation getRushingArchon() throws GameActionException {
        if (isSlotFree(RUSHING_ARCHON)) return null;
        BitDecoder dec = new BitDecoder(readSlot(RUSHING_ARCHON));
        return dec.readMapLocation();
    }

    public void setRushingArchon(MapLocation loc) throws GameActionException {
        if (loc == null) {
            clearSlot(RUSHING_ARCHON);
        } else {
            BitEncoder enc = new BitEncoder();
            enc.writeMapLocation(loc);
            writeSlot(RUSHING_ARCHON, enc.finish());
        }
    }

    public CryForHelp[] getCriesForHelp() throws GameActionException {
        CryForHelp[] res = new CryForHelp[4];
        for (int i = SEGMENT_CRIES_FOR_HELP.start; i < SEGMENT_CRIES_FOR_HELP.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                MapLocation enemyLoc = dec.readMapLocation();
                int numEnemies = dec.read(6);
                int roundNumber = dec.read(12);
                res[i - SEGMENT_CRIES_FOR_HELP.start] = new CryForHelp(enemyLoc, numEnemies, roundNumber);
            }
        }
        return res;
    }

    public void addCryForHelp(CryForHelp cry) throws GameActionException {
        if (clearCries(cry.enemyLoc)) return;

        int free = -1;
        for (int i = SEGMENT_CRIES_FOR_HELP.start; i < SEGMENT_CRIES_FOR_HELP.end; i++) {
            if (isSlotFree(i)) {
                free = i;
                break;
            }
        }

        if (free != -1) {
            BitEncoder enc = new BitEncoder();
            enc.writeMapLocation(cry.enemyLoc);
            enc.write(cry.numEnemies, 6);
            enc.write(cry.roundNumber, 12);
            writeSlot(free, enc.finish());
        }
     }

    private boolean clearCries(MapLocation enemyLoc) throws GameActionException {
        CryForHelp[] cries = getCriesForHelp();
        for (int i = 0; i < cries.length; i++) {
            if (cries[i] != null) {
                if (cries[i].enemyLoc.distanceSquaredTo(enemyLoc) <= 9) {
                    return true;
                } else if (rc.getRoundNum() - cries[i].roundNumber > 1) {
                    clearSlot(SEGMENT_CRIES_FOR_HELP.start + i);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public EnemySpottedLocation[] getEnemySpottedLocations() throws GameActionException {
        EnemySpottedLocation[] res = new EnemySpottedLocation[4];
        for (int i = ENEMY_SPOTTED_LOCATIONS.start; i < ENEMY_SPOTTED_LOCATIONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                MapLocation enemyLoc = dec.readMapLocation();
                int roundNumber = dec.read(12);
                res[i - ENEMY_SPOTTED_LOCATIONS.start] = new EnemySpottedLocation(enemyLoc, roundNumber);
            }
        }
        return res;
    }

    public void addEnemySpottedLocation(EnemySpottedLocation loc) throws GameActionException {
        if (clearSpotted(loc.loc)) return;

        int free = -1;
        for (int i = SEGMENT_CRIES_FOR_HELP.start; i < SEGMENT_CRIES_FOR_HELP.end; i++) {
            if (isSlotFree(i)) {
                free = i;
                break;
            }
        }

        if (free != -1) {
            BitEncoder enc = new BitEncoder();
            enc.writeMapLocation(loc.loc);
            enc.write(loc.roundNumber, 12);
            writeSlot(free, enc.finish());
        }
    }

    private boolean clearSpotted(MapLocation enemyLoc) throws GameActionException {
        EnemySpottedLocation[] locs = getEnemySpottedLocations();
        for (int i = 0; i < locs.length; i++) {
            if (locs[i] != null) {
                if (locs[i].loc.distanceSquaredTo(enemyLoc) <= 9) {
                    return true;
                } else if (rc.getRoundNum() - locs[i].roundNumber > 3) {
                    clearSlot(SEGMENT_CRIES_FOR_HELP.start + i);
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public ScoutingMask getScoutingMask() throws GameActionException {
        if (isSlotFree(SCOUTING_MASK)) {
            return new ScoutingMask();
        } else {
           return new ScoutingMask(readSlot(SCOUTING_MASK));
        }
    }

    public void setScoutingMask(ScoutingMask mask) throws GameActionException {
        writeSlot(SCOUTING_MASK, mask.getBitmask());
    }

    public SymmetryType getSymmetryType() throws GameActionException {
        if (rc.readSharedArray(62) == 0) return null;
        return SymmetryType.values()[rc.readSharedArray(62) - 1];
    }

    public void setSymmetryType(SymmetryType type) throws GameActionException {
        rc.writeSharedArray(62, type.ordinal() + 1);
    }

    public int getBuildIndex() throws GameActionException {
        return rc.readSharedArray(63);
    }

    public void setBuildIndex(int index) throws GameActionException {
        rc.writeSharedArray(63, index);
    }

    public MapLocation[] getLeadLocations() throws GameActionException {
        MapLocation[] res = new MapLocation[5];
        for (int i = SEGMENT_LEAD_LOCATIONS.start; i < SEGMENT_LEAD_LOCATIONS.end; i++) {
            if (isSlotFree(i)) continue;
            BitDecoder dec = new BitDecoder(readSlot(i));
            res[i - SEGMENT_LEAD_LOCATIONS.start] = dec.readMapLocation();
        }
        return res;
    }

    public void clearLeadLocation(int index) throws GameActionException {
        clearSlot(SEGMENT_LEAD_LOCATIONS.start + index);
    }

    public void addLeadLocation(MapLocation loc) throws GameActionException {
        MapLocation[] locs = getLeadLocations();
        int index = -1;
        for (int i = 0; i < locs.length; i++) {
            if (locs[i] == null) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            index = new Random(33).nextInt(5);
        }

        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(loc);
        writeSlot(SEGMENT_LEAD_LOCATIONS.start + index, enc.finish());
    }

    private int getFreeSlot(Range segment) throws GameActionException {
        for (int i = segment.start; i < segment.end; i++) {
            if (isSlotFree(i)) {
                return i;
            }
        }
        return -1;
    }

    public Double[] getDispersionAngles() throws GameActionException {
        Double[] angles = new Double[4];
        for (int i = 0; i < angles.length; i++) {
            int slot = SEGMENT_DISPERSION_ANGLES.start + i;
            if (!isSlotFree(slot)) {
                angles[i] = Math.toRadians(readSlot(slot));
            }
        }
        return angles;
    }

    public void setDispersionAngle(int archonIndex, Double angle) throws GameActionException {
        int slot = SEGMENT_DISPERSION_ANGLES.start + archonIndex;
        if (angle == null) {
            clearSlot(slot);
        } else {
            writeSlot(slot, (int) Math.toDegrees(angle));
        }
    }

    private boolean isSlotFree(int index) throws GameActionException {
        return rc.readSharedArray(index * 2) == 0;
    }

    private void clearSlot(int index) throws GameActionException {
        rc.writeSharedArray(index * 2, 0);
    }

    private int readSlot(int index) throws GameActionException {
        int a = rc.readSharedArray(index * 2) - 1;
        int b = rc.readSharedArray(index * 2 + 1) - 1;
        return a | (b << 16);
    }

    private void writeSlot(int index, int value) throws GameActionException  {
        rc.writeSharedArray(index * 2, (value & 0xFFFF) + 1);
        rc.writeSharedArray(index * 2 + 1, ((value >>> 16) & 0xFFFF) + 1);
    }
}
