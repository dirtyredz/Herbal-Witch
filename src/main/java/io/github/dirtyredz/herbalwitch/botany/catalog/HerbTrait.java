package io.github.dirtyredz.herbalwitch.botany.catalog;

/**
 * Fictional ingredient properties of a herb. Recipe and grimoire metadata only: a trait never
 * changes what a raw herb does in the world (raw toxic herbs are simply not food).
 */
public enum HerbTrait {
    RESTORATIVE("restorative", "Restorative"),
    CALMING("calming", "Calming"),
    TOXIC("toxic", "Toxic"),
    PROTECTIVE("protective", "Protective"),
    GROWTH("growth", "Growth"),
    ATTRACTION("attraction", "Attraction"),
    BINDING("binding", "Binding"),
    SIGHT("sight", "Sight"),
    SPEED("speed", "Speed"),
    CONCEALMENT("concealment", "Concealment");

    private final String id;
    private final String displayName;

    HerbTrait(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }
}
