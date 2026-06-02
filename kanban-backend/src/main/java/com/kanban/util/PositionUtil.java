package com.kanban.util;

/**
 * Utility for calculating card position using the float midpoint algorithm.
 * This avoids O(n) updates on every drag-and-drop by using double-precision
 * positions and only recalculating the moved card's position.
 */
public class PositionUtil {

    /** Base position increment for new items */
    public static final double BASE_POSITION = 65536.0;

    /** Threshold below which positions are considered colliding */
    public static final double COLLISION_THRESHOLD = 1e-12;

    /**
     * Calculate position when inserting a card between two existing cards.
     *
     * @param prevPosition position of the card above (null if inserting at top)
     * @param nextPosition position of the card below (null if inserting at bottom)
     * @return the new position value
     */
    public static double calculatePosition(Double prevPosition, Double nextPosition) {
        if (prevPosition == null && nextPosition == null) {
            return BASE_POSITION;  // First card in empty list
        }
        if (prevPosition == null) {
            return nextPosition / 2.0;  // Insert at top
        }
        if (nextPosition == null) {
            return prevPosition + BASE_POSITION;  // Insert at bottom
        }
        return (prevPosition + nextPosition) / 2.0;  // Insert between two cards
    }

    /**
     * Check if two position values are too close (within collision threshold).
     */
    public static boolean isCollision(double pos1, double pos2) {
        return Math.abs(pos1 - pos2) < COLLISION_THRESHOLD;
    }
}
