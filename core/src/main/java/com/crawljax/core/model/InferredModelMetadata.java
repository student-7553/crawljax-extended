package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * Provenance of an {@link InferredModel} (algorithm, k-tail parameter, and trace size).
 */
public final class InferredModelMetadata {

    private final String algorithm;
    private final int k;
    private final int traceCount;
    private final int interactionCount;

    public InferredModelMetadata(String algorithm, int k, int traceCount, int interactionCount) {
        this.algorithm = algorithm;
        this.k = k;
        this.traceCount = traceCount;
        this.interactionCount = interactionCount;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getK() {
        return k;
    }

    public int getTraceCount() {
        return traceCount;
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, k, traceCount, interactionCount);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InferredModelMetadata)) {
            return false;
        }
        InferredModelMetadata that = (InferredModelMetadata) object;
        return k == that.k
                && traceCount == that.traceCount
                && interactionCount == that.interactionCount
                && Objects.equals(algorithm, that.algorithm);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("algorithm", algorithm)
                .add("k", k)
                .add("traceCount", traceCount)
                .add("interactionCount", interactionCount)
                .toString();
    }
}
