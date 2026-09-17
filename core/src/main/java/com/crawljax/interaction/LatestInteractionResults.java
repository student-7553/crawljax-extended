package com.crawljax.interaction;

import java.util.Collections;
import java.util.List;

/**
 * Snapshot returned by {@code window.getLatestInteractionResults()} in retro-board.
 */
public class LatestInteractionResults {

    private List<InteractionPayload> interactions = Collections.emptyList();
    private List<LayoutShiftPayload> layoutShifts = Collections.emptyList();

    public List<InteractionPayload> getInteractions() {
        return interactions == null ? Collections.emptyList() : interactions;
    }

    public List<LayoutShiftPayload> getLayoutShifts() {
        return layoutShifts == null ? Collections.emptyList() : layoutShifts;
    }
}
