package io.github.dirtyredz.herbalwitch.companions.familiars.persistence;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureReconciliation;
import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.FamiliarCaptureInteropGameTests;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeTarget;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.nio.file.Path;
import java.util.UUID;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** Real transport, owner death and unloaded-home state survive the process boundary together. */
final class FamiliarRestartLifecycle {
    static void prepare(GameTestHelper h,Path root,Runnable done) {
        FamiliarCaptureInteropGameTests.bindReleased(h,b->{
            var store=FamiliarStore.get(b.owner().server);
            b.owner().getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
            check(FamiliarCommands.set(b.owner(),b.bond(),FamiliarCommand.FOLLOW).equals("command_set"),"restart lifecycle follow");
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(b.owner()));
            check(store.record(b.bond()).orElseThrow().command()==FamiliarCommand.STAY,"logout retained mobile command");
            // The headless connection has no negotiated channel for unrelated login sync packets.
            var login=new PlayerEvent.PlayerLoggedInEvent(b.owner());
            FamiliarCommands.login(login);
            io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt.login(login);
            check(store.mobile().count(b.owner().getUUID())==0,"reconnect resumed mobile command");
            check(FamiliarCommands.set(b.owner(),b.bond(),FamiliarCommand.GUARD_WITCH).equals("command_set"),"restart lifecycle guard");
            b.owner().hurt(b.owner().damageSources().genericKill(),Float.MAX_VALUE);
            check(!b.owner().isAlive() && store.record(b.bond()).orElseThrow().command()==FamiliarCommand.STAY,"owner death retained guard");
            var home=new CompoundTag();var pos=new BlockPos(20_000_000,80,20_000_000);
            home.putString("dimension",h.getLevel().dimension().location().toString());home.putLong("position",pos.asLong());home.putUUID("instance",UUID.randomUUID());
            check(!h.getLevel().hasChunkAt(pos) && store.update(store.record(b.bond()).orElseThrow().change(s->s.put("home",home)),true),"restart unloaded home fixture");
            var tag=new CompoundTag();tag.putUUID("bond",b.bond());tag.put("token",b.oldToken().save(new CompoundTag()));tag.put("source",b.oldSource());
            var live=new CompoundTag();b.familiar().save(live);tag.put("entity",live);tag.put("home",home);
            try {NbtIo.writeCompressed(tag,root.resolve("capture-lifecycle.nbt").toFile());}
            catch(java.io.IOException failure){throw new java.io.UncheckedIOException(failure);}
            done.run();
        });
    }
    static void verify(GameTestHelper h,Path root) throws java.io.IOException {
        var tag=NbtIo.readCompressed(root.resolve("capture-lifecycle.nbt").toFile());
        var record=FamiliarStore.get(h.getLevel().getServer()).record(tag.getUUID("bond")).orElseThrow();
        check(record.phase()==FamiliarRecord.Phase.ACTIVE && record.command()==FamiliarCommand.STAY,"owner lifecycle changed bond after restart");
        var player=AnimalFixtures.realPlayer(h,"restart_token_"+UUID.randomUUID());
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.of(tag.getCompound("token")));
        check("token".equals(CaptureService.release(player,InteractionHand.MAIN_HAND,h.absolutePos(new BlockPos(5,0,5)),Direction.UP)),"old M8 token became usable after restart");
        var source=EntityType.loadEntityRecursive(tag.getCompound("source"),h.getLevel(),e->e);
        check(source!=null && !h.getLevel().addFreshEntity(source),"stale M8 source admitted after restart");
        var mob=FamiliarReconciliation.loaded(h.getLevel().getServer(),record);
        if(mob==null){mob=(Mob)EntityType.loadEntityRecursive(tag.getCompound("entity"),h.getLevel(),e->e);check(mob!=null && h.getLevel().addFreshEntity(mob),"released-generation familiar did not reload");}
        check(CaptureReconciliation.generation(mob)==1,"released generation lost across restart");
        var pos=BlockPos.of(tag.getCompound("home").getLong("position"));
        check(!h.getLevel().hasChunkAt(pos) && FamiliarHomeTarget.resolve(mob,record).reason().equals("home_unloaded")
                && !h.getLevel().hasChunkAt(pos) && record.state().getCompound("home").equals(tag.getCompound("home")),"restart loaded or forgot pending home");
    }
}
