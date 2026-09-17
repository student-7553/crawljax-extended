package com.crawljax.interaction;

/**
 * Keyboard details mirrored from retro-board {@code TypedKeyInput}.
 */
public class TypedKeyInput {

    private String key;
    private String code;
    private boolean ctrlKey;
    private boolean altKey;
    private boolean shiftKey;
    private boolean metaKey;
    private boolean repeat;

    public String getKey() {
        return key;
    }

    public String getCode() {
        return code;
    }

    public boolean isCtrlKey() {
        return ctrlKey;
    }

    public boolean isAltKey() {
        return altKey;
    }

    public boolean isShiftKey() {
        return shiftKey;
    }

    public boolean isMetaKey() {
        return metaKey;
    }

    public boolean isRepeat() {
        return repeat;
    }
}
