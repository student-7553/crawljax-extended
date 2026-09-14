package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * Aggregated timing values (milliseconds) for one metric on a transition.
 */
public final class TimingMetric {

    private final int count;
    private final double min;
    private final double max;
    private final double mean;
    private final double sum;

    public TimingMetric(int count, double min, double max, double mean, double sum) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.mean = mean;
        this.sum = sum;
    }

    public int getCount() {
        return count;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }

    public double getSum() {
        return sum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, min, max, mean, sum);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TimingMetric)) {
            return false;
        }
        TimingMetric that = (TimingMetric) object;
        return count == that.count
                && Double.compare(that.min, min) == 0
                && Double.compare(that.max, max) == 0
                && Double.compare(that.mean, mean) == 0
                && Double.compare(that.sum, sum) == 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("count", count)
                .add("mean", mean)
                .toString();
    }
}
