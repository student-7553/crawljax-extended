package com.crawljax.core.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Chooses the next inferred-model transition so that every reachable automaton state is visited at
 * least once. Ranking is applied only to transitions the caller has marked as DOM-enabled.
 */
public final class ModelCoveragePlanner {

    private final InferredModel model;
    private final Set<String> visited = new HashSet<>();
    private final Set<InferredTransition> failed = new HashSet<>();
    private final Set<InferredTransition> firedSelfLoops = new HashSet<>();
    private String currentStateId;

    public ModelCoveragePlanner(InferredModel model) {
        this.model = model;
        this.currentStateId = model.getInitialState().getId();
        this.visited.add(this.currentStateId);
    }

    public String getCurrentStateId() {
        return currentStateId;
    }

    public int getVisitedCount() {
        return visited.size();
    }

    public boolean isVisited(String stateId) {
        return visited.contains(stateId);
    }

    public boolean isCoverageComplete() {
        Set<String> unvisited = unvisitedIds();
        if (unvisited.isEmpty()) {
            return true;
        }
        return shortestPath(currentStateId, unvisited) == null
                && shortestPath(model.getInitialState().getId(), unvisited) == null;
    }

    /**
     * Resets the automaton cursor to the initial state after the browser reloads the start URL.
     */
    public void resetToInitial() {
        currentStateId = model.getInitialState().getId();
        visited.add(currentStateId);
    }

    public void onSuccess(InferredTransition transition) {
        currentStateId = transition.getTargetId();
        visited.add(currentStateId);
        if (transition.isSelfLoop()) {
            firedSelfLoops.add(transition);
        }
    }

    public void onFailure(InferredTransition transition) {
        failed.add(transition);
    }

    /**
     * @param enabledOutgoing outgoing transitions whose CSS selectors are present and displayed
     * @return the next action, or {@code null} if reachable coverage is done
     */
    public PlannedAction next(List<InferredTransition> enabledOutgoing) {
        if (isCoverageComplete()) {
            return null;
        }

        List<InferredTransition> enabled = usable(enabledOutgoing);

        InferredTransition selfLoop = best(enabled, t -> t.isSelfLoop() && !firedSelfLoops.contains(t));
        if (selfLoop != null) {
            return PlannedAction.fire(selfLoop);
        }

        InferredTransition toUnvisited = best(enabled, t -> !t.isSelfLoop() && !visited.contains(t.getTargetId()));
        if (toUnvisited != null) {
            return PlannedAction.fire(toUnvisited);
        }

        Set<String> unvisited = unvisitedIds();
        List<InferredTransition> fromCurrent = shortestPath(currentStateId, unvisited);
        if (fromCurrent != null) {
            // Fire the next edge even if it is not DOM-enabled so the crawler can record
            // onFailure. Resetting here would replay a working prefix forever.
            return PlannedAction.fire(fromCurrent.get(0));
        }

        List<InferredTransition> fromStart = shortestPath(model.getInitialState().getId(), unvisited);
        if (fromStart != null) {
            if (currentStateId.equals(model.getInitialState().getId())) {
                return PlannedAction.fire(fromStart.get(0));
            }
            return PlannedAction.resetAndFire(fromStart.get(0));
        }

        return null;
    }

    private List<InferredTransition> usable(List<InferredTransition> enabledOutgoing) {
        List<InferredTransition> usable = new ArrayList<>();
        if (enabledOutgoing == null) {
            return usable;
        }
        for (InferredTransition transition : enabledOutgoing) {
            if (transition != null
                    && !failed.contains(transition)
                    && currentStateId.equals(transition.getSourceId())) {
                usable.add(transition);
            }
        }
        return usable;
    }

    private InferredTransition best(
            List<InferredTransition> enabled, java.util.function.Predicate<InferredTransition> predicate) {
        return enabled.stream()
                .filter(predicate)
                .max(Comparator.comparingInt(InferredTransition::getCount))
                .orElse(null);
    }

    private Set<String> unvisitedIds() {
        Set<String> unvisited = new HashSet<>();
        for (InferredState state : model.getStates()) {
            if (!visited.contains(state.getId())) {
                unvisited.add(state.getId());
            }
        }
        return unvisited;
    }

    private List<InferredTransition> shortestPath(String from, Set<String> goals) {
        if (goals == null || goals.isEmpty()) {
            return null;
        }
        Queue<String> queue = new ArrayDeque<>();
        Map<String, InferredTransition> reachedBy = new HashMap<>();
        Set<String> seen = new HashSet<>();
        queue.add(from);
        seen.add(from);
        String found = null;
        while (!queue.isEmpty() && found == null) {
            String node = queue.poll();
            for (InferredTransition edge : model.getOutgoing(node)) {
                if (failed.contains(edge) || edge.isSelfLoop()) {
                    continue;
                }
                String target = edge.getTargetId();
                if (!seen.add(target)) {
                    continue;
                }
                reachedBy.put(target, edge);
                if (goals.contains(target)) {
                    found = target;
                    break;
                }
                queue.add(target);
            }
        }
        if (found == null) {
            return null;
        }
        List<InferredTransition> path = new ArrayList<>();
        String cursor = found;
        while (!cursor.equals(from)) {
            InferredTransition edge = reachedBy.get(cursor);
            path.add(edge);
            cursor = edge.getSourceId();
        }
        Collections.reverse(path);
        return path;
    }
}
