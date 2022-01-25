package prototype2.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype2.RobotCategory;
import prototype2.Strategy;
import prototype2.build.GoldBuild;
import prototype2.build.LeadBuild;
import prototype2.SymmetryType;

import java.util.ArrayList;
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
 * The meaning of a slot depends on its position in the array.
 */
public final class Communications {
    private final RobotController rc;

    private static final Range SEGMENT_FRIENDLY_ARCHONS = new Range(0, 4);
    private static final Range SEGMENT_ENEMY_ARCHONS = new Range(4, 8);
    private static final Range SEGMENT_CRIES_FOR_HELP = new Range(8, 12);
    private static final Range SEGMENT_LEAD_LOCATIONS = new Range(12, 14);
    private static final int LEAD_BUILD = 14;
    private static final int GOLD_BUILD = 15;
    private static final Range SEGMENT_DISPERSION_ANGLES = new Range(16, 20);
    private static final int LEAD_COUNTER = 20;
    private static final Range ENEMY_SPOTTED_LOCATIONS = new Range(21, 23);
    private static final Range ROBOT_COUNTS = new Range(23, 25);
    private static final int STRATEGY = 25;
    private static final int COLLECT_GOLD = 26;
    private static final int WATCHTOWER_BUILD_LOCATION = 27;

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
    public List<Archon> readFriendlyArchons() throws GameActionException {
        List<Archon> res = new ArrayList<>(4);
        for (int i = SEGMENT_FRIENDLY_ARCHONS.start; i < SEGMENT_FRIENDLY_ARCHONS.end; i++) {
            if (!isSlotFree(i)) {
                BitDecoder dec = new BitDecoder(readSlot(i));
                res.add(new Archon(dec.readMapLocation(), dec.readBool(), dec.read(6), dec.readBool(), dec.readBool()));
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

    public int addFriendlyArchon(MapLocation loc, int numLeadLocations) throws GameActionException {
        int slot = getFreeSlot(SEGMENT_FRIENDLY_ARCHONS);
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(loc);
        enc.writeBoolean(false);
        enc.write(numLeadLocations, 6);
        enc.writeBoolean(false);
        enc.writeBoolean(true);
        writeSlot(slot, enc.finish());
        return slot - SEGMENT_FRIENDLY_ARCHONS.start;
    }

    public void updateFriendlyArchon(int index, Archon archon) throws GameActionException {
        int slot = SEGMENT_FRIENDLY_ARCHONS.start + index;
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(archon.loc);
        enc.writeBoolean(archon.isDestroyed);
        enc.write(archon.numLeadLocations, 6);
        enc.writeBoolean(archon.isLead);
        enc.writeBoolean(archon.inPregame);
        writeSlot(slot, enc.finish());
    }

    public void addEnemyArchon(MapLocation loc) throws GameActionException {
        int slot = getFreeSlot(SEGMENT_ENEMY_ARCHONS);
        if (slot == -1) return;
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
        for (int i = ENEMY_SPOTTED_LOCATIONS.start; i < ENEMY_SPOTTED_LOCATIONS.end; i++) {
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
                if (rc.getRoundNum() - locs[i].roundNumber > 3) {
                    clearSlot(ENEMY_SPOTTED_LOCATIONS.start + i);
                    return false;
                } else if (locs[i].loc.distanceSquaredTo(enemyLoc) <= 9) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public SymmetryType getSymmetryType() throws GameActionException {
        if (rc.readSharedArray(63) == 0) return null;
        return SymmetryType.values()[rc.readSharedArray(63) - 1];
    }

    public void setSymmetryType(SymmetryType type) throws GameActionException {
        rc.writeSharedArray(63, type.ordinal() + 1);
    }

    public MapLocation[] getLeadLocations() throws GameActionException {
        MapLocation[] res = new MapLocation[2];
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
            index = new Random(33).nextInt(2);
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

    public int getNumRobots(RobotCategory category) throws GameActionException {
        return readSlotEightBits(category.ordinal());
    }

    public void incrementNumRobots(RobotCategory category) throws GameActionException {
        int count = readSlotEightBits(category.ordinal());
        setNumRobots(category, count + 1);
    }

    public void clearRobotCounts() throws GameActionException {
        for (RobotCategory cat : RobotCategory.values()) {
            setNumRobots(cat, 0);
        }
    }

    private void setNumRobots(RobotCategory category, int amount) throws GameActionException {
        writeSlotEightBits(category.ordinal(), amount);

        if (category == RobotCategory.BUILDER) {
        }
    }

    public LeadBuild getLeadBuild() throws GameActionException {
        if (isSlotFree(LEAD_BUILD)) return null;
        return LeadBuild.values()[readSlot(LEAD_BUILD)];
    }

    public GoldBuild getGoldBuild() throws GameActionException {
        if (isSlotFree(GOLD_BUILD)) return null;
        return GoldBuild.values()[readSlot(GOLD_BUILD)];
    }

    public void setLeadBuild(LeadBuild build) throws GameActionException {
        if (build == null) {
            clearSlot(LEAD_BUILD);
        } else {
            writeSlot(LEAD_BUILD, build.ordinal());
        }
    }

    public void setGoldBuild(GoldBuild build) throws GameActionException {
        if (build == null) {
            clearSlot(GOLD_BUILD);
        } else {
            writeSlot(GOLD_BUILD, build.ordinal());
        }
    }

    public int getBuildIndex() throws GameActionException {
        return rc.readSharedArray(63);
    }

    public void setBuildIndex(int index) throws GameActionException {
        rc.writeSharedArray(63, index);
    }

    public Strategy getStrategy() throws GameActionException {
        if (isSlotFree(STRATEGY)) return null;
        return Strategy.values()[readSlot(STRATEGY)];
    }

    public void setStrategy(Strategy strat) throws GameActionException {
        writeSlot(STRATEGY, strat.ordinal());
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

    public void writeSlotEightBits(int slot, int amount) throws GameActionException{
        if(amount > 255){
            throw new RuntimeException("more than 255 of robot type");
        }
        if(isSlotFree(ROBOT_COUNTS.start)){
            writeSlot(ROBOT_COUNTS.start, 0);
        }
        if(isSlotFree(ROBOT_COUNTS.start + 1)){
            writeSlot(ROBOT_COUNTS.start+1, 0);
        }
        switch(slot){
            case 0:
                writeSlot(ROBOT_COUNTS.start, ((0xFFFFFF00 & (readSlot(ROBOT_COUNTS.start))) | amount   ));
                break;
            case 1:
                writeSlot(ROBOT_COUNTS.start, ((0xFFFF00FF & (readSlot(ROBOT_COUNTS.start))) ) | (amount<<8)   );
                break;
            case 2:
                writeSlot(ROBOT_COUNTS.start, ((0xFF00FFFF & (readSlot(ROBOT_COUNTS.start))) | (amount<<16)   ));
                break;
            case 3:
                writeSlot(ROBOT_COUNTS.start, ((0x00FFFFFF & (readSlot(ROBOT_COUNTS.start))) | (amount<<24)   ));
                break;
            case 4:
                writeSlot(ROBOT_COUNTS.start + 1, ((0xFFFFFF00 & (readSlot(ROBOT_COUNTS.start + 1))) | amount   ));
                break;
            case 5:
                writeSlot(ROBOT_COUNTS.start + 1, ((0xFFFF00FF & (readSlot(ROBOT_COUNTS.start + 1))) | (amount<<8)   ));
                break;
            case 6:
                writeSlot(ROBOT_COUNTS.start + 1, ((0xFF00FFFF & (readSlot(ROBOT_COUNTS.start + 1))) | (amount<<16)   ));
                break;
            case 7:
                writeSlot(ROBOT_COUNTS.start + 1, ((0x00FFFFFF & (readSlot(ROBOT_COUNTS.start + 1))) | (amount<<24)   ));
                break;

        }
    }
    public int readSlotEightBits(int slot) throws GameActionException{

        if(isSlotFree(ROBOT_COUNTS.start)){
            writeSlot(ROBOT_COUNTS.start, 0);
        }
        if(isSlotFree(ROBOT_COUNTS.start + 1)){
            writeSlot(ROBOT_COUNTS.start+1, 0);
        }
        switch(slot){
            case 0:
                return (0xFF & readSlot(ROBOT_COUNTS.start));
            case 1:
                return (0xFF00 & readSlot(ROBOT_COUNTS.start))>>>8;
            case 2:
                return (0xFF0000 & readSlot(ROBOT_COUNTS.start))>>>16;
            case 3:
                return (0xFF000000 & readSlot(ROBOT_COUNTS.start))>>>24;
            case 4:
                return (0xFF & readSlot(ROBOT_COUNTS.start + 1));
            case 5:
                return (0xFF00 & readSlot(ROBOT_COUNTS.start + 1))>>>8;
            case 6:
                return (0xFF0000 & readSlot(ROBOT_COUNTS.start + 1))>>>16;
            case 7:
                return (0xFF000000 & readSlot(ROBOT_COUNTS.start + 1))>>>24;
        }
        throw new RuntimeException("lol slot is 0-7");
    }
    public void makeGold() throws GameActionException{
        writeSlot(COLLECT_GOLD, 1);
    }
    public void doNotMakeGold() throws GameActionException{
        writeSlot(COLLECT_GOLD, 0);
    }
    public boolean canMakeGold() throws GameActionException{
        if(isSlotFree(COLLECT_GOLD)){
            writeSlot(COLLECT_GOLD, 1);
        }
        if(readSlot(COLLECT_GOLD) == 1){
            return true;
        }
        return false;
    }
    public void setWatchtowerBuildLocation(MapLocation loc) throws GameActionException{
        if(loc == null){
            resetWatchtowerBuildLocation();
            return;
        }
        BitEncoder enc = new BitEncoder();
        enc.writeMapLocation(loc);
        writeSlot(WATCHTOWER_BUILD_LOCATION, enc.finish());
    }
    public void resetWatchtowerBuildLocation() throws GameActionException{
        clearSlot(WATCHTOWER_BUILD_LOCATION);
    }
    public MapLocation getWatchtowerBuildLocation() throws GameActionException{
        if(isSlotFree(WATCHTOWER_BUILD_LOCATION)){
            return null;
        }
        BitDecoder dec = new BitDecoder(readSlot(WATCHTOWER_BUILD_LOCATION));
        return dec.readMapLocation();
    }
}
