package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * A single key event recorded on a keyboard transition.
 */
public final class TypedKey {

    private final String key;
    private final String code;
    private final boolean ctrlKey;
    private final boolean altKey;
    private final boolean shiftKey;
    private final boolean metaKey;
    private final boolean repeat;

    public TypedKey(
            String key,
            String code,
            boolean ctrlKey,
            boolean altKey,
            boolean shiftKey,
            boolean metaKey,
            boolean repeat) {
        this.key = key;
        this.code = code;
        this.ctrlKey = ctrlKey;
        this.altKey = altKey;
        this.shiftKey = shiftKey;
        this.metaKey = metaKey;
        this.repeat = repeat;
    }

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

    public boolean isPrintable() {
        return key != null && key.length() == 1;
    }

    /**
     * {@code Shift}, {@code Control}, {@code Alt}, and {@code Meta} are recorded alongside the
     * character they modify and should not be replayed on their own.
     */
    public boolean isModifierOnly() {
        return "Shift".equals(key)
                || "Control".equals(key)
                || "Ctrl".equals(key)
                || "Alt".equals(key)
                || "Meta".equals(key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, code, ctrlKey, altKey, shiftKey, metaKey, repeat);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TypedKey)) {
            return false;
        }
        TypedKey that = (TypedKey) object;
        return ctrlKey == that.ctrlKey
                && altKey == that.altKey
                && shiftKey == that.shiftKey
                && metaKey == that.metaKey
                && repeat == that.repeat
                && Objects.equals(key, that.key)
                && Objects.equals(code, that.code);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", key)
                .add("code", code)
                .toString();
    }
}
