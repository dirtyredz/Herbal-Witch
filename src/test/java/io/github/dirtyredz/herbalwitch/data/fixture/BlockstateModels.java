package io.github.dirtyredz.herbalwitch.data.fixture;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a generated blockstate file the way the game does, for tests: a variants file picks
 * the variant whose properties all match; a multipart file applies every part whose {@code when}
 * holds ({@code a|b} alternatives, {@code OR} lists). Returns the applied models with their y turn.
 */
public final class BlockstateModels {
    /** One applied model and its blockstate y rotation. */
    public record Applied(String model, int y) {
    }

    private BlockstateModels() {
    }

    public static List<Applied> select(JsonObject blockstate, Map<String, String> props) {
        List<Applied> out = new ArrayList<>();
        if (blockstate.has("variants")) {
            for (Map.Entry<String, JsonElement> v : blockstate.getAsJsonObject("variants").entrySet()) {
                if (v.getKey().isEmpty() || Arrays.stream(v.getKey().split(",")).allMatch(kv -> {
                    String[] p = kv.split("=");
                    return p[1].equals(props.get(p[0]));
                })) {
                    out.add(applied(v.getValue()));
                    return out;
                }
            }
            return out;
        }
        for (JsonElement part : blockstate.getAsJsonArray("multipart")) {
            JsonObject p = part.getAsJsonObject();
            if (!p.has("when") || holds(p.getAsJsonObject("when"), props)) {
                out.add(applied(p.get("apply")));
            }
        }
        return out;
    }

    private static Applied applied(JsonElement apply) {
        JsonObject a = apply.isJsonArray() ? apply.getAsJsonArray().get(0).getAsJsonObject() : apply.getAsJsonObject();
        return new Applied(a.get("model").getAsString(), a.has("y") ? a.get("y").getAsInt() : 0);
    }

    private static boolean holds(JsonObject when, Map<String, String> props) {
        if (when.has("OR")) {
            for (JsonElement w : when.getAsJsonArray("OR")) {
                if (holds(w.getAsJsonObject(), props)) {
                    return true;
                }
            }
            return false;
        }
        for (Map.Entry<String, JsonElement> e : when.entrySet()) {
            if (!Arrays.asList(e.getValue().getAsString().split("\\|")).contains(props.get(e.getKey()))) {
                return false;
            }
        }
        return true;
    }
}
