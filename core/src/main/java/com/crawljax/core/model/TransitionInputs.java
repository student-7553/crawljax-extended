package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

/**
 * Keyboard payload attached to an {@link InferredTransition}.
 */
public final class TransitionInputs {

    private final ImmutableList<TypedKey> typedKeys;

    public TransitionInputs(List<TypedKey> typedKeys) {
        this.typedKeys = typedKeys == null ? ImmutableList.of() : ImmutableList.copyOf(typedKeys);
    }

    public ImmutableList<TypedKey> getTypedKeys() {
        return typedKeys;
    }

    /**
     * Rebuilds the text a user would see after applying the recorded key sequence, including
     * backspaces. Modifier-only and control keys such as {@code Enter} and {@code Shift} are
     * skipped.
     */
    public String reconstructTypedText() {
        StringBuilder text = new StringBuilder();
        for (TypedKey typedKey : typedKeys) {
            if ("Backspace".equals(typedKey.getKey())) {
                if (text.length() > 0) {
                    text.deleteCharAt(text.length() - 1);
                }
            } else if (typedKey.isPrintable()) {
                text.append(typedKey.getKey());
            }
        }
        return text.toString();
    }

    /**
     * Keys to replay in order without clearing the field. Modifier-only keys are omitted so that
     * {@code Shift+H} is sent as {@code H}.
     */
    public ImmutableList<TypedKey> replayableKeys() {
        ImmutableList.Builder<TypedKey> replayable = ImmutableList.builder();
        for (TypedKey typedKey : typedKeys) {
            if (typedKey.getKey() != null && !typedKey.isModifierOnly()) {
                replayable.add(typedKey);
            }
        }
        return replayable.build();
    }

    public boolean isEmpty() {
        return typedKeys.isEmpty();
    }

    public boolean containsEnter() {
        for (TypedKey typedKey : typedKeys) {
            if ("Enter".equals(typedKey.getKey())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(typedKeys);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof TransitionInputs)) {
            return false;
        }
        return typedKeys.equals(((TransitionInputs) object).typedKeys);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("typedKeys", typedKeys.size())
                .add("text", reconstructTypedText())
                .toString();
    }
}
