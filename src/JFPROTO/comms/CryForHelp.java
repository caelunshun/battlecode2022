package JFPROTO.comms;

import battlecode.common.MapLocation;

/**
 * Sent by a soldier when it is outnumbered.
 */
public class CryForHelp {
    /**
     * Location of the nearest enemy to the soldier;
     */
    public final MapLocation enemyLoc;
    /**
     * Number of enemies within the soldier's vision radius.
     */
    public final int numEnemies;
    /**
     * The round number we made the cry on
     */
    public final int roundNumber;

    public CryForHelp(MapLocation enemyLoc, int numEnemies, int roundNumber) {
        this.enemyLoc = enemyLoc;
        this.numEnemies = numEnemies;
        this.roundNumber = roundNumber;
    }
}
