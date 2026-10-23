package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.action.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.sensing.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.PrincipalPermission;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarPowerCoverageGameTests {
    private static void sense(GameTestHelper h, String species, FamiliarPower power) {
        FamiliarFixtures.bound(h, species, "sense_" + species + UUID.randomUUID(), b -> {
            if (species.equals("bat")) for (int x=0;x<9;x++) for(int y=1;y<=4;y++) for(int z=0;z<9;z++) if(x==0||x==8||z==0||z==8||y==4) h.setBlock(new BlockPos(x,y,z), Blocks.STONE);
            Runnable sense = () -> { try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                // This exercises sensing, not random wandering or a light-engine timing assumption.
                b.mob().getNavigation().stop(); b.mob().moveTo(h.absolutePos(new BlockPos(4,1,5)).getCenter());
                b.mob().setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);

                // One test per sensing batch: remove completed neighboring fixtures from the bounded query.
                int radius = io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig.current().senseRadius();
                h.getLevel().getEntities(b.mob(), b.mob().getBoundingBox().inflate(radius), e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
                var victim = h.spawn(EntityType.PILLAGER, new BlockPos(6,1,5)); victim.setNoAi(true);
                var task = new FamiliarSenseTask(b.mob(), power, new FamiliarHerbCursor());
                check(task.valid() && b.mob().hasLineOfSight(victim), species + " sensing fixture is not ready or visible");
                task.perform();
                check(b.record().state().getCompound("powerCooldowns").contains(power.key()), species + " sensing produced no paid result");
                check(WitchStatusService.energyUnits(b.owner()) == 7200, species + " sensing payment");
                h.succeed();
            } };
            if (species.equals("bat")) h.startSequence().thenWaitUntil(() -> check(h.getLevel().getMaxLocalRawBrightness(h.absolutePos(new BlockPos(6,1,5))) <= 7, "bat fixture awaits darkness")).thenExecute(sense);
            else h.runAfterDelay(10, sense);
        });
    }
    @GameTest(template=TEMPLATE,batch="herbalwitch_cat_sense",timeoutTicks=100)
    public static void catSensesHostile(GameTestHelper h) { sense(h,"cat",FamiliarPower.THREATS); }
    @GameTest(template=TEMPLATE,batch="herbalwitch_wolf_sense",timeoutTicks=100)
    public static void wolfTracksHostile(GameTestHelper h) { sense(h,"wolf",FamiliarPower.TRACK); }
    @GameTest(template=TEMPLATE,batch="herbalwitch_snake_sense",timeoutTicks=100)
    public static void snakeSensesCreature(GameTestHelper h) { sense(h,"snake",FamiliarPower.CREATURES); }
    @GameTest(template=TEMPLATE,batch="herbalwitch_bat_sense",timeoutTicks=100)
    public static void batSensesDarkCreature(GameTestHelper h) { sense(h,"bat",FamiliarPower.CREATURES); }
    @GameTest(template=TEMPLATE,batch="herbalwitch_owl_herbs",timeoutTicks=100)
    public static void owlFindsMatureHerb(GameTestHelper h) {
        FamiliarFixtures.bound(h,"owl","owl_herbs_"+UUID.randomUUID(), b -> {
            try(var online=OnlinePlayers.register(List.of(b.owner()))) {
                var pos=b.mob().blockPosition().east(); h.getLevel().setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());
                h.getLevel().setBlockAndUpdate(pos,BotanyBlocks.plant(HerbCatalog.SAGE).getStateForAge(3));
                var task=new FamiliarSenseTask(b.mob(),FamiliarPower.HERBS,new FamiliarHerbCursor());
                for(int turn=0;turn<200 && !task.done();turn++) task.perform();
                check(WitchStatusService.energyUnits(b.owner())==7200,"owl herb sensing produced no result"); h.succeed();
            }
        });
    }
    @GameTest(template=TEMPLATE,batch="herbalwitch_rabbit_garden",timeoutTicks=100)
    public static void rabbitRechecksGardenPermissionAndZeroEnergy(GameTestHelper h) {
        FamiliarFixtures.bound(h,"rabbit","garden_"+UUID.randomUUID(), b -> {
            try(var online=OnlinePlayers.register(List.of(b.owner()))) {
                var pos=b.mob().blockPosition().east(); h.getLevel().setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());
                var young=BotanyBlocks.plant(HerbCatalog.SAGE).getStateForAge(0); h.getLevel().setBlockAndUpdate(pos,young);
                boolean[] denied={true}; PrincipalPermission.register((level,owner,at)->denied[0] && owner.equals(b.owner().getUUID()) && at.equals(pos) ? Denial.CLAIMS:Denial.NONE);
                try {
                    var before=b.record();
                    check(!FamiliarGardenAid.apply(b.mob(),pos),"garden permission bypassed");
                    check(b.record().equals(before) && h.getLevel().getBlockState(pos).equals(young) && WitchStatusService.energyUnits(b.owner())==7500,"denied garden mutated state");
                    denied[0]=false;
                    check(FamiliarGardenAid.apply(b.mob(),pos) && !h.getLevel().getBlockState(pos).equals(young),"garden produced no growth");
                    check(WitchStatusService.energyUnits(b.owner())==7000,"garden cost");
                    check(WitchStatusService.spend(b.owner(),7000,()->true),"zero energy fixture");
                    var store=FamiliarStore.get(b.owner().server); store.update(b.record().change(s->s.remove("powerCooldowns")),false);
                    h.getLevel().setBlockAndUpdate(pos,young); before=b.record();
                    check(!FamiliarGardenAid.apply(b.mob(),pos) && b.record().equals(before) && h.getLevel().getBlockState(pos).equals(young),"zero energy garden changed state");
                } finally { denied[0]=false; }
                h.succeed();
            }
        });
    }
    @GameTest(template=TEMPLATE,batch="herbalwitch_offline_reserve_exhaustion",timeoutTicks=100)
    public static void loadedOfflineHomeGuardSpendsOnlyFiniteReserve(GameTestHelper h) {
        FamiliarFixtures.bound(h,"wolf","offline_reserve_"+UUID.randomUUID(), b -> {
            var homePos=new BlockPos(3,1,4);
            check(PlayerFixtures.placeOnFloor(h,b.owner(),FamiliarHomes.get(FamiliarHomeKind.BED).item().get(),homePos).consumesAction(),"bed fixture");
            var home=(FamiliarHomeBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(homePos));
            b.owner().getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarHomeService.assign(b.owner(),home,b.bond()).equals("home_assigned"),"guard home assignment");
            var store=FamiliarStore.get(b.owner().server); store.update(b.record().change(s->{s.putString("command","protect_home");s.putInt("reserve",800);}),false);
            FamiliarOfflineGuards.observe(b.mob(),b.record());
            var threat=h.spawn(EntityType.PILLAGER,new BlockPos(6,1,5)); threat.setNoAi(true);
            check(FamiliarCombatPowers.intercept(b.mob(),threat),"offline interception refused");
            check(FamiliarCombatPowers.repel(b.mob(),threat),"offline repel refused");
            check(b.record().state().getInt("reserve")==0 && WitchStatusService.energyUnits(b.owner())==7500,"reserve conservation");
            store.update(b.record().change(s->{s.remove("powerCooldowns");s.remove("interceptUntil");}),false);
            var before=b.record();var motion=threat.getDeltaMovement();
            check(!FamiliarCombatPowers.repel(b.mob(),threat) && b.record().equals(before) && threat.getDeltaMovement().equals(motion),"exhausted reserve produced effect");
            h.succeed();
        });
    }
    @GameTest(template=TEMPLATE,batch="herbalwitch_snake_venom",timeoutTicks=100)
    public static void snakeVenomUsesOnePaidGuardEffect(GameTestHelper h) {
        FamiliarFixtures.bound(h,"snake","snake_venom_"+UUID.randomUUID(), b -> {
            try(var online=OnlinePlayers.register(List.of(b.owner()))) {
                FamiliarStore.get(b.owner().server).update(b.record().change(s->s.putString("command","guard_witch")),false);
                var threat=h.spawn(EntityType.PILLAGER,new BlockPos(5,1,5));threat.setNoAi(true);
                check(FamiliarCombatPowers.venom(b.mob(),threat),"guard venom refused");
                check(threat.hasEffect(net.minecraft.world.effect.MobEffects.POISON) && WitchStatusService.energyUnits(b.owner())==7100,"venom effect/payment");
                check(!FamiliarCombatPowers.venom(b.mob(),threat),"venom cooldown bypass");h.succeed();
            }
        });
    }
}
