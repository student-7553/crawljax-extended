package com.crawljax.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.state.Identification.How;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class InferredModelLoaderTest {

    @Test
    public void loadsFixtureAndIndexesGraph() {
        InferredModel model = InferredModelLoader.load(fixturePath());

        assertThat(model.getMetadata().getAlgorithm(), is("GK-Tail"));
        assertThat(model.getMetadata().getK(), is(2));
        assertThat(model.getStateCount(), is(3));
        assertThat(model.getTransitionCount(), is(3));
        assertThat(model.getInitialState().getId(), is("q0"));
        assertThat(model.getTerminalStates(), hasSize(1));
        assertThat(model.getTerminalStates().get(0).getId(), is("q2"));

        InferredTransition clickHome = model.getOutgoing("q0").get(0);
        assertThat(clickHome.getAction().getType(), is(ActionType.POINTER));
        assertThat(clickHome.getAction().getTarget(), is("div.menu>a.home"));
        assertThat(clickHome.getAction().toIdentification().getHow(), is(How.css));
        assertThat(clickHome.getTimings().suggestedWaitMillis(), is(100L));

        InferredTransition typing = model.getOutgoing("q1").get(0);
        assertThat(typing.isSelfLoop(), is(true));
        assertThat(typing.getAction().isKeyboard(), is(true));
        assertThat(typing.getInputs().reconstructTypedText(), is("Hi"));

        assertThat(model.getOutgoing("q2"), hasSize(0));
        assertThat(model.getIncoming("q1"), hasSize(2));
    }

    @Test
    public void storesModelOnCrawljaxConfiguration() {
        InferredModel loaded = InferredModelLoader.load(fixturePath());

        CrawljaxConfiguration config = CrawljaxConfiguration.builderFor("http://localhost")
                .setInferredModel(fixturePath())
                .build();

        assertThat(config.getInferredModel(), is(notNullValue()));
        assertThat(config.getInferredModel().getStateCount(), is(loaded.getStateCount()));
        assertThat(config.getInferredModel().getInitialState().getId(), is("q0"));
    }

    @Test
    public void missingFileFails() {
        assertThrows(CrawljaxException.class, () -> InferredModelLoader.load(Paths.get("does-not-exist.json")));
    }

    @Test
    public void unknownActionTypeFails() {
        String json = "{"
                + "\"states\":[{\"id\":\"q0\",\"initial\":true,\"terminal\":false}],"
                + "\"transitions\":[{"
                + "\"source\":\"q0\",\"target\":\"q0\","
                + "\"action\":{\"type\":\"swipe\",\"target\":\"div.x\"}"
                + "}]}";
        assertThrows(CrawljaxException.class, () -> InferredModelLoader.load(new StringReader(json)));
    }

    @Test
    public void missingInitialStateFails() {
        String json = "{"
                + "\"states\":[{\"id\":\"q0\",\"initial\":false,\"terminal\":true}],"
                + "\"transitions\":[]}";
        assertThrows(CrawljaxException.class, () -> InferredModelLoader.load(new StringReader(json)));
    }

    @Test
    public void danglingTransitionFails() {
        String json = "{"
                + "\"states\":[{\"id\":\"q0\",\"initial\":true,\"terminal\":false}],"
                + "\"transitions\":[{"
                + "\"source\":\"q0\",\"target\":\"q99\","
                + "\"action\":{\"type\":\"pointer\",\"target\":\"a.link\"}"
                + "}]}";
        assertThrows(CrawljaxException.class, () -> InferredModelLoader.load(new StringReader(json)));
    }

    @Test
    public void loadsSampleModelWhenPresent() {
        Path sample = sampleModelPath();
        assumeTrue("samples/model.json is not on disk", sample != null && Files.isRegularFile(sample));

        InferredModel model = InferredModelLoader.load(sample);
        assertThat(model.getMetadata().getAlgorithm(), is("GK-Tail"));
        assertThat(model.getStateCount(), is(35));
        assertThat(model.getTransitionCount(), is(42));
        assertThat(model.getInitialState().getId(), is("q0"));
        assertThat(model.getTerminalStates(), hasSize(1));
        assertThat(model.getState("q34").isTerminal(), is(true));
        assertThat(model.getOutgoing("q18"), hasSize(3));
        assertThat(model.getOutgoing("q0").get(0).getAction().getType(), is(ActionType.POINTER));

        InferredTransition typing = findFirstKeyboardWithText(model);
        assertThat(typing, is(notNullValue()));
        assertThat(typing.getInputs().reconstructTypedText().isEmpty(), is(false));
    }

    @Test
    public void configurationWithoutModelLeavesItUnset() {
        CrawljaxConfiguration config =
                CrawljaxConfiguration.builderFor("http://localhost").build();
        assertThat(config.getInferredModel(), is(nullValue()));
    }

    private static InferredTransition findFirstKeyboardWithText(InferredModel model) {
        for (InferredTransition transition : model.getTransitions()) {
            if (transition.getAction().isKeyboard()
                    && !transition.getInputs().reconstructTypedText().isEmpty()) {
                return transition;
            }
        }
        return null;
    }

    private static Path fixturePath() {
        return Paths.get("src", "test", "resources", "model", "inferred-model.json");
    }

    private static Path sampleModelPath() {
        Path fromCoreModule = Paths.get("..", "samples", "model.json");
        if (Files.isRegularFile(fromCoreModule)) {
            return fromCoreModule;
        }
        Path fromRepoRoot = Paths.get("samples", "model.json");
        if (Files.isRegularFile(fromRepoRoot)) {
            return fromRepoRoot;
        }
        return null;
    }
}
