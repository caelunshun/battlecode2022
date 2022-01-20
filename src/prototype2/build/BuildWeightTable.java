package prototype2.build;

/**
 * Determines what unit to build based on a weight
 * assigned to each ordinal.
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

    public BuildWeightTable(int numWeights) {
        this.weights = new int[numWeights];
    }

    public void addWeight(int ordinal, int amount) {
        weights[ordinal] += amount;
    }

    public int getHighestWeight() {
        int highest = -1;
        for (int i = 0; i < weights.length; i++) {
            if (highest == -1 || weights[i] > weights[highest]) {
                highest = i;
            }
        }
        return highest;
    }

    public void clearWeight(int ordinal) {
        weights[ordinal] = 0;
    }

    public int[] getWeights() {
        return weights;
    }
}
