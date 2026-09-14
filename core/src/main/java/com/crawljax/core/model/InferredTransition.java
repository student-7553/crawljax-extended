package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;

/**
 * A labeled edge in an {@link InferredModel}: source state, target state, and the user action that
 * caused the move.
 */
public final class InferredTransition {

    private final String sourceId;
    private final String targetId;
    private final InferredAction action;
    private final int count;
    private final TimingStats timings;
    private final TransitionInputs inputs;

    public InferredTransition(
            String sourceId,
            String targetId,
            InferredAction action,
            int count,
            TimingStats timings,
            TransitionInputs inputs) {
        this.sourceId = Preconditions.checkNotNull(sourceId, "Transition source must not be null");
        this.targetId = Preconditions.checkNotNull(targetId, "Transition target must not be null");
        this.action = Preconditions.checkNotNull(action, "Transition action must not be null");
        this.count = count;
        this.timings = timings;
        this.inputs = inputs == null ? new TransitionInputs(null) : inputs;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public InferredAction getAction() {
        return action;
    }

    /**
     * How often this edge was observed in the traces used to infer the model.
     */
    public int getCount() {
        return count;
    }

    public TimingStats getTimings() {
        return timings;
    }

    public TransitionInputs getInputs() {
        return inputs;
    }

    public boolean isSelfLoop() {
        return sourceId.equals(targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, targetId, action, count);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InferredTransition)) {
            return false;
        }
        InferredTransition that = (InferredTransition) object;
        return count == that.count
                && sourceId.equals(that.sourceId)
                && targetId.equals(that.targetId)
                && action.equals(that.action)
                && Objects.equals(timings, that.timings)
                && Objects.equals(inputs, that.inputs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", sourceId)
                .add("target", targetId)
                .add("action", action)
                .add("count", count)
                .toString();
    }
}
