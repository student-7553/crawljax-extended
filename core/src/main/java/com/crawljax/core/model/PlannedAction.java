package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * The next model transition to fire, optionally after resetting the browser to the start URL.
 */
public final class PlannedAction {

    private final InferredTransition transition;
    private final boolean resetToIndex;

    private PlannedAction(InferredTransition transition, boolean resetToIndex) {
        this.transition = Preconditions.checkNotNull(transition, "Transition must not be null");
        this.resetToIndex = resetToIndex;
    }

    public static PlannedAction fire(InferredTransition transition) {
        return new PlannedAction(transition, false);
    }

    public static PlannedAction resetAndFire(InferredTransition transition) {
        return new PlannedAction(transition, true);
    }

    public InferredTransition getTransition() {
        return transition;
    }

    public boolean isResetToIndex() {
        return resetToIndex;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("resetToIndex", resetToIndex)
                .add("transition", transition)
                .toString();
    }
}
