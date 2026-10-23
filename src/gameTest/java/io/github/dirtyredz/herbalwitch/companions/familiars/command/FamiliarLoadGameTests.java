package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarBinding;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWork;
import io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarLoadGameTests {
    private static OnlinePlayers.Registration online;
    @AfterBatch(batch="herbalwitch_familiar_load") public static void cleanup(ServerLevel level){if(online!=null){online.close();online=null;}}
    @GameTest(template=TEMPLATE,batch="herbalwitch_familiar_load",timeoutTicks=180)
    public static void paginatedRosterAndManyOwnersShareOneWorkBudget(GameTestHelper h) {
        floor(h);FamiliarFixtures.whenReady(h,()->{
            var store=FamiliarStore.get(h.getLevel().getServer());var owners=new ArrayList<ServerPlayer>();var loaded=new ArrayList<Mob>();
            for(int ownerIndex=0;ownerIndex<3;ownerIndex++) {
                var owner=AnimalFixtures.realPlayer(h,"load_"+UUID.randomUUID());owners.add(owner);
                owner.getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
                for(int i=0;i<40;i++) {
                    var record=record(h,owner);check(store.create(record),"load fixture record");
                    if(i<3) {
                        loaded.add(spawn(h,record,i,ownerIndex));
                        check(FamiliarCommands.set(owner,record.bond(),FamiliarCommand.FOLLOW).equals("command_set"),"owner cap fixture refused");
                    }
                }
                check(store.mobile().count(owner.getUUID())==3,"per-owner mobile cap not filled");
                var page1=store.roster().page(owner.getUUID(),null,32);var page2=store.roster().page(owner.getUUID(),page1.get(31),32);
                check(page1.size()==32 && page2.size()==8 && Collections.disjoint(page1,page2)
                        && store.roster().page(owner.getUUID(),page2.get(7),32).isEmpty(),"large roster pagination leaked or skipped records");
            }
            online=OnlinePlayers.register(owners);
            for(int i=0;i<3;i++) {
                var owner=AnimalFixtures.realPlayer(h,"offline_load_"+UUID.randomUUID());
                owner.getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
                var record=record(h,owner);check(store.create(record),"offline guard record");loaded.add(spawn(h,record,i,3));
                var pos=new BlockPos(2+i*2,1,7);
                check(PlayerFixtures.placeOnFloor(h,owner,FamiliarHomes.get(FamiliarHomeKind.BED).item().get(),pos).consumesAction(),"load home placement");
                owner.getInventory().setItem(0,new ItemStack(GrimoireItems.BOOK.get()));
                var home=(FamiliarHomeBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));
                check(FamiliarHomeService.assign(owner,home,record.bond()).equals("home_assigned"),"load home assignment");
                check(FamiliarCommands.set(owner,record.bond(),FamiliarCommand.PROTECT_HOME).equals("command_set"),"offline guard command");
            }
            var far=new BlockPos(20_000_000,80,20_000_000);check(!h.getLevel().hasChunkAt(far),"load fixture remote chunk");
            h.runAfterDelay(10,()->sample(h,loaded,far,40,0,0));
        });
    }
    private static FamiliarRecord record(GameTestHelper h,ServerPlayer owner){
        var state=new CompoundTag();state.putString("command","stay");state.putString("dimension",h.getLevel().dimension().location().toString());
        state.putLong("position",h.absolutePos(new BlockPos(4,1,5)).asLong());state.putInt("reserve",0);
        return new FamiliarRecord(UUID.randomUUID(),owner.getUUID(),UUID.randomUUID(),"wolf","minecraft:wolf",1,FamiliarRecord.Phase.ACTIVE,UUID.randomUUID(),1,state,new CompoundTag());
    }
    private static Mob spawn(GameTestHelper h,FamiliarRecord record,int x,int z){
        var mob=FamiliarEntities.WOLF.get().create(h.getLevel());mob.setUUID(record.creature());mob.setTame(true);mob.setOwnerUUID(record.owner());
        FamiliarBinding.of(mob).orElseThrow().bind(record.bond(),record.owner(),record.generation());
        mob.moveTo(h.absolutePos(new BlockPos(2+x*2,1,2+z)).getCenter());check(h.getLevel().addFreshEntity(mob),"load real entity admission");return mob;
    }
    private static void sample(GameTestHelper h,List<Mob> loaded,BlockPos far,int left,int paths,int scans){
        var work=FamiliarWork.in(h.getLevel());check(work.lastPaths()<=4 && work.lastScans()<=8,"shared work budget exceeded under load");
        check(!h.getLevel().hasChunkAt(far) && h.getLevel().getForcedChunks().isEmpty(),"familiar load created forced terrain");
        int totalPaths=paths+work.lastPaths(),totalScans=scans+work.lastScans();
        if(left==0){check(totalPaths>0 && totalScans>0,"load fixture exercised no real work");loaded.forEach(Mob::discard);h.succeed();}
        else h.runAfterDelay(1,()->sample(h,loaded,far,left-1,totalPaths,totalScans));
    }
}
