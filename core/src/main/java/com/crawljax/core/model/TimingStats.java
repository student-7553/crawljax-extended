package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * Interaction timing breakdown recorded on an {@link InferredTransition}.
 */
public final class TimingStats {

    private final TimingMetric value;
    private final TimingMetric inputDelay;
    private final TimingMetric processingDuration;
    private final TimingMetric presentationDelay;
    private final TimingMetric totalScriptDuration;
    private final TimingMetric totalStyleAndLayoutDuration;
    private final TimingMetric totalPaintDuration;
    private final TimingMetric totalUnattributedDuration;

    public TimingStats(
            TimingMetric value,
            TimingMetric inputDelay,
            TimingMetric processingDuration,
            TimingMetric presentationDelay,
            TimingMetric totalScriptDuration,
            TimingMetric totalStyleAndLayoutDuration,
            TimingMetric totalPaintDuration,
            TimingMetric totalUnattributedDuration) {
        this.value = value;
        this.inputDelay = inputDelay;
        this.processingDuration = processingDuration;
        this.presentationDelay = presentationDelay;
        this.totalScriptDuration = totalScriptDuration;
        this.totalStyleAndLayoutDuration = totalStyleAndLayoutDuration;
        this.totalPaintDuration = totalPaintDuration;
        this.totalUnattributedDuration = totalUnattributedDuration;
    }

    public TimingMetric getValue() {
        return value;
    }

    public TimingMetric getInputDelay() {
        return inputDelay;
    }

    public TimingMetric getProcessingDuration() {
        return processingDuration;
    }

    public TimingMetric getPresentationDelay() {
        return presentationDelay;
    }

    public TimingMetric getTotalScriptDuration() {
        return totalScriptDuration;
    }

    public TimingMetric getTotalStyleAndLayoutDuration() {
        return totalStyleAndLayoutDuration;
    }

    public TimingMetric getTotalPaintDuration() {
        return totalPaintDuration;
    }

    public TimingMetric getTotalUnattributedDuration() {
        return totalUnattributedDuration;
    }

    /**
     * Mean time from input to presentation, useful as a wait after firing this action.
     */
    public long suggestedWaitMillis() {
        if (value == null) {
            return 0;
        }
        return Math.max(0, Math.round(value.getMean()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                value,
                inputDelay,
                processingDuration,
                presentationDelay,
                totalScriptDuration,
                totalStyleAndLayoutDuration,
                totalPaintDuration,
                totalUnattributedDuration);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TimingStats)) {
            return false;
        }
        TimingStats that = (TimingStats) object;
        return Objects.equals(value, that.value)
                && Objects.equals(inputDelay, that.inputDelay)
                && Objects.equals(processingDuration, that.processingDuration)
                && Objects.equals(presentationDelay, that.presentationDelay)
                && Objects.equals(totalScriptDuration, that.totalScriptDuration)
                && Objects.equals(totalStyleAndLayoutDuration, that.totalStyleAndLayoutDuration)
                && Objects.equals(totalPaintDuration, that.totalPaintDuration)
                && Objects.equals(totalUnattributedDuration, that.totalUnattributedDuration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .toString();
    }
}
