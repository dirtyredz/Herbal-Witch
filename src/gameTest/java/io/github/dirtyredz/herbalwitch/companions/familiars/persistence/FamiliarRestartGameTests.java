package io.github.dirtyredz.herbalwitch.companions.familiars.persistence;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarRecall;
import io.github.dirtyredz.herbalwitch.companions.familiars.equipment.FamiliarEquipmentInventory;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarDetailMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.recovery.FamiliarRevival;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarItems;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarOffering;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.GameTestHolder;
import java.nio.file.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch_restart")
public final class FamiliarRestartGameTests {
    private static FamiliarRestartCapture capture;
    @GameTestGenerator public static Collection<TestFunction> restart() {
        String phase=System.getProperty("herbalwitch.restart.phase","");
        if(phase.isEmpty()) return List.of();
        if(!Set.of("prepare","verify").contains(phase)) throw new IllegalArgumentException("Unknown restart phase");
        return List.of(new TestFunction("familiar_restart","familiar_restart."+phase,"herbalwitch_restart:empty",600,0,true,
                h -> {if(phase.equals("prepare"))prepare(h);else FamiliarRestartReplay.verify(h,evidence());}));
    }
    static Path evidence() { return Path.of(System.getProperty("herbalwitch.restart.evidence")).toAbsolutePath(); }
    @AfterBatch(batch="familiar_restart") public static void cleanup(ServerLevel level) { if(capture!=null){capture.close();capture=null;} }
    private static void prepare(GameTestHelper h) {
        capture=new FamiliarRestartCapture(evidence(),h.getLevel().getServer());
        FamiliarFixtures.bound(h,"raven","restart_"+UUID.randomUUID(),b -> {
            b.owner().getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
            b.owner().getInventory().setItem(9,new ItemStack(FamiliarItems.gear("raven_satchel")));
            var menu=new FamiliarDetailMenu(40,b.owner().getInventory(),24L,b.bond());b.owner().containerMenu=menu;
            menu.clicked(2,0,ClickType.QUICK_MOVE,b.owner());
            check(FamiliarEquipmentInventory.item(b.record(),1).is(FamiliarItems.gear("raven_satchel")),"restart equipment transfer");
            b.owner().closeContainer();
            h.runAfterDelay(6,()->{
                check(FamiliarRecall.recall(b.owner(),b.bond()).equals("recalled"),"restart real transfer");
                var mob=FamiliarReconciliation.loaded(b.owner().server,b.record());
                check(mob!=null,"restart transferred entity"); mob.hurt(mob.damageSources().generic(),1000);
                check(b.record().phase()==FamiliarRecord.Phase.INCAPACITATED,"restart incapacitation");
                h.runAfterDelay(6,()->{
                    check(FamiliarRevival.revive(b.owner(),b.bond(),h.absolutePos(new BlockPos(4,1,4)),AltarOffering.none()),"restart real revival");
                    h.runAfterDelay(6,()->pickup(h,b));
                });
            });
        });
    }
    private static void pickup(GameTestHelper h,FamiliarFixtures.Bound b) {
        var mob=FamiliarReconciliation.loaded(b.owner().server,b.record());check(mob!=null,"revived entity missing");
        var pos=mob.position().add(1,0,0);var item=new ItemEntity(h.getLevel(),pos.x,pos.y,pos.z,new ItemStack(Items.STICK,8));
        item.setNoPickUpDelay();item.setThrower(b.owner().getUUID());check(h.getLevel().addFreshEntity(item),"restart dropped item");
        var oldItem=new CompoundTag();item.save(oldItem);
        try {NbtIo.writeCompressed(oldItem,evidence().resolve("stale-item.nbt").toFile());}
        catch(java.io.IOException failure){throw new java.io.UncheckedIOException(failure);}
        try(var online=OnlinePlayers.register(List.of(b.owner()))) {
            check(FamiliarRetrieveItem.apply(b.owner(),mob,item,item.getItem().save(new CompoundTag())),"restart real pickup");
        }
        check(FamiliarEquipmentInventory.cargo(b.record()).getCount()==8,"restart pickup cargo");
        h.runAfterDelay(6,()->{
            var menu=new FamiliarDetailMenu(40,b.owner().getInventory(),25L,b.bond());b.owner().containerMenu=menu;
            check(menu.collectCargo(b.owner()).equals("cargo_collected"),"restart real cargo extraction");b.owner().closeContainer();
            check(b.owner().getInventory().countItem(Items.STICK)==8,"restart cargo conservation");
            h.runAfterDelay(6,()->{
                capture.failNext=true;
                check(!FamiliarReserve.charge(b.owner(),b.bond()).equals("reserve_charged"),"injected final write succeeded");
                check(!FamiliarStore.get(b.owner().server).writable(b.bond()),"write failure did not make shard read-only");
                capture.finish();FamiliarRestartExtra.prepare(evidence());h.succeed();
            });
        });
    }
}
