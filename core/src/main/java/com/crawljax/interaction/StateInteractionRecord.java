package com.crawljax.interaction;

/**
 * Interaction / layout-shift data attributed to a single Crawljax state.
 */
public class StateInteractionRecord {

    private final String stateName;
    private final int stateId;
    private final InteractionPayload interaction;
    private final LayoutShiftPayload layoutShift;

    public StateInteractionRecord(
            String stateName, int stateId, InteractionPayload interaction, LayoutShiftPayload layoutShift) {
        this.stateName = stateName;
        this.stateId = stateId;
        this.interaction = interaction;
        this.layoutShift = layoutShift;
    }

    public String getStateName() {
        return stateName;
    }

    public int getStateId() {
        return stateId;
    }

    public InteractionPayload getInteraction() {
        return interaction;
    }

    public LayoutShiftPayload getLayoutShift() {
        return layoutShift;
    }
}
