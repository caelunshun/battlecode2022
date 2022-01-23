package OLDCODE.nav;

import battlecode.common.MapLocation;

import java.util.Objects;

/**
 * A line extrapolated from two endpoints.
 */
public class Line {
    private MapLocation endpointA;
    private MapLocation endpointB;

    public Line(MapLocation endpointA, MapLocation endpointB) {
        this.endpointA = endpointA;
        this.endpointB = endpointB;
        if (endpointA.x > endpointB.x) {
            this.endpointA = endpointB;
            this.endpointB = endpointA;
        }
    }

    public boolean isEndpoint(MapLocation loc) {
        return loc.equals(endpointA)
                || loc.equals(endpointB);
    }

    public double xIntersectionWithLine(Line other) {
        // special case: vertical line
        if (isVertical())
            return endpointA.x;
        else if (other.isVertical())
            return other.endpointA.x;

        return (other.getYIntercept() - getYIntercept()) / (getSlope() - other.getSlope());
    }

    public boolean isVertical() {
        return endpointA.x == endpointB.x;
    }

    public double getSlope() {
        return ((double) endpointB.y - endpointA.y) / (endpointB.x - endpointA.x);
    }

    public double getYIntercept() {
        return endpointA.y - getSlope() * endpointA.x;
    }

    public double evaluate(double x) {
        return x * getSlope() + getYIntercept();
    }

    public boolean intersectsTile(MapLocation tile) {
        Line bottom = new Line(tile, tile.translate(1, 0));
        Line top = new Line(tile.translate(0, 1), tile.translate(1, 1));
        Line left = new Line(tile, tile.translate(0, 1));
        Line right = new Line(tile.translate(1, 0), tile.translate(1, 1));

        return
                intersectsWithinTile(bottom, tile)
                        || intersectsWithinTile(top, tile)
                        || intersectsWithinTile(left, tile)
                        || intersectsWithinTile(right, tile);
    }

    private boolean intersectsWithinTile(Line line, MapLocation tile) {
        double x = this.xIntersectionWithLine(line);
        double y = this.evaluate(x);
        if (this.isVertical()) y = line.evaluate(x);

        double xOffset = x - tile.x;
        double yOffset = y - tile.y;
        return this.equals(line) || (xOffset >= 0 && xOffset <= 1 && yOffset >= 0 && yOffset <= 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return Objects.equals(endpointA, line.endpointA) && Objects.equals(endpointB, line.endpointB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointA, endpointB);
    }

    @Override
    public String toString() {
        return "Line{" +
                "endpointA=" + endpointA +
                ", endpointB=" + endpointB +
                '}';
    }
}