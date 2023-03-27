package uk.ac.soton.comp1206.utils;

/**
 * A simple class to represent a 2D integer vector.
 */

public class Vector2 implements Comparable<Vector2> {
    public int x;
    public int y;
    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 zero() {return new Vector2(0, 0);}
    public static Vector2 identity() {return new Vector2(1, 1);}
    public static Vector2 up() {return new Vector2(0, -1);
}
    public static Vector2 down() {return new Vector2(0, 1);
}
    public static Vector2 left() {return new Vector2(-1,0);}
    public static Vector2 right() {return new Vector2(1,0);}

    public boolean equals(Vector2 other) {
        return this.x == other.x && this.y == other.y;
    }

    public Vector2 clamp(Vector2 bottomLeft, Vector2 topRight) {
        int newX = Math.max(bottomLeft.x, Math.min(x, topRight.x));
        int newY = Math.max(bottomLeft.y, Math.min(y, topRight.y));
        return new Vector2(newX, newY);
    }

    public Vector2 copy() {
        return new Vector2(x, y);
    }

    public static Vector2 uniform(int value) {
        return new Vector2(value, value);
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }


    public int compareTo(Vector2 o) {
        return x == o.x ? Integer.compare(y, o.y) : Integer.compare(x, o.x);
    }

    @Override
    public int hashCode() {
        return x * 100 + y;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Vector2) {
            return this.equals((Vector2) other);
        }
        return false;
    }

}