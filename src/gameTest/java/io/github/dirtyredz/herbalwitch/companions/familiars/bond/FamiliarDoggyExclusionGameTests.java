package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.CaptureService;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureSavedData;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** Uses the actual optional mod's registered dog without linking any of its classes. */
@GameTestHolder("herbalwitch")
public final class FamiliarDoggyExclusionGameTests {
    @GameTestGenerator public static Collection<TestFunction> optionalDog() {
        if(!ModList.get().isLoaded("doggytalents"))return List.of();
        return List.of(new TestFunction("herbalwitch_doggy_exclusion","familiar_doggy_exclusion","herbalwitch:empty",100,0,true,FamiliarDoggyExclusionGameTests::verify));
    }
    private static void verify(GameTestHelper h) {
        floor(h);FamiliarFixtures.whenReady(h,()->{
            var owner=AnimalFixtures.realPlayer(h,"doggy_exclusion_"+UUID.randomUUID());
            check(WitchStatusService.convert(owner,UUID.randomUUID()),"doggy owner fixture");
            var type=ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse("doggytalents:dog"));
            check(type!=null,"actual Doggy Talents entity type missing");
            var entity=type.create(h.getLevel());check(entity instanceof Mob,"actual Doggy Talents entity missing");
            var dog=(Mob)entity;dog.moveTo(h.absolutePos(new BlockPos(4,1,5)).getCenter());
            if(dog instanceof TamableAnimal pet){pet.setTame(true);pet.setOwnerUUID(owner.getUUID());}
            check(h.getLevel().addFreshEntity(dog),"doggy insertion");
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AnimalItems.CHARM.get()));
            var stack=owner.getMainHandItem().copy();float health=dog.getHealth();
            check("species".equals(FamiliarEligibility.refusal(owner,dog,h.absolutePos(new BlockPos(4,1,4)))),"foreign trained dog accepted for binding");
            check("unsupported".equals(CaptureService.capture(owner,dog,InteractionHand.MAIN_HAND)),"foreign trained dog accepted for capture");
            check(ItemStack.matches(stack,owner.getMainHandItem()) && dog.isAlive() && dog.getHealth()==health
                    && WitchStatusService.energyUnits(owner)==10000 && FamiliarStore.get(owner.server).creature(dog.getUUID()).isEmpty()
                    && CaptureSavedData.get(owner.server).record(dog.getUUID()).isEmpty(),"foreign dog refusal mutated state");
            dog.discard();h.succeed();
        });
    }
}
