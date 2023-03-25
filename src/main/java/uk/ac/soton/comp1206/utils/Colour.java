package uk.ac.soton.comp1206.utils;


import javafx.scene.paint.Color;

/**
 * Contains methods for colouring and styling strings, using ANSI codes.<br> If multiple styles are
 * wanted on a single string, calls to {@code colour()} can be nested.
 * <p>
 * <p/> Available colors:
 * <ul>
 *     <li>RED</li>
 *     <li>GREEN</li>
 *     <li>YELLOW</li>
 *     <li>ORANGE</li>
 *     <li>PURPLE</li>
 *     <li>CYAN</li>
 *     <li>DEFAULT (depending on your theme)</li>
 *     <li>RED_BG (black text on a red background)</li>
 *     <li>GREEN_BG (")</li>
 *     <li>YELLOW_BG</li>
 *     <li>ORANGE_BG</li>
 *     <li>PURPLE_BG</li>
 *     <li>CYAN_BG</li>
 * </ul>
 * Available rendering modes:
 * <ul>
 *     <li><b>BOLD</b></li>
 *     <li><i>ITALIC</i></li>
 *     <li><u>UNDERLINE</u></li>
 *     <li><s>STRIKETHROUGH</s></li>
 * </ul>
 *
 * @author Adam Robson
 * @version 1.0
 */
public class Colour {

    /**
     * The available colours for text.
     */
    public enum TextColour {
        RED, GREEN, YELLOW, ORANGE, PURPLE, CYAN, DEFAULT, RED_BG, GREEN_BG, YELLOW_BG, ORANGE_BG, PURPLE_BG, CYAN_BG,
        ;
        private static final String[] values = {"\033[31m", "\033[32m", "\033[33m", "\033[34m",
                "\033[35m", "\033[36m", "\033[0m", "\033[41;30m", "\033[42;30m", "\033[43;30m",
                "\033[44;30m", "\033[45;30m", "\033[46;30m",};

        public String code() {
            return values[this.ordinal()];
        }
    }


    /**
     * The available styles for text.
     */
    public enum TextMode {
        RESET, BOLD, ITALIC, UNDERLINE, STRIKETHROUGH,
        ;
        private static final String[] values = {"\033[0m", "\033[1m", "\033[3m", "\033[4m",
                "\033[9m",};

        public String code() {
            return values[this.ordinal()];
        }
    }

    /**
     * Adds ANSI colour codes to render a string in the specified colour.
     *
     * @param message the string to colour
     * @param colour  the colour to use
     * @return the coloured string
     */
    public static String colour(String message, TextColour colour) {
        return colour.code() + message + "\033[0m";
    }


    /**
     * Adds ANSI colour codes to render a string in the specified colour, and with the specified
     * rendering mode.
     *
     * @param message the string to colour
     * @param colour  the colour to use
     * @param mode    the rendering mode to use
     * @return the coloured string
     */
    public static String colour(String message, TextColour colour, TextMode mode) {
        return colour.code() + mode.code() + message + "\033[0m";
    }

    /**
     * Adds ANSI colour codes to render a string in the specified colour, and with the two specified
     * rendering modes combined.
     *
     * @param message the string to colour
     * @param colour  the colour to use
     * @param mode1   the first rendering mode to use
     * @param mode2   the second rendering mode to use
     * @return the coloured string
     */
    public static String colour(String message, TextColour colour, TextMode mode1, TextMode mode2) {
        return colour.code() + mode1.code() + mode2.code() + message + "\033[0m";
    }

    /**
     * Adds ANSI codes to render a string in the specified rendering mode.
     *
     * @param message the string to style
     * @param mode    the rendering mode to use
     * @return the styled string
     */
    public static String mode(String message, TextMode mode) {
        return mode.code() + message + "\033[0m";
    }


    /**
     * a utility method to convert RGB values into a rgb hex string, prefixed with a #<br> e.g.
     * {@code rgbToHex(255, 0, 0)} returns {@code "#ff0000"}
     *
     * @param r red, between 0 and 255
     * @param g green, between 0 and 255
     * @param b blue, between 0 and 255
     * @return the resulting hex string
     */
    public static String rgb(int r, int g, int b) {
        return String.format("#%02x%02x%02x", r, g, b);
    }


    /**
     * a utility method to convert HSV values into a rgb hex string, prefixed with a #<br>
     *
     * @param h Hue, between 0 and 1
     * @param s Saturation, between 0 and 1
     * @param v Value, between 0 and 1
     * @return the resulting hex string
     */
    public static String hsv(float h, float s, float v) {
        var c = Color.hsb(h, s, v);
        return Colour.rgb(
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255)
        );
    }


    /**
     * Testing the class
     */
    public static void main(String[] args) {
        System.out.println(colour("red", TextColour.RED, TextMode.BOLD));
        System.out.println(colour("green", TextColour.GREEN, TextMode.ITALIC));
        System.out.println(
                colour("orange", TextColour.ORANGE, TextMode.STRIKETHROUGH, TextMode.BOLD));
        System.out.println(colour("purple", TextColour.PURPLE, TextMode.UNDERLINE));
        System.out.println(colour("white", TextColour.DEFAULT));
        System.out.println(colour("reddish", TextColour.RED_BG, TextMode.BOLD));
        System.out.println(colour("greenish", TextColour.GREEN_BG, TextMode.ITALIC));
        System.out.println(colour("yellowish", TextColour.YELLOW_BG, TextMode.UNDERLINE));
        System.out.println(colour("orangeish", TextColour.ORANGE_BG, TextMode.STRIKETHROUGH));
        System.out.println(
                colour("purpleish", TextColour.PURPLE_BG, TextMode.BOLD, TextMode.ITALIC));
        System.out.println(
                colour("cyanish", TextColour.CYAN_BG, TextMode.BOLD, TextMode.UNDERLINE));

        System.out.println(fatal("fatal"));
        System.out.println(error("error"));
        System.out.println(warn("warn"));
        System.out.println(red("red"));
        System.out.println(green("green"));
        System.out.println(yellow("yellow"));
        System.out.println(orange("orange"));
        System.out.println(cyan("cyan"));
        System.out.println(purple("purple"));

    }


    /**
     * Colours the message in red using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String red(String message) {
        return colour(message, TextColour.RED);
    }

    /**
     * Colours the message in green using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String green(String message) {
        return colour(message, TextColour.GREEN);
    }

    /**
     * Colours the message in yellow using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String yellow(String message) {
        return colour(message, TextColour.YELLOW);
    }

    /**
     * Colours the message in blue using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String orange(String message) {
        return colour(message, TextColour.ORANGE);
    }

    /**
     * Colours the message in purple using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String purple(String message) {
        return colour(message, TextColour.PURPLE);
    }

    /**
     * Colours the message in cyan using ANSI colour codes.
     *
     * @param message the message to colour
     * @return the coloured message
     */
    public static String cyan(String message) {
        return colour(message, TextColour.CYAN);
    }

    /**
     * Renders the message in bold.
     * @param message the message to style
     * @return the styled message
     */
    public static String bold(String message) {
        return mode(message, TextMode.BOLD);
    }

    /**
     * Renders the message in italic.
     * @param message the message to style
     * @return the styled message
     */
    public static String italic(String message) {
        return mode(message, TextMode.ITALIC);
    }

    /**
     * Renders the message with an underline.
     * @param message the message to style
     * @return the styled message
     */
    public static String underline(String message) {
        return mode(message, TextMode.UNDERLINE);
    }

    public static String fatal(String message) {
        return colour(message, TextColour.RED_BG, TextMode.BOLD, TextMode.UNDERLINE);
    }

    public static String error(String message) {
        return red(bold(message));
    }

    public static String warn(String message) {
        return yellow(bold(message));
    }
}