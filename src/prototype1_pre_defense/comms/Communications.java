package prototype1_pre_defense.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype1_pre_defense.generic.SymmetryType;

import java.util.ArrayList;
import java.util.List;

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
 * - next 8 slots: positions of known lead clusters and whether they've been claimed.
 */
public final class Communications {
    private RobotController rc;

    private static final Range SEGMENT_FRIENDLY_ARCHONS = new Range(0, 4);
    private static final Range SEGMENT_ENEMY_ARCHONS = new Range(4, 8);
    private static final Range SEGMENT_LEAD_CLUSTERS = new Range(8, 16);

    public Communications(RobotController rc) {
        this.rc = rc;
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

    public List<LeadCluster> readLeadClusters() throws GameActionException {
        List<LeadCluster> res = new ArrayList<>(4);
        for (int i = SEGMENT_LEAD_CLUSTERS.start; i < SEGMENT_LEAD_CLUSTERS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                res.add(new LeadCluster(dec.readMapLocation(), dec.read(4)));
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

    public void addLeadCluster(LeadCluster cluster) throws GameActionException {
        int slot = getFreeSlot(SEGMENT_LEAD_CLUSTERS);
        if (slot == -1) return;
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(cluster.loc);
        enc.write(cluster.numClaims, 4);
        writeSlot(slot, enc.finish());
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

    private int getFreeSlot(Range segment) throws GameActionException {
        for (int i = segment.start; i < segment.end; i++) {
            if (isSlotFree(i)) {
                return i;
            }
        }
        return -1;
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
