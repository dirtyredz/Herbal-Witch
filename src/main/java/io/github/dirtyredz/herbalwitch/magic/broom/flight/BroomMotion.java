package io.github.dirtyredz.herbalwitch.magic.broom.flight;

/** Registry-free movement math in blocks/tick and degrees, shared by the server and tests. */
public final class BroomMotion {
    public record Horizontal(double x, double z) { }

    private BroomMotion() { }

    public static Horizontal target(int forward, int sideways, double yaw, double blocksPerSecond) {
        if (forward < -1 || forward > 1 || sideways < -1 || sideways > 1 || !Double.isFinite(yaw)
                || !Double.isFinite(blocksPerSecond) || blocksPerSecond < 1 || blocksPerSecond > 16) {
            throw new IllegalArgumentException("Invalid flight motion");
        }
        double length = Math.max(1, Math.hypot(forward, sideways));
        double scale = blocksPerSecond / (20 * length);
        double radians = Math.toRadians(yaw);
        return new Horizontal((-Math.sin(radians) * forward + Math.cos(radians) * sideways) * scale,
                (Math.cos(radians) * forward + Math.sin(radians) * sideways) * scale);
    }

    public static Horizontal accelerate(Horizontal current, Horizontal target, double blocksPerSecondSquared) {
        if (!Double.isFinite(blocksPerSecondSquared) || blocksPerSecondSquared < 1 || blocksPerSecondSquared > 20
                || !Double.isFinite(current.x) || !Double.isFinite(current.z)
                || !Double.isFinite(target.x) || !Double.isFinite(target.z)) {
            throw new IllegalArgumentException("Invalid acceleration");
        }
        double dx = target.x - current.x, dz = target.z - current.z;
        double distance = Math.hypot(dx, dz);
        double step = blocksPerSecondSquared / 400;
        if (distance <= step) { return target; }
        return new Horizontal(current.x + dx / distance * step, current.z + dz / distance * step);
    }

    public static double turn(double current, double target, double degreesPerSecond) {
        if (!Double.isFinite(current) || !Double.isFinite(target) || !Double.isFinite(degreesPerSecond)
                || degreesPerSecond < 30 || degreesPerSecond > 180) {
            throw new IllegalArgumentException("Invalid turn");
        }
        double delta = ((target - current) % 360 + 540) % 360 - 180;
        double step = degreesPerSecond / 20;
        return current + Math.max(-step, Math.min(step, delta));
    }
}
