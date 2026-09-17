package com.crawljax.interaction;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.ExitNotifier.ExitStatus;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.core.plugin.PostCrawlingPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.StateVertex;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures retro-board interaction / layout-shift results for each newly discovered state by calling
 * {@code window.getLatestInteractionResults()} in the browser.
 * <p>
 * Only payloads with {@code createdAt} greater than the previous poll watermark are attributed to the
 * state (i.e. triggered by the crawler action that produced it). When multiple new entries exist, the
 * one with the highest {@code createdAt} is kept. The index state advances the watermark but is not
 * attributed to a crawler action.
 * <p>
 * Watermarks are tracked per {@link EmbeddedBrowser} instance so parallel crawlers do not interleave
 * timestamps across browsers.
 */
public class InteractionResultsPlugin implements OnNewStatePlugin, PreCrawlingPlugin, PostCrawlingPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(InteractionResultsPlugin.class);

    static final String FETCH_SCRIPT =
            "return (typeof window.getLatestInteractionResults === 'function')"
                    + " ? window.getLatestInteractionResults() : null;";

    private static final String OUTPUT_FILE = "interaction-results.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final ConcurrentMap<String, StateInteractionRecord> recordsByState = new ConcurrentHashMap<>();
    private final ConcurrentMap<BrowserKey, BrowserWatermark> watermarksByBrowser = new ConcurrentHashMap<>();

    private File outputDirectory;

    @Override
    public void preCrawling(CrawljaxConfiguration config) {
        outputDirectory = config.getOutputDir();
        watermarksByBrowser.clear();
        LOG.info("InteractionResultsPlugin will write to {}", new File(outputDirectory, OUTPUT_FILE));
    }

    @Override
    public void onNewState(CrawlerContext context, StateVertex newState) {
        EmbeddedBrowser browser = context.getBrowser();
        BrowserWatermark browserWatermark =
                watermarksByBrowser.computeIfAbsent(new BrowserKey(browser), key -> new BrowserWatermark());

        InteractionPayload interaction;
        LayoutShiftPayload layoutShift;
        long watermark;
        synchronized (browserWatermark) {
            LatestInteractionResults latest = fetchLatestResults(browser);
            if (latest == null) {
                LOG.debug(
                        "No getLatestInteractionResults() on page for state {} ({})",
                        newState.getName(),
                        newState.getId());
                return;
            }

            watermark = browserWatermark.createdAt;
            interaction = InteractionResultsSelector.selectNewestInteraction(latest.getInteractions(), watermark)
                    .orElse(null);
            layoutShift = InteractionResultsSelector.selectNewestLayoutShift(latest.getLayoutShifts(), watermark)
                    .orElse(null);
            browserWatermark.createdAt = InteractionResultsSelector.maxCreatedAt(
                    latest.getInteractions(), latest.getLayoutShifts(), watermark);

            if (newState.getId() == StateVertex.INDEX_ID) {
                LOG.info(
                        "Index state: advanced interaction watermark to {} for browser {} (no crawler-action attribution)",
                        browserWatermark.createdAt,
                        System.identityHashCode(browser));
                return;
            }
        }

        StateInteractionRecord record =
                new StateInteractionRecord(newState.getName(), newState.getId(), interaction, layoutShift);
        recordsByState.put(newState.getName(), record);

        if (interaction != null) {
            LOG.info(
                    "State {} interaction createdAt={} value={} target={}",
                    newState.getName(),
                    interaction.getCreatedAt(),
                    interaction.getValue(),
                    interaction.getInteractionTarget());
        } else {
            LOG.info("State {}: no new interaction since watermark {}", newState.getName(), watermark);
        }
        if (layoutShift != null) {
            LOG.info(
                    "State {} layoutShift createdAt={} value={}",
                    newState.getName(),
                    layoutShift.getCreatedAt(),
                    layoutShift.getValue());
        } else {
            LOG.info("State {}: no new layoutShift since watermark {}", newState.getName(), watermark);
        }
    }

    @Override
    public void postCrawling(CrawlSession session, ExitStatus exitReason) {
        File outDir = outputDirectory != null ? outputDirectory : session.getConfig().getOutputDir();
        if (outDir == null) {
            LOG.warn("No output directory; skipping interaction-results.json");
            return;
        }
        File out = new File(outDir, OUTPUT_FILE);
        try (Writer writer = new FileWriter(out)) {
            gson.toJson(getRecordsByState(), writer);
            LOG.info("Wrote {} state interaction records to {}", recordsByState.size(), out.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to write {}", out.getAbsolutePath(), e);
        }
    }

    /**
     * @return immutable snapshot of per-state records collected so far
     */
    public Map<String, StateInteractionRecord> getRecordsByState() {
        return ImmutableMap.copyOf(recordsByState);
    }

    public StateInteractionRecord getRecord(String stateName) {
        return recordsByState.get(stateName);
    }

    private LatestInteractionResults fetchLatestResults(EmbeddedBrowser browser) {
        try {
            Object raw = browser.executeJavaScript(FETCH_SCRIPT);
            if (raw == null) {
                return null;
            }
            return gson.fromJson(gson.toJsonTree(raw), LatestInteractionResults.class);
        } catch (RuntimeException e) {
            LOG.warn("Failed to call getLatestInteractionResults(): {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "InteractionResultsPlugin";
    }

    /**
     * Identity key so distinct browser instances never share a watermark, regardless of
     * {@code equals}/{@code hashCode}.
     */
    static final class BrowserKey {
        private final EmbeddedBrowser browser;

        BrowserKey(EmbeddedBrowser browser) {
            this.browser = browser;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BrowserKey && ((BrowserKey) other).browser == browser;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(browser);
        }
    }

    static final class BrowserWatermark {
        long createdAt;
    }
}
