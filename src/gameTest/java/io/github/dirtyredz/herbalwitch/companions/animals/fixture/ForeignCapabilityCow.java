package io.github.dirtyredz.herbalwitch.companions.animals.fixture;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

/** Shares vanilla tags but deliberately has no tested adapter. Never packaged in the release jar. */
public final class ForeignCapabilityCow extends Cow {
    public ForeignCapabilityCow(Level level) { super(EntityType.COW, level); }
}
