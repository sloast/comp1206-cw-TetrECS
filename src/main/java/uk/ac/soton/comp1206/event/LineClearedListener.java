package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.utils.Vector2;

/**
 * Passes information about cleared blocks to the UI so animations can be played.
 */
public interface LineClearedListener {

    /**
     * Called when a line(s) is cleared
     *
     * @param line The set of blocks that were cleared
     */
    void onLineCleared(Set<Vector2> line);
}