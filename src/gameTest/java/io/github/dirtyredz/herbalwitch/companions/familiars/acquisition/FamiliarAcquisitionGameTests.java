package io.github.dirtyredz.herbalwitch.companions.familiars.acquisition;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarBinding;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.List;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarAcquisitionGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_acquisition")
    public static void allThreeSpeciesHaveSafeDaytimeSpawnsAndUnboundCreativeEggs(GameTestHelper h) {
        floor(h); long time = h.getLevel().getDayTime(); h.getLevel().setDayTime(6000);
        h.setBlock(new BlockPos(4,3,4), Blocks.GLOWSTONE);
        h.runAfterDelay(10, () -> { try {
            var pos = h.absolutePos(new BlockPos(4,1,4)); h.setBlock(new BlockPos(4,0,4), Blocks.GRASS_BLOCK);
            for (String species : List.of("raven", "owl", "snake")) {
                var type = FamiliarEntities.type(species);
                check(SpawnPlacements.checkSpawnRules(type, h.getLevel(), MobSpawnType.NATURAL, pos, h.getLevel().random), "daytime acquisition refused: " + species + " light=" + h.getLevel().getRawBrightness(pos,0) + " ground=" + h.getLevel().getBlockState(pos.below()));
                var egg = FamiliarItems.egg(species); check(egg instanceof net.minecraft.world.item.SpawnEggItem, "missing creative egg");
                var mob = type.create(h.getLevel()); mob.moveTo(pos.getCenter()); check(h.getLevel().addFreshEntity(mob), "wild insertion refused");
                check(FamiliarBinding.of(mob).orElseThrow().reference().isEmpty() && !mob.isNoAi(), "wild animal received a bond/disabled AI"); mob.discard();
                check(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(h.getLevel().registryAccess()).is(egg)), "survival egg recipe exists");
            }
            h.setBlock(new BlockPos(4,0,4), Blocks.AIR);
            check(!SpawnPlacements.checkSpawnRules(FamiliarEntities.RAVEN.get(), h.getLevel(), MobSpawnType.NATURAL, pos, h.getLevel().random), "unsupported air spawn accepted");
            h.succeed();
        } finally { h.getLevel().setDayTime(time); } });
    }
}
