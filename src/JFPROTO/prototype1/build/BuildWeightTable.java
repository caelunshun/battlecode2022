package JFPROTO.prototype1.build;

/**
 * Determines what unit to build based on a weight
 * assigned to each BuildType.
 *
 * When a unit is built, its weight is set to 0.
 * The archon increments weights each round
 * based on which units require prioritization.
 */
public class BuildWeightTable {
    private int[] weights;

    public BuildWeightTable(int[] weights) {
        this.weights = weights;
    }

    public BuildWeightTable() {
        this.weights = new int[7];
    }

    public void addWeight(BuildType type, int amount) {
        weights[type.ordinal()] += amount;
    }

    public BuildType getHighestWeight() {
        int highest = -1;
        for (int i = 0; i < weights.length; i++) {
            if (highest == -1 || weights[i] > weights[highest]) {
                highest = i;
            }
        }
        return BuildType.values()[highest];
    }

    public void clearWeight(BuildType type) {
        weights[type.ordinal()] = 0;
    }

    public int[] getWeights() {
        return weights;
    }
}
