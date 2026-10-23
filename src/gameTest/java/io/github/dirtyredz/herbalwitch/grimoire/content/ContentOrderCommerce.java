package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.ArrayList;
import java.util.List;

/** Independent M11 additions: ten commerce guides at priority 1300, after the M10 tool guides. */
final class ContentOrderCommerce {
    static final List<String> ADDITIONS = List.of("setup", "stocking", "pricing", "metadata", "buying", "proceeds", "orders", "villagers", "offline", "recovery")
            .stream().map(topic -> "guide|herbalwitch:commerce/" + topic).toList();

    private ContentOrderCommerce() {
    }

    static List<String> expected() {
        var out = new ArrayList<>(ContentOrderTools.expected());
        out.addAll(ADDITIONS);
        return List.copyOf(out);
    }
}
