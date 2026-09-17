package com.crawljax.interaction;

/**
 * Attribution mirrored from retro-board {@code InteractionAttribution}.
 */
public class InteractionAttribution {

    private String loadState;
    private LongestScriptAttribution longestScript;
    private Double totalScriptDuration;
    private Double totalStyleAndLayoutDuration;
    private Double totalPaintDuration;
    private Double totalUnattributedDuration;

    public String getLoadState() {
        return loadState;
    }

    public LongestScriptAttribution getLongestScript() {
        return longestScript;
    }

    public Double getTotalScriptDuration() {
        return totalScriptDuration;
    }

    public Double getTotalStyleAndLayoutDuration() {
        return totalStyleAndLayoutDuration;
    }

    public Double getTotalPaintDuration() {
        return totalPaintDuration;
    }

    public Double getTotalUnattributedDuration() {
        return totalUnattributedDuration;
    }
}
