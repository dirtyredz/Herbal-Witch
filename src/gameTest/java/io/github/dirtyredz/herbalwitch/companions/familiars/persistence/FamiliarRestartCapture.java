package io.github.dirtyredz.herbalwitch.companions.familiars.persistence;

import com.google.gson.Gson;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.platform.persistence.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Mob;
import java.nio.file.*;
import java.util.*;

/** Copies actual durable bytes immediately before/after atomic replacement, never synthesizing a phase. */
final class FamiliarRestartCapture implements AutoCloseable {
    final Path root;
    final MinecraftServer server;
    final List<String> phases=new ArrayList<>();
    final Map<UUID,CompoundTag> sources=new HashMap<>(), latest=new HashMap<>();
    final SnapshotProbe probe;
    int cases;
    boolean failNext;
    FamiliarRestartCapture(Path root,MinecraftServer server) {
        this.root=root;this.server=server;
        probe=new SnapshotProbe(this::observe);
    }
    private void observe(VerifiedSnapshotWriter.Stage stage,Path file) {
        if (!(stage==VerifiedSnapshotWriter.Stage.BEFORE_REPLACE || stage==VerifiedSnapshotWriter.Stage.BEFORE_VERIFY)
                || !file.getFileName().toString().startsWith(FamiliarSavedData.PREFIX) || !Files.isRegularFile(file)) return;
        try {
            var record=FamiliarCodec.decode(new VerifiedSnapshotWriter(FamiliarSavedData.FORMAT).read(file,FamiliarCodec.MAX_BYTES));
            if(cases>=192) throw new IllegalStateException("Restart checkpoint evidence exceeded bound");
            for(var level:server.getAllLevels()) if(level.getEntity(record.creature()) instanceof Mob mob && !mob.isRemoved()) {
                var entity=new CompoundTag();mob.save(entity);latest.put(record.creature(),entity);sources.putIfAbsent(record.creature(),entity.copy());
            }
            var dir=root.resolve(String.format(Locale.ROOT,"case-%03d",cases++)); Files.createDirectories(dir.resolve("data"));
            Files.copy(file,dir.resolve("data").resolve(file.getFileName()));
            var player=file.getParent().getParent().resolve("playerdata").resolve(record.owner()+".dat");
            if(Files.isRegularFile(player)) {Files.createDirectories(dir.resolve("playerdata"));Files.copy(player,dir.resolve("playerdata").resolve(player.getFileName()));}
            var meta=new CompoundTag();meta.putUUID("bond",record.bond());meta.putString("checkpoint",stage.name());
            if(latest.containsKey(record.creature()))meta.put("entity",latest.get(record.creature()).copy());
            if(sources.containsKey(record.creature()))meta.put("source",sources.get(record.creature()).copy());
            NbtIo.writeCompressed(meta,dir.resolve("case.nbt").toFile());
            if(!phases.contains(record.phase().name())) phases.add(record.phase().name());
            if(failNext && stage==VerifiedSnapshotWriter.Stage.BEFORE_VERIFY) {failNext=false;throw new IllegalStateException("Injected final snapshot verification failure");}
        } catch(java.io.IOException failure) {throw new java.io.UncheckedIOException(failure);}
    }
    void finish() {
        close();
        try { Files.writeString(root.resolve("prepared.json"),new Gson().toJson(Map.of("process",ProcessHandle.current().pid(),"cases",cases,"phases",phases))); }
        catch(java.io.IOException failure) {throw new java.io.UncheckedIOException(failure);}
    }
    @Override public void close(){probe.close();}
}
