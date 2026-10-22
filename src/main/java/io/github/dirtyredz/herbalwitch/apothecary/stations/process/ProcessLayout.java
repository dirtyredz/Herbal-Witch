package io.github.dirtyredz.herbalwitch.apothecary.stations.process;

import java.util.stream.IntStream;

/**
 * Which inventory slots of a station play which part in a recipe. {@code -1} marks a part the
 * station does not have.
 */
public record ProcessLayout(int[] inputs, int base, int packaging, int output, int returns) {
    public ProcessLayout {
        inputs = inputs.clone();
    }

    @Override
    public int[] inputs() {
        return inputs.clone();
    }

    /** A layout derived from the inventory's roles (first BASE, PACKAGING, OUTPUT and RETURN slot). */
    public static ProcessLayout of(ProcessInventory inventory) {
        return new ProcessLayout(inventory.slots(ProcessInventory.Role.INPUT),
                first(inventory, ProcessInventory.Role.BASE), first(inventory, ProcessInventory.Role.PACKAGING),
                first(inventory, ProcessInventory.Role.OUTPUT), first(inventory, ProcessInventory.Role.RETURN));
    }

    private static int first(ProcessInventory inventory, ProcessInventory.Role role) {
        return IntStream.of(inventory.slots(role)).findFirst().orElse(-1);
    }
}
