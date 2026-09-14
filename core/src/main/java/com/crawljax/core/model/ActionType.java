package com.crawljax.core.model;

/**
 * User action recorded on a transition of an {@link InferredModel}.
 */
public enum ActionType {
    POINTER,
    KEYBOARD;

    static ActionType fromJson(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Action type is missing");
        }
        switch (type.trim().toLowerCase()) {
            case "pointer":
                return POINTER;
            case "keyboard":
                return KEYBOARD;
            default:
                throw new IllegalArgumentException("Unknown action type: " + type);
        }
    }
}
