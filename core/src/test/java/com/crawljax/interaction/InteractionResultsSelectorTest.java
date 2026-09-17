package com.crawljax.interaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class InteractionResultsSelectorTest {

    private static final Gson GSON = new Gson();

    @Test
    public void selectsInteractionNewerThanWatermarkWithHighestCreatedAt() {
        InteractionPayload older = interaction(100, 10);
        InteractionPayload mid = interaction(200, 50);
        InteractionPayload newest = interaction(300, 20);

        Optional<InteractionPayload> selected =
                InteractionResultsSelector.selectNewestInteraction(Arrays.asList(older, newest, mid), 150);

        assertTrue(selected.isPresent());
        assertThat(selected.get().getCreatedAt(), is(300L));
        assertThat(selected.get().getValue(), is(20.0));
    }

    @Test
    public void ignoresInteractionsAtOrBeforeWatermark() {
        Optional<InteractionPayload> selected = InteractionResultsSelector.selectNewestInteraction(
                Arrays.asList(interaction(100, 1), interaction(200, 2)), 200);

        assertFalse(selected.isPresent());
    }

    @Test
    public void selectsLayoutShiftNewerThanWatermarkWithHighestCreatedAt() {
        LayoutShiftPayload older = layoutShift(10, 0.1);
        LayoutShiftPayload newer = layoutShift(30, 0.05);
        LayoutShiftPayload mid = layoutShift(20, 0.9);

        Optional<LayoutShiftPayload> selected =
                InteractionResultsSelector.selectNewestLayoutShift(Arrays.asList(older, mid, newer), 15);

        assertTrue(selected.isPresent());
        assertThat(selected.get().getCreatedAt(), is(30L));
        assertThat(selected.get().getValue(), is(0.05));
    }

    @Test
    public void maxCreatedAtConsidersBothLists() {
        long max = InteractionResultsSelector.maxCreatedAt(
                Collections.singletonList(interaction(50, 1)),
                Collections.singletonList(layoutShift(80, 0.2)),
                10);
        assertThat(max, is(80L));
    }

    @Test
    public void emptyListsYieldEmptySelection() {
        assertThat(
                InteractionResultsSelector.selectNewestInteraction(Collections.emptyList(), 0).orElse(null),
                is(nullValue()));
        assertThat(
                InteractionResultsSelector.selectNewestLayoutShift(null, 0).orElse(null), is(nullValue()));
    }

    private static InteractionPayload interaction(long createdAt, double value) {
        return GSON.fromJson("{\"createdAt\":" + createdAt + ",\"value\":" + value + "}", InteractionPayload.class);
    }

    private static LayoutShiftPayload layoutShift(long createdAt, double value) {
        return GSON.fromJson("{\"createdAt\":" + createdAt + ",\"value\":" + value + "}", LayoutShiftPayload.class);
    }
}
