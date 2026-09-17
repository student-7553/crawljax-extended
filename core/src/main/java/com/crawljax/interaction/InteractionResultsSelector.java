package com.crawljax.interaction;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;

/**
 * Selects payloads that appeared after the previous crawler action using {@code createdAt}, keeping
 * only the newest entry when several are new.
 */
final class InteractionResultsSelector {

    private InteractionResultsSelector() {}

    static Optional<InteractionPayload> selectNewestInteraction(
            List<InteractionPayload> interactions, long watermarkCreatedAt) {
        return selectNewest(interactions, watermarkCreatedAt, InteractionPayload::getCreatedAt);
    }

    static Optional<LayoutShiftPayload> selectNewestLayoutShift(
            List<LayoutShiftPayload> layoutShifts, long watermarkCreatedAt) {
        return selectNewest(layoutShifts, watermarkCreatedAt, LayoutShiftPayload::getCreatedAt);
    }

    static long maxCreatedAt(
            List<InteractionPayload> interactions, List<LayoutShiftPayload> layoutShifts, long fallback) {
        long max = fallback;
        if (interactions != null) {
            for (InteractionPayload interaction : interactions) {
                if (interaction != null) {
                    max = Math.max(max, interaction.getCreatedAt());
                }
            }
        }
        if (layoutShifts != null) {
            for (LayoutShiftPayload layoutShift : layoutShifts) {
                if (layoutShift != null) {
                    max = Math.max(max, layoutShift.getCreatedAt());
                }
            }
        }
        return max;
    }

    private static <T> Optional<T> selectNewest(List<T> entries, long watermarkCreatedAt, ToLongFunction<T> createdAt) {
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return entries.stream()
                .filter(entry -> entry != null && createdAt.applyAsLong(entry) > watermarkCreatedAt)
                .max(Comparator.comparingLong(createdAt));
    }
}
