package uk.ac.soton.comp1206.utils;

/**
 * A simple class to represent a 2D integer vector with x and y components.
 */
public class Vector2 implements Comparable<Vector2> {

    /**
     * The x component of the vector
     */
    public int x;

    /**
     * The y component of the vector
     */
    public int y;

    /**
     * Create a new vector with x and y components
     *
     * @param x the x component
     * @param y the y component
     */
    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Shorthand for {@code new Vector2(0, 0)}
     *
     * @return a new vector with x and y components of 0
     */
    public static Vector2 zero() {
        return new Vector2(0, 0);
    }

    /**
     * Shorthand for {@code new Vector2(1, 1)}
     *
     * @return a new vector with x and y components of 1
     */
    public static Vector2 identity() {
        return new Vector2(1, 1);
    }

    /**
     * Shorthand for {@code new Vector2(0, -1)}
     *
     * @return a unit vector pointing upwards
     */
    public static Vector2 up() {
        return new Vector2(0, -1);
    }

    /**
     * Shorthand for {@code new Vector2(0, 1)}
     *
     * @return a unit vector pointing downwards
     */
    public static Vector2 down() {
        return new Vector2(0, 1);
    }

    /**
     * Shorthand for {@code new Vector2(-1, 0)}
     *
     * @return a unit vector pointing left
     */
    public static Vector2 left() {
        return new Vector2(-1, 0);
    }

    /**
     * Shorthand for {@code new Vector2(1, 0)}
     *
     * @return a unit vector pointing right
     */
    public static Vector2 right() {
        return new Vector2(1, 0);
    }

    /**
     * Shorthand for {@code new Vector2(value, value)}
     *
     * @param value the value to use for both components
     * @return a new vector with x and y components of value
     */
    public static Vector2 uniform(int value) {
        return new Vector2(value, value);
    }

    /**
     * Check if this vector is equal to another {@link Vector2}
     *
     * @param other the other {@link Vector2}
     * @return {@code true} if the vectors are equal
     */
    public boolean equals(Vector2 other) {
        return this.x == other.x && this.y == other.y;
    }

    /**
     * Clamp this vector to be within the given bounding box
     *
     * @param bottomLeft the bottom left corner of the bounding box
     * @param topRight   the top right corner of the bounding box
     * @return a new vector clamped to the bounding box
     */
    public Vector2 clamp(Vector2 bottomLeft, Vector2 topRight) {
        int newX = Math.max(bottomLeft.x, Math.min(x, topRight.x));
        int newY = Math.max(bottomLeft.y, Math.min(y, topRight.y));
        return new Vector2(newX, newY);
    }

    /**
     * Return a copy of this vector
     *
     * @return a new vector with the same x and y components
     */
    public Vector2 copy() {
        return new Vector2(x, y);
    }

    /**
     * Add another vector to this one
     *
     * @param other the other {@link Vector2}
     * @return a new vector representing the sum of the two vectors
     */
    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    @Override
    public int compareTo(Vector2 o) {
        return x == o.x ? Integer.compare(y, o.y) : Integer.compare(x, o.x);
    }

    /**
     * Return the hash code for this vector.
     * <p>
     * The hash code will be unique as long as the y component is in the range -50 to 50.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return x * 100 + y;
    }

    /**
     * Return a string representation of this vector in the form {@code (x, y)}
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Check if this vector is equal to another object
     *
     * @param other the other object
     * @return {@code true} if the other object is a {@link Vector2} and is equal to this one
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Vector2) {
            return this.equals((Vector2) other);
        }
        return false;
    }

}