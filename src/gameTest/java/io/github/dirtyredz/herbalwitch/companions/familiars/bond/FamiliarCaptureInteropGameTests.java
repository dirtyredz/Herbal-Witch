package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationEffects;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarCaptureInteropGameTests {
    private static io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers.Registration online;
    @AfterBatch(batch="herbalwitch_familiar_dimension")
    public static void cleanup(net.minecraft.server.level.ServerLevel level){if(online!=null){online.close();online=null;}}
    @GameTest(template=TEMPLATE,batch="herbalwitch_familiar_dimension",timeoutTicks=180)
    public static void ownerChangesDimensionAndExplicitRecallTransfersExactlyOneIncarnation(GameTestHelper h) {
        FamiliarFixtures.bound(h,"cat","dimension_"+UUID.randomUUID(),b->{
            var owner=b.owner();var destination=owner.server.getLevel(net.minecraft.world.level.Level.NETHER);
            check(destination!=null,"nether fixture unavailable");
            // The test explicitly prepares one destination chunk; familiar code may not load terrain.
            for(int x=4;x<=12;x++)for(int z=4;z<=12;z++)for(int y=119;y<=123;y++)
                destination.setBlock(new BlockPos(x,y,z),(y==119?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
            owner.getInventory().setItem(0,new ItemStack(io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems.BOOK.get()));
            online=io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers.register(List.of(owner));
            check(io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommands.set(owner,b.bond(),FamiliarCommand.FOLLOW).equals("command_set"),"dimension follow");
            owner.teleportTo(destination,8.5,120,8.5,0,0);
            h.runAfterDelay(10,()->{
                check(b.mob().level()==h.getLevel() && !b.mob().isRemoved() && b.record().state().getString("status").equals("other_dimension"),"dimension change silently moved familiar");
                String recalled=io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarRecall.recall(owner,b.bond());
                check(recalled.equals("recalled"),"cross-dimension recall refused: "+recalled+", owner="+owner.position()+", energy="+WitchStatusService.energyUnits(owner));
                var current=destination.getEntity(b.record().creature());
                check(b.mob().isRemoved() && h.getLevel().getEntity(b.mob().getUUID())==null && current instanceof Mob
                        && b.record().generation()==2 && WitchStatusService.energyUnits(owner)==7000,"cross-dimension identity/payment mismatch");
                current.discard();owner.teleportTo(h.getLevel(),h.absolutePos(new BlockPos(4,1,3)).getX()+.5,h.absolutePos(new BlockPos(4,1,3)).getY(),h.absolutePos(new BlockPos(4,1,3)).getZ()+.5,0,0);
                h.succeed();
            });
        });
    }
    public record ReleasedBond(ServerPlayer owner,Mob familiar,UUID bond,ItemStack oldToken,CompoundTag oldSource) {}
    @GameTest(template=TEMPLATE,batch="herbalwitch_familiar_capture_interop",timeoutTicks=180)
    public static void releasedAnimalBindsButOldTokenAndSourceCannotRecreateIt(GameTestHelper h) {
        bindReleased(h,b->{
            var record=FamiliarStore.get(b.owner().server).record(b.bond()).orElseThrow();
            b.owner().setItemInHand(InteractionHand.MAIN_HAND,b.oldToken().copy());
            check("token".equals(CaptureService.release(b.owner(),InteractionHand.MAIN_HAND,h.absolutePos(new BlockPos(5,0,5)),Direction.UP)),"old M8 token recreated bound creature");
            var old=EntityType.loadEntityRecursive(b.oldSource(),h.getLevel(),e->e);check(old!=null && !h.getLevel().addFreshEntity(old),"old native source chunk admitted");
            b.owner().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AnimalItems.CHARM.get()));
            check("unsupported".equals(CaptureService.capture(b.owner(),b.familiar(),InteractionHand.MAIN_HAND)),"bound familiar entered capture charm");
            check(FamiliarStore.get(b.owner().server).record(b.bond()).orElseThrow().equals(record) && WitchStatusService.energyUnits(b.owner())==7500
                    && h.getLevel().getEntity(record.creature())==b.familiar(),"interop denial changed canonical authority");h.succeed();
        });
    }
    /** The restart harness uses the same actual transport-to-bond path, retaining its old token bytes. */
    public static void bindReleased(GameTestHelper h,Consumer<ReleasedBond> done) {
        floor(h);var owner=AnimalFixtures.realPlayer(h,"m9_interop_"+UUID.randomUUID());
        check(WitchStatusService.convert(owner,UUID.randomUUID()),"interop witch setup");
        var cat=h.spawn(EntityType.CAT,new BlockPos(4,1,5));cat.setAge(0);cat.setTame(true);cat.setOwnerUUID(owner.getUUID());
        AnimalFixtures.relationship(cat,owner.getUUID(),100,5);cat.addEffect(new MobEffectInstance(PreparationEffects.SLEEP.get(),100));
        var oldSource=new CompoundTag();cat.save(oldSource);
        AnimalFixtures.afterTransportInterval(h,()->{
            owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(AnimalItems.CHARM.get()));
            String reason=CaptureService.capture(owner,cat,InteractionHand.MAIN_HAND);check(reason==null,"interop capture: "+reason);
            var token=owner.getMainHandItem().copy();
            AnimalFixtures.afterTransportInterval(h,()->{
                reasonRelease(h,owner);var released=(Mob)h.getLevel().getEntity(cat.getUUID());check(released!=null,"interop released source missing");
                released.removeEffect(PreparationEffects.SLEEP.get());released.setTarget(null);
                h.runAfterDelay(6,()->FamiliarFixtures.whenReady(h,()->{
                    released.moveTo(h.absolutePos(new BlockPos(4,1,5)).getCenter());
                    var altar=h.absolutePos(new BlockPos(4,1,4));check(FamiliarEligibility.refusal(owner,released,altar)==null,"released source ineligible");
                    check(FamiliarBindingService.bind(owner,released,altar,AltarOffering.none()),"released generation binding refused");
                    var record=FamiliarStore.get(owner.server).creature(cat.getUUID()).orElseThrow();var familiar=(Mob)h.getLevel().getEntity(cat.getUUID());
                    check(familiar!=null && CaptureReconciliation.generation(familiar)==1,"binding lost released transport generation");
                    done.accept(new ReleasedBond(owner,familiar,record.bond(),token,oldSource));
                }));
            });
        });
    }
    private static void reasonRelease(GameTestHelper h,ServerPlayer owner){String reason=CaptureService.release(owner,InteractionHand.MAIN_HAND,h.absolutePos(new BlockPos(5,0,5)),Direction.UP);check(reason==null,"interop release: "+reason);}
}
