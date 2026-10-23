package io.github.dirtyredz.herbalwitch.showcase.layout;

/** Pure descriptor, never an entity instance or executable initializer. All samples start unowned and adult. */
public record ShowcaseEntitySample(String type, double x, double y, double z, boolean inert, int appearance) {
    public ShowcaseEntitySample(String type, double x, double y, double z) { this(type, x, y, z, false, 0); }
    public ShowcaseEntitySample {
        if (appearance < 0 || appearance > 1 || !type.matches("[a-z0-9_.-]+:[a-z0-9_/.-]+") || type.length() > 256 || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("Invalid showcase entity sample");
    }
    public ShowcaseEntitySample offset(int dx, int dz) { return new ShowcaseEntitySample(type, x + dx, y, z + dz, inert, appearance); }
}
