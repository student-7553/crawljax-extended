package com.crawljax.interaction;

/**
 * Interaction metrics mirrored from retro-board {@code InteractionPayload}.
 */
public class InteractionPayload {

    private String name;
    private long createdAt;
    private Object interactionId;
    private double value;
    private String interactionTarget;
    private String interactionType;
    private double interactionTime;
    private double inputDelay;
    private double processingDuration;
    private double presentationDelay;
    private double nextPaintTime;
    private String typedInput;
    private TypedKeyInput typedKey;
    private InteractionAttribution attribution;

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Object getInteractionId() {
        return interactionId;
    }

    public double getValue() {
        return value;
    }

    public String getInteractionTarget() {
        return interactionTarget;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public double getInteractionTime() {
        return interactionTime;
    }

    public double getInputDelay() {
        return inputDelay;
    }

    public double getProcessingDuration() {
        return processingDuration;
    }

    public double getPresentationDelay() {
        return presentationDelay;
    }

    public double getNextPaintTime() {
        return nextPaintTime;
    }

    public String getTypedInput() {
        return typedInput;
    }

    public TypedKeyInput getTypedKey() {
        return typedKey;
    }

    public InteractionAttribution getAttribution() {
        return attribution;
    }
}
