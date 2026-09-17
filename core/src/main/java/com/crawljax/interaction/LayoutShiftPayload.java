package com.crawljax.interaction;

import java.util.Collections;
import java.util.List;

/**
 * Layout-shift metrics mirrored from retro-board {@code LayoutShiftPayload}.
 */
public class LayoutShiftPayload {

    private String name;
    private long createdAt;
    private double value;
    private double startTime;
    private double duration;
    private boolean hadRecentInput;
    private double lastInputTime;
    private List<LayoutShiftSource> sources = Collections.emptyList();

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public double getValue() {
        return value;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getDuration() {
        return duration;
    }

    public boolean isHadRecentInput() {
        return hadRecentInput;
    }

    public double getLastInputTime() {
        return lastInputTime;
    }

    public List<LayoutShiftSource> getSources() {
        return sources;
    }
}
