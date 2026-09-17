package com.crawljax.interaction;

/**
 * Shifted node mirrored from retro-board {@code LayoutShiftSource}.
 */
public class LayoutShiftSource {

    private String node;
    private LayoutShiftRect previousRect;
    private LayoutShiftRect currentRect;

    public String getNode() {
        return node;
    }

    public LayoutShiftRect getPreviousRect() {
        return previousRect;
    }

    public LayoutShiftRect getCurrentRect() {
        return currentRect;
    }
}
