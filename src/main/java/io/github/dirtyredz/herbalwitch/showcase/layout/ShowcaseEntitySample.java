package io.github.dirtyredz.herbalwitch.showcase.layout;

/** Pure descriptor, never an entity instance or executable initializer. All samples start unowned and adult. */
public record ShowcaseEntitySample(String type, double x, double y, double z) {
    public ShowcaseEntitySample {
        if (!type.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+") || type.length() > 256 || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("Invalid showcase entity sample");
    }
    public ShowcaseEntitySample offset(int dx, int dz) { return new ShowcaseEntitySample(type, x + dx, y, z + dz); }
}
