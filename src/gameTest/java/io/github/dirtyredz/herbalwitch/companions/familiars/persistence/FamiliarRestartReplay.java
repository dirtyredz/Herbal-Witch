package io.github.dirtyredz.herbalwitch.companions.familiars.persistence;

import com.google.gson.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarReceiptVerification;
import io.github.dirtyredz.herbalwitch.companions.familiars.ability.retrieval.FamiliarPickupReceipts;
import io.github.dirtyredz.herbalwitch.companions.lifecycle.CompanionLifecycleAuthority;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.*;
import java.nio.file.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** A new JVM reopens real on-disk checkpoints, then exercises the real entity admission event path. */
final class FamiliarRestartReplay {
    static FamiliarRecord disk(Path directory) throws java.io.IOException {
        try(var paths=Files.newDirectoryStream(directory.resolve("data"),FamiliarSavedData.PREFIX+"*.dat")) {
            var path=paths.iterator().next();return FamiliarCodec.decode(new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT).read(path,FamiliarCodec.MAX_BYTES));
        }
    }
    @SuppressWarnings("unchecked")
    static Map<MinecraftServer,FamiliarStore> worlds() {
        try {var field=FamiliarStore.class.getDeclaredField("WORLDS");field.setAccessible(true);return (Map<MinecraftServer,FamiliarStore>)field.get(null);}
        catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
    }
    static void verify(GameTestHelper h,Path root) {
        floor(h);
        try {
            var prepared=JsonParser.parseString(Files.readString(root.resolve("prepared.json"))).getAsJsonObject();
            check(prepared.get("process").getAsLong()!=ProcessHandle.current().pid(),"restart reused JVM");
            int count=prepared.get("cases").getAsInt();boolean duplicates=false;
            for(int i=0;i<count;i++) duplicates|=one(h,root.resolve(String.format(Locale.ROOT,"case-%03d",i)));
            var oldItem=NbtIo.readCompressed(root.resolve("stale-item.nbt").toFile());
            check(FamiliarPickupReceipts.get(h.getLevel().getServer()).retired(oldItem.getUUID("UUID")),"pickup tombstone lost across process restart");
            var item=EntityType.loadEntityRecursive(oldItem,h.getLevel(),e->e);check(item!=null,"stale item decode");
            check(!h.getLevel().addFreshEntity(item) && item.isRemoved(),"stale item entity became usable after restart");
            var extra=FamiliarRestartExtra.verify(h,root);
            var report=new LinkedHashMap<String,Object>();report.put("process",ProcessHandle.current().pid());report.put("cases",count);report.put("failures",List.of());
            report.put("staleItems",true);report.put("duplicatesQuarantined",duplicates);report.putAll(extra);
            Files.writeString(root.resolve("verified.json"),new Gson().toJson(report));h.succeed();
        } catch(java.io.IOException failure){throw new java.io.UncheckedIOException(failure);}
    }
    private static boolean one(GameTestHelper h,Path directory) throws java.io.IOException {
        var before=disk(directory);var server=h.getLevel().getServer();
        var expected=before.interrupted() || !FamiliarReceiptVerification.verify(before,directory.resolve("data")) ? FamiliarRecord.Phase.QUARANTINED:before.phase();
        CompanionLifecycleAuthority.retire(server,before.creature());
        var original=FamiliarStore.get(server);
        try(var store=new FamiliarStore(directory.resolve("data"),(name,data)->{})) {
            worlds().put(server,store);
            check(!store.ready(),"restart index bypassed readiness");
            check(store.indexBatch()<=32 && store.ready(),"single-shard restart indexing failed: "+directory);
            var record=store.record(before.bond()).orElseThrow();
            check(record.phase()==expected,"wrong restart phase: "+directory+" "+record.phase()+" expected "+expected);
            check(record.generation()==before.generation() && record.state().equals(before.state()) && record.payload().equals(before.payload()),"restart lost inventory/receipt/payload: "+directory);
            check(h.getLevel().getEntity(record.creature())==null,"indexing spawned an absent familiar");
            var meta=NbtIo.readCompressed(directory.resolve("case.nbt").toFile());
            if(meta.contains("source")) {
                var stale=entity(h,meta.getCompound("source"));
                check(stale!=null && !h.getLevel().addFreshEntity(stale),"old source chunk admitted: "+directory);
                check(FamiliarReconciliation.record(stale).isEmpty(),"old source gained inventory authority");stale.discard();
            }
            if(!meta.contains("entity"))return false;
            var candidate=entity(h,meta.getCompound("entity"));check(candidate!=null,"candidate decode");
            boolean shouldAdmit=record.phase()==FamiliarRecord.Phase.ACTIVE && FamiliarReconciliation.matches(candidate,record);
            boolean admitted=h.getLevel().addFreshEntity(candidate);
            check(admitted==shouldAdmit,"restart candidate admission disagreed: "+directory);
            if(!admitted) {check(FamiliarReconciliation.record(candidate).isEmpty(),"refused candidate owns inventory");candidate.discard();return false;}
            check(FamiliarReconciliation.record(candidate).isPresent(),"admitted candidate has no authority");
            var duplicate=entity(h,meta.getCompound("entity"));
            check(!h.getLevel().addFreshEntity(duplicate),"duplicate current incarnation admitted");
            check(store.record(record.bond()).orElseThrow().phase()==FamiliarRecord.Phase.QUARANTINED,"duplicate did not quarantine");
            check(FamiliarReconciliation.record(candidate).isEmpty() && FamiliarReconciliation.record(duplicate).isEmpty(),"duplicate exposed two inventories");
            duplicate.discard();candidate.discard();return true;
        } finally {CompanionLifecycleAuthority.retire(server,before.creature());worlds().put(server,original);}
    }
    private static Mob entity(GameTestHelper h,CompoundTag tag) {
        var entity=EntityType.loadEntityRecursive(tag.copy(),h.getLevel(),e->e);
        if(!(entity instanceof Mob mob))return null;
        mob.moveTo(h.absolutePos(new net.minecraft.core.BlockPos(4,1,5)).getCenter());return mob;
    }
}
