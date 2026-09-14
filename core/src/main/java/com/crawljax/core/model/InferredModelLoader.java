package com.crawljax.core.model;

import com.crawljax.core.CrawljaxException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a GK-Tail (or compatible) inferred automaton from JSON into an {@link InferredModel}.
 */
public final class InferredModelLoader {

    private static final Logger LOG = LoggerFactory.getLogger(InferredModelLoader.class);
    private static final Gson GSON = new GsonBuilder().create();

    private InferredModelLoader() {}

    public static InferredModel load(File file) {
        if (file == null) {
            throw new CrawljaxException("Inferred model file must not be null");
        }
        return load(file.toPath());
    }

    public static InferredModel load(Path path) {
        if (path == null) {
            throw new CrawljaxException("Inferred model path must not be null");
        }
        if (!Files.isRegularFile(path)) {
            throw new CrawljaxException("Inferred model file does not exist: " + path);
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            InferredModel model = load(reader);
            LOG.info("Loaded inferred model from {} ({})", path, model);
            return model;
        } catch (IOException e) {
            throw new CrawljaxException("Could not read inferred model from " + path, e);
        }
    }

    public static InferredModel load(Reader reader) {
        RawModel raw;
        try {
            raw = GSON.fromJson(reader, RawModel.class);
        } catch (JsonParseException e) {
            throw new CrawljaxException("Could not parse inferred model JSON", e);
        }
        if (raw == null) {
            throw new CrawljaxException("Inferred model JSON is empty");
        }
        return toModel(raw);
    }

    private static InferredModel toModel(RawModel raw) {
        List<InferredState> states = new ArrayList<>();
        if (raw.states != null) {
            for (RawState rawState : raw.states) {
                states.add(toState(rawState));
            }
        }

        List<InferredTransition> transitions = new ArrayList<>();
        if (raw.transitions != null) {
            for (RawTransition rawTransition : raw.transitions) {
                transitions.add(toTransition(rawTransition));
            }
        }

        try {
            return new InferredModel(toMetadata(raw.metadata), states, transitions);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CrawljaxException("Invalid inferred model: " + e.getMessage(), e);
        }
    }

    private static InferredModelMetadata toMetadata(RawMetadata raw) {
        if (raw == null) {
            return null;
        }
        return new InferredModelMetadata(raw.algorithm, raw.k, raw.traceCount, raw.interactionCount);
    }

    private static InferredState toState(RawState raw) {
        if (raw == null || raw.id == null) {
            throw new CrawljaxException("Inferred model contains a state without an id");
        }
        return new InferredState(raw.id, raw.initial, raw.terminal, raw.visitCount, raw.terminalCount);
    }

    private static InferredTransition toTransition(RawTransition raw) {
        if (raw == null) {
            throw new CrawljaxException("Inferred model contains a null transition");
        }
        return new InferredTransition(
                raw.source,
                raw.target,
                toAction(raw.action),
                raw.count,
                toTimings(raw.timings),
                toInputs(raw.inputs));
    }

    private static InferredAction toAction(RawAction raw) {
        if (raw == null) {
            throw new CrawljaxException("Transition is missing an action");
        }
        try {
            return new InferredAction(ActionType.fromJson(raw.type), raw.target);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CrawljaxException("Invalid transition action: " + e.getMessage(), e);
        }
    }

    private static TimingStats toTimings(RawTimingStats raw) {
        if (raw == null) {
            return null;
        }
        return new TimingStats(
                toMetric(raw.value),
                toMetric(raw.inputDelay),
                toMetric(raw.processingDuration),
                toMetric(raw.presentationDelay),
                toMetric(raw.totalScriptDuration),
                toMetric(raw.totalStyleAndLayoutDuration),
                toMetric(raw.totalPaintDuration),
                toMetric(raw.totalUnattributedDuration));
    }

    private static TimingMetric toMetric(RawTimingMetric raw) {
        if (raw == null) {
            return null;
        }
        return new TimingMetric(raw.count, raw.min, raw.max, raw.mean, raw.sum);
    }

    private static TransitionInputs toInputs(RawInputs raw) {
        if (raw == null || raw.typedKeys == null) {
            return new TransitionInputs(null);
        }
        List<TypedKey> keys = new ArrayList<>();
        for (RawTypedKey rawKey : raw.typedKeys) {
            if (rawKey == null) {
                continue;
            }
            keys.add(new TypedKey(
                    rawKey.key,
                    rawKey.code,
                    rawKey.ctrlKey,
                    rawKey.altKey,
                    rawKey.shiftKey,
                    rawKey.metaKey,
                    rawKey.repeat));
        }
        return new TransitionInputs(keys);
    }

    static final class RawModel {
        RawMetadata metadata;
        List<RawState> states;
        List<RawTransition> transitions;
    }

    static final class RawMetadata {
        String algorithm;
        int k;
        int traceCount;
        int interactionCount;
    }

    static final class RawState {
        String id;
        boolean initial;
        boolean terminal;
        int visitCount;
        int terminalCount;
    }

    static final class RawTransition {
        String source;
        String target;
        RawAction action;
        int count;
        RawTimingStats timings;
        RawInputs inputs;
    }

    static final class RawAction {
        String type;
        String target;
    }

    static final class RawTimingStats {
        RawTimingMetric value;
        RawTimingMetric inputDelay;
        RawTimingMetric processingDuration;
        RawTimingMetric presentationDelay;
        RawTimingMetric totalScriptDuration;
        RawTimingMetric totalStyleAndLayoutDuration;
        RawTimingMetric totalPaintDuration;
        RawTimingMetric totalUnattributedDuration;
    }

    static final class RawTimingMetric {
        int count;
        double min;
        double max;
        double mean;
        double sum;
    }

    static final class RawInputs {
        List<RawTypedKey> typedKeys;
    }

    static final class RawTypedKey {
        String key;
        String code;
        boolean ctrlKey;
        boolean altKey;
        boolean shiftKey;
        boolean metaKey;
        boolean repeat;
    }
}
