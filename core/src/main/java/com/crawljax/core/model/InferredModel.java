package com.crawljax.core.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An inferred user-behavior automaton (typically produced by GK-Tail) used to guide crawling.
 *
 * <p>This is distinct from Crawljax's runtime {@link com.crawljax.core.state.StateFlowGraph}:
 * states here are abstract interaction states ({@code q0}, {@code q1}, ...) and edges are labeled
 * with recorded CSS selectors and keyboard/pointer actions.
 */
public final class InferredModel {

    private final InferredModelMetadata metadata;
    private final ImmutableMap<String, InferredState> statesById;
    private final ImmutableList<InferredTransition> transitions;
    private final ImmutableListMultimap<String, InferredTransition> outgoing;
    private final ImmutableListMultimap<String, InferredTransition> incoming;
    private final InferredState initialState;
    private final ImmutableList<InferredState> terminalStates;

    public InferredModel(
            InferredModelMetadata metadata, List<InferredState> states, List<InferredTransition> transitions) {
        Preconditions.checkNotNull(states, "States must not be null");
        Preconditions.checkNotNull(transitions, "Transitions must not be null");
        Preconditions.checkArgument(!states.isEmpty(), "Inferred model must contain at least one state");

        this.metadata = metadata;
        this.statesById = indexStates(states);
        this.transitions = ImmutableList.copyOf(transitions);
        this.initialState = findInitialState(this.statesById);
        this.terminalStates = findTerminalStates(this.statesById);
        this.outgoing = indexBySource(this.transitions, this.statesById);
        this.incoming = indexByTarget(this.transitions, this.statesById);
    }

    private static ImmutableMap<String, InferredState> indexStates(List<InferredState> states) {
        Map<String, InferredState> indexed = new LinkedHashMap<>();
        for (InferredState state : states) {
            InferredState previous = indexed.put(state.getId(), state);
            Preconditions.checkArgument(previous == null, "Duplicate state id: %s", state.getId());
        }
        return ImmutableMap.copyOf(indexed);
    }

    private static InferredState findInitialState(ImmutableMap<String, InferredState> statesById) {
        InferredState found = null;
        for (InferredState state : statesById.values()) {
            if (state.isInitial()) {
                Preconditions.checkArgument(found == null, "Inferred model must have exactly one initial state");
                found = state;
            }
        }
        Preconditions.checkArgument(found != null, "Inferred model must have an initial state");
        return found;
    }

    private static ImmutableList<InferredState> findTerminalStates(ImmutableMap<String, InferredState> statesById) {
        ImmutableList.Builder<InferredState> terminals = ImmutableList.builder();
        for (InferredState state : statesById.values()) {
            if (state.isTerminal()) {
                terminals.add(state);
            }
        }
        return terminals.build();
    }

    private static ImmutableListMultimap<String, InferredTransition> indexBySource(
            ImmutableList<InferredTransition> transitions, ImmutableMap<String, InferredState> statesById) {
        ImmutableListMultimap.Builder<String, InferredTransition> builder = ImmutableListMultimap.builder();
        for (InferredTransition transition : transitions) {
            Preconditions.checkArgument(
                    statesById.containsKey(transition.getSourceId()),
                    "Transition source %s is not a known state",
                    transition.getSourceId());
            Preconditions.checkArgument(
                    statesById.containsKey(transition.getTargetId()),
                    "Transition target %s is not a known state",
                    transition.getTargetId());
            builder.put(transition.getSourceId(), transition);
        }
        return builder.build();
    }

    private static ImmutableListMultimap<String, InferredTransition> indexByTarget(
            ImmutableList<InferredTransition> transitions, ImmutableMap<String, InferredState> statesById) {
        ImmutableListMultimap.Builder<String, InferredTransition> builder = ImmutableListMultimap.builder();
        for (InferredTransition transition : transitions) {
            builder.put(transition.getTargetId(), transition);
        }
        return builder.build();
    }

    public InferredModelMetadata getMetadata() {
        return metadata;
    }

    public InferredState getInitialState() {
        return initialState;
    }

    public InferredState getState(String id) {
        return statesById.get(id);
    }

    public ImmutableSet<InferredState> getStates() {
        return ImmutableSet.copyOf(statesById.values());
    }

    public ImmutableList<InferredTransition> getTransitions() {
        return transitions;
    }

    public ImmutableList<InferredState> getTerminalStates() {
        return terminalStates;
    }

    /**
     * Outgoing actions the crawler can fire from the given automaton state.
     */
    public ImmutableList<InferredTransition> getOutgoing(String stateId) {
        return outgoing.get(stateId);
    }

    public ImmutableList<InferredTransition> getOutgoing(InferredState state) {
        return getOutgoing(state.getId());
    }

    public ImmutableList<InferredTransition> getIncoming(String stateId) {
        return incoming.get(stateId);
    }

    public int getStateCount() {
        return statesById.size();
    }

    public int getTransitionCount() {
        return transitions.size();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("metadata", metadata)
                .add("states", getStateCount())
                .add("transitions", getTransitionCount())
                .add("initial", initialState.getId())
                .add("terminals", terminalStates.size())
                .toString();
    }
}
