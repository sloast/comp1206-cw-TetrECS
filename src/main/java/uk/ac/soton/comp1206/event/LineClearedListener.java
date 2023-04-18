package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.utils.Vector2;

public interface LineClearedListener {

    void onLineCleared(Set<Vector2> line);
}