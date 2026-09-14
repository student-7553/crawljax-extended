package com.crawljax.core.model;

import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;

/**
 * The concrete UI action that labels an {@link InferredTransition}.
 *
 * <p>{@code target} is a CSS selector recorded from the user trace.
 */
public final class InferredAction {

    private final ActionType type;
    private final String target;

    public InferredAction(ActionType type, String target) {
        this.type = Preconditions.checkNotNull(type, "Action type must not be null");
        this.target = Preconditions.checkNotNull(target, "Action target must not be null");
    }

    public ActionType getType() {
        return type;
    }

    /**
     * CSS selector of the element the user interacted with.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Identification Crawljax can later use to locate the element in the live DOM.
     */
    public Identification toIdentification() {
        return new Identification(How.css, target);
    }

    public boolean isPointer() {
        return type == ActionType.POINTER;
    }

    public boolean isKeyboard() {
        return type == ActionType.KEYBOARD;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InferredAction)) {
            return false;
        }
        InferredAction that = (InferredAction) object;
        return type == that.type && target.equals(that.target);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("target", target)
                .toString();
    }
}
