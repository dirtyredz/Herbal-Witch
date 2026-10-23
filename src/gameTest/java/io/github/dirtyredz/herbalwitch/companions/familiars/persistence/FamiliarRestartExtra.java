package io.github.dirtyredz.herbalwitch.companions.familiars.persistence;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarReceiptVerification;
import io.github.dirtyredz.herbalwitch.platform.persistence.VerifiedSnapshotWriter;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

/** Deliberately damaged files are isolated so they cannot hide the valid checkpoint admission matrix. */
final class FamiliarRestartExtra {
    static void prepare(Path root) {
        try {
            var first=NbtIo.readCompressed(root.resolve("case-000/case.nbt").toFile());
            NbtIo.writeCompressed(first.getCompound("source"),root.resolve("before-first-binding.nbt").toFile());
            FamiliarRecord sample=null;
            try(var directories=Files.newDirectoryStream(root,"case-*")) {
                for(var directory:directories) {
                    var r=FamiliarRestartReplay.disk(directory);
                    if(!r.interrupted() && r.phase()==FamiliarRecord.Phase.ACTIVE && FamiliarReceiptVerification.verify(r,directory.resolve("data"))) {sample=r;break;}
                }
            }
            if(sample==null)throw new IllegalStateException("No committed checkpoint for extra restart fixtures");
            var clean=sample.change(s->{s.remove("receipt");s.remove("settledReceipt");});
            write(root.resolve("corrupt/data"),clean);var corrupt=path(root.resolve("corrupt/data"),clean);var bytes=Files.readAllBytes(corrupt);bytes[bytes.length-1]^=1;Files.write(corrupt,bytes);
            var raw=FamiliarCodec.encode(clean,FamiliarCodec.MAX_BYTES);var tag=NbtIo.read(new DataInputStream(new ByteArrayInputStream(raw)));tag.putInt("schema",99);
            var out=new ByteArrayOutputStream();NbtIo.write(tag,new DataOutputStream(out));
            new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT).write(path(root.resolve("schema/data"),clean),out.toByteArray(),FamiliarCodec.MAX_BYTES);
            // The settled economic watermark remains, but the player's save is intentionally absent/older.
            write(root.resolve("older-player/data"),sample);
            for(int i=0;i<97;i++) {
                var r=new FamiliarRecord(UUID.randomUUID(),clean.owner(),UUID.randomUUID(),clean.species(),clean.originalType(),1,FamiliarRecord.Phase.ACTIVE,UUID.randomUUID(),1,clean.state(),new CompoundTag());
                write(root.resolve("index/data"),r);
            }
        }catch(IOException failure){throw new UncheckedIOException(failure);}
    }
    private static Path path(Path directory,FamiliarRecord record){return directory.resolve(FamiliarSavedData.name(record.bond())+".dat");}
    private static void write(Path directory,FamiliarRecord record)throws IOException {
        new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT).write(path(directory,record),FamiliarCodec.encode(record,FamiliarCodec.MAX_BYTES),FamiliarCodec.MAX_BYTES);
    }
    static Map<String,Object> verify(GameTestHelper h,Path root)throws IOException {
        var server=h.getLevel().getServer();var original=FamiliarStore.get(server);
        try(var empty=new FamiliarStore(root.resolve("before-first/data"),(name,data)->{})) {
            FamiliarRestartReplay.worlds().put(server,empty);empty.indexBatch();
            var tag=NbtIo.readCompressed(root.resolve("before-first-binding.nbt").toFile());
            var source=net.minecraft.world.entity.EntityType.loadEntityRecursive(tag,h.getLevel(),e->e);
            check(source!=null && h.getLevel().addFreshEntity(source),"before first durable binding lost the ordinary source");
            check(empty.creature(source.getUUID()).isEmpty(),"absent binding acquired inventory authority");source.discard();
        }finally{FamiliarRestartReplay.worlds().put(server,original);}
        for(String damaged:List.of("corrupt","schema")) {
            var directory=root.resolve(damaged+"/data");Path file;
            try(var files=Files.newDirectoryStream(directory)){file=files.iterator().next();}
            var before=Files.readAllBytes(file);
            try(var store=new FamiliarStore(directory,(name,data)->{})) {
                store.indexBatch();check(store.indexed() && !store.ready(),damaged+" did not refuse mutations");
                check(Arrays.equals(before,Files.readAllBytes(file)),damaged+" was rewritten");
            }
        }
        var older=FamiliarRestartReplay.disk(root.resolve("older-player"));
        try(var store=new FamiliarStore(root.resolve("older-player/data"),(name,data)->{})) {
            store.indexBatch();var after=store.record(older.bond()).orElseThrow();
            check(after.phase()==FamiliarRecord.Phase.QUARANTINED && after.state().equals(older.state()),"older player save unlocked or lost canonical inventory");
        }
        try(var store=new FamiliarStore(root.resolve("index/data"),(name,data)->{})) {
            check(store.indexBatch()==32 && !store.ready(),"startup index failed first 32-shard bound");
            int total=32;while(!store.indexed()){int n=store.indexBatch();check(n<=32,"unbounded indexing");total+=n;}
            check(total==97 && store.ready(),"bounded roster rebuild incomplete");
        }
        return Map.of("corruptPreserved",true,"schemaPreserved",true,"indexBounded",true,"olderPlayerQuarantined",true);
    }
}
