package com.crawljax.interaction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.interaction.InteractionResultsPlugin.BrowserKey;
import com.crawljax.interaction.InteractionResultsPlugin.BrowserWatermark;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.Test;

public class InteractionResultsPluginParallelTest {

    @Test
    public void distinctBrowsersGetIndependentWatermarkEntries() {
        EmbeddedBrowser browserA = mock(EmbeddedBrowser.class);
        EmbeddedBrowser browserB = mock(EmbeddedBrowser.class);

        ConcurrentMap<BrowserKey, BrowserWatermark> map = new ConcurrentHashMap<>();
        BrowserWatermark watermarkA = map.computeIfAbsent(new BrowserKey(browserA), key -> new BrowserWatermark());
        BrowserWatermark watermarkB = map.computeIfAbsent(new BrowserKey(browserB), key -> new BrowserWatermark());

        assertNotSame(watermarkA, watermarkB);

        watermarkA.createdAt = 100;
        watermarkB.createdAt = 200;

        assertThat(map.get(new BrowserKey(browserA)).createdAt, is(100L));
        assertThat(map.get(new BrowserKey(browserB)).createdAt, is(200L));
    }

    @Test
    public void sameBrowserReusesWatermarkEntry() {
        EmbeddedBrowser browser = mock(EmbeddedBrowser.class);
        ConcurrentMap<BrowserKey, BrowserWatermark> map = new ConcurrentHashMap<>();

        BrowserWatermark first = map.computeIfAbsent(new BrowserKey(browser), key -> new BrowserWatermark());
        first.createdAt = 42;
        BrowserWatermark second = map.computeIfAbsent(new BrowserKey(browser), key -> new BrowserWatermark());

        assertThat(second.createdAt, is(42L));
        assertThat(map.size(), is(1));
    }
}
