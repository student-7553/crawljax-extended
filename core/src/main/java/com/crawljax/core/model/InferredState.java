package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;

/**
 * A state in an inferred user-behavior automaton (for example a GK-Tail state {@code q0}).
 *
 * <p>These states are abstract interaction states, not Crawljax DOM {@code StateVertex} objects.
 */
public final class InferredState {

    private final String id;
    private final boolean initial;
    private final boolean terminal;
    private final int visitCount;
    private final int terminalCount;

    public InferredState(String id, boolean initial, boolean terminal, int visitCount, int terminalCount) {
        this.id = Preconditions.checkNotNull(id, "State id must not be null");
        this.initial = initial;
        this.terminal = terminal;
        this.visitCount = visitCount;
        this.terminalCount = terminalCount;
    }

    public String getId() {
        return id;
    }

    public boolean isInitial() {
        return initial;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public int getTerminalCount() {
        return terminalCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof InferredState)) {
            return false;
        }
        return id.equals(((InferredState) object).id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("initial", initial)
                .add("terminal", terminal)
                .add("visitCount", visitCount)
                .toString();
    }
}
