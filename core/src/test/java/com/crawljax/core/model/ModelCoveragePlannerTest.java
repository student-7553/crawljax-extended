package com.crawljax.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class ModelCoveragePlannerTest {

    @Test
    public void containsEnterDetectsSubmitKey() {
        TypedKey enter = new TypedKey("Enter", "Enter", false, false, false, false, false);
        TransitionInputs inputs = new TransitionInputs(java.util.Collections.singletonList(enter));
        assertTrue(inputs.containsEnter());
        assertThat(inputs.reconstructTypedText(), is(""));
    }

    @Test
    public void replayableKeysKeepControlsAndSkipModifiers() {
        TypedKey shift = new TypedKey("Shift", "ShiftLeft", false, false, true, false, false);
        TypedKey h = new TypedKey("H", "KeyH", false, false, true, false, false);
        TypedKey backspace = new TypedKey("Backspace", "Backspace", false, false, false, false, false);
        TypedKey tab = new TypedKey("Tab", "Tab", false, false, false, false, false);
        TransitionInputs inputs = new TransitionInputs(Arrays.asList(shift, h, backspace, tab));

        assertThat(inputs.reconstructTypedText(), is(""));
        assertThat(inputs.replayableKeys().size(), is(3));
        assertThat(inputs.replayableKeys().get(0).getKey(), is("H"));
        assertThat(inputs.replayableKeys().get(1).getKey(), is("Backspace"));
        assertThat(inputs.replayableKeys().get(2).getKey(), is("Tab"));
    }

    @Test
    public void followsLinearPathTowardUnvisitedStates() {
        InferredTransition t01 = pointer("q0", "q1", "a.one", 1);
        InferredTransition t12 = pointer("q1", "q2", "a.two", 1);
        InferredModel model = model(states("q0", "q1", "q2"), t01, t12);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        assertThat(planner.getCurrentStateId(), is("q0"));
        PlannedAction first = planner.next(Collections.singletonList(t01));
        assertThat(first.getTransition(), is(t01));
        assertFalse(first.isResetToIndex());

        planner.onSuccess(t01);
        assertThat(planner.getCurrentStateId(), is("q1"));
        PlannedAction second = planner.next(Collections.singletonList(t12));
        assertThat(second.getTransition(), is(t12));

        planner.onSuccess(t12);
        assertTrue(planner.isCoverageComplete());
        assertThat(planner.next(Collections.emptyList()), is(nullValue()));
    }

    @Test
    public void firesMissingOutgoingEdgeInsteadOfResetting() {
        InferredTransition t01 = pointer("q0", "q1", "a.one", 1);
        InferredTransition t12 = pointer("q1", "q2", "a.two", 1);
        InferredModel model = model(states("q0", "q1", "q2"), t01, t12);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);
        planner.onSuccess(t01);

        PlannedAction next = planner.next(Collections.emptyList());
        assertFalse(next.isResetToIndex());
        assertThat(next.getTransition(), is(t12));

        planner.onFailure(t12);
        assertTrue(planner.isCoverageComplete());
        assertThat(planner.next(Collections.emptyList()), is(nullValue()));
    }

    @Test
    public void doesNotResetWhenAlreadyAtIndexAndEdgeIsMissing() {
        InferredTransition t01 = pointer("q0", "q1", "a.one", 1);
        InferredModel model = model(states("q0", "q1"), t01);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        PlannedAction next = planner.next(Collections.emptyList());
        assertFalse(next.isResetToIndex());
        assertThat(next.getTransition(), is(t01));
    }

    @Test
    public void prefersHigherCountEdgeToUnvisitedState() {
        InferredTransition rare = pointer("q0", "q1", "a.rare", 1);
        InferredTransition common = pointer("q0", "q2", "a.common", 5);
        InferredModel model = model(states("q0", "q1", "q2"), rare, common);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        PlannedAction next = planner.next(Arrays.asList(rare, common));
        assertThat(next.getTransition(), is(common));
    }

    @Test
    public void firesUnusedSelfLoopBeforeLeaving() {
        InferredTransition loop = keyboard("q0", "q0", "input.search", 2);
        InferredTransition leave = pointer("q0", "q1", "button.go", 1);
        InferredModel model = model(states("q0", "q1"), loop, leave);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        PlannedAction first = planner.next(Arrays.asList(loop, leave));
        assertThat(first.getTransition(), is(loop));

        planner.onSuccess(loop);
        PlannedAction second = planner.next(Arrays.asList(loop, leave));
        assertThat(second.getTransition(), is(leave));
    }

    @Test
    public void skipsFailedEdgeAndTakesAlternative() {
        InferredTransition broken = pointer("q0", "q1", "a.broken", 10);
        InferredTransition ok = pointer("q0", "q2", "a.ok", 1);
        InferredModel model = model(states("q0", "q1", "q2"), broken, ok);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        planner.onFailure(broken);
        PlannedAction next = planner.next(Arrays.asList(broken, ok));
        assertThat(next.getTransition(), is(ok));
    }

    @Test
    public void resetsToIndexWhenCurrentStateHasNoPathToUnvisited() {
        InferredTransition toA = pointer("q0", "q1", "a.left", 1);
        InferredTransition toB = pointer("q0", "q2", "a.right", 1);
        InferredModel model = model(states("q0", "q1", "q2"), toA, toB);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        planner.onSuccess(toA);
        assertThat(planner.getCurrentStateId(), is("q1"));

        PlannedAction next = planner.next(Collections.emptyList());
        assertTrue(next.isResetToIndex());
        assertThat(next.getTransition(), is(toB));

        planner.resetToInitial();
        assertThat(planner.getCurrentStateId(), is("q0"));
    }

    @Test
    public void returnsNullWhenRemainingStatesAreUnreachableAfterFailures() {
        InferredTransition only = pointer("q0", "q1", "a.only", 1);
        InferredModel model = model(states("q0", "q1"), only);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);

        planner.onFailure(only);
        assertTrue(planner.isCoverageComplete());
        assertThat(planner.next(Collections.singletonList(only)), is(nullValue()));
        assertThat(planner.getVisitedCount(), is(1));
    }

    @Test
    public void followsEnabledFirstStepOfShortestPath() {
        InferredTransition t01 = pointer("q0", "q1", "a.one", 1);
        InferredTransition t12 = pointer("q1", "q2", "a.two", 1);
        InferredTransition t23 = pointer("q2", "q3", "a.three", 1);
        InferredModel model = model(states("q0", "q1", "q2", "q3"), t01, t12, t23);
        ModelCoveragePlanner planner = new ModelCoveragePlanner(model);
        planner.onSuccess(t01);

        PlannedAction next = planner.next(Collections.singletonList(t12));
        assertFalse(next.isResetToIndex());
        assertThat(next.getTransition(), is(t12));
    }

    private static List<InferredState> states(String... ids) {
        List<InferredState> states = new java.util.ArrayList<>();
        for (int i = 0; i < ids.length; i++) {
            boolean initial = i == 0;
            boolean terminal = i == ids.length - 1;
            states.add(new InferredState(ids[i], initial, terminal, 1, terminal ? 1 : 0));
        }
        return states;
    }

    private static InferredTransition pointer(String from, String to, String css, int count) {
        return new InferredTransition(
                from, to, new InferredAction(ActionType.POINTER, css), count, null, null);
    }

    private static InferredTransition keyboard(String from, String to, String css, int count) {
        return new InferredTransition(
                from, to, new InferredAction(ActionType.KEYBOARD, css), count, null, new TransitionInputs(null));
    }

    private static InferredModel model(List<InferredState> states, InferredTransition... transitions) {
        return new InferredModel(null, states, Arrays.asList(transitions));
    }
}
