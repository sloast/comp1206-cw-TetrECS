package uk.ac.soton.comp1206.utils;

/**
 * A simple class to represent a 2D integer vector.
 */

public class Vector2Int implements Comparable<Vector2Int> {
    public int x;
    public int y;
    public Vector2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Vector2Int other) {
        return this.x == other.x && this.y == other.y;
    }

    public Vector2Int plus(Vector2Int other) {
        return new Vector2Int(this.x + other.x, this.y + other.y);
    }


    public int compareTo(Vector2Int o) {
        return x == o.x ? Integer.compare(y, o.y) : Integer.compare(x, o.x);
    }

    @Override
    public int hashCode() {
        return x * 100 + y;
    }
}