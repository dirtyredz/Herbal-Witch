package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The first loaded tick of every saved processing job (schema 1 from 0.2.0, schema 2 from 0.6.0):
 * an unchanged recipe keeps its job, a changed or removed recipe cancels it, and in every case each
 * stored item is still there afterwards. Ticks are driven directly; the chunks themselves stay idle.
 */
final class SavedJobs {
    private SavedJobs() {
    }

    static void firstTick(ServerLevel level, List<LevelChunk> chunks, UpgradeReport report) {
        List<ProcessStationEntity> stations = new ArrayList<>();
        for (LevelChunk chunk : chunks) {
            for (BlockEntity entity : chunk.getBlockEntities().values()) {
                if (entity instanceof ProcessStationEntity station) stations.add(station);
            }
        }
        for (ProcessStationEntity station : stations) {
            check(level, station, report);
        }
        report.put("stations", stations.size());
    }

    private static void check(ServerLevel level, ProcessStationEntity station, UpgradeReport report) {
        BlockPos pos = station.getBlockPos();
        String where = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(station.getType()) + " at " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        CompoundTag saved = station.station().job().save();
        Map<String, Integer> before = contents(station);
        boolean unsupported = station.station().job().unsupported();
        String current = currentRecipe(level, saved);
        station.serverTick();
        CompoundTag after = station.station().job().save();
        Map<String, Integer> afterContents = contents(station);

        String savedRecipe = saved.getString("Recipe");
        String afterRecipe = after.getString("Recipe");
        String outcome;
        if (unsupported) outcome = station.station().job().unsupported() ? "preserved_unknown_schema" : "unknown_schema_lost";
        else if (savedRecipe.isEmpty()) outcome = afterRecipe.isEmpty() ? "idle" : "matched";
        else if (afterRecipe.equals(savedRecipe)) outcome = "resumed";
        else if (afterRecipe.isEmpty()) outcome = "cancelled";
        else outcome = "rematched";

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("where", where);
        entry.put("savedSchema", saved.getInt("Schema"));
        entry.put("savedRecipe", savedRecipe);
        entry.put("recipeNow", current);
        entry.put("outcome", outcome);
        entry.put("savedProgress", saved.getInt("Progress"));
        entry.put("progressAfter", after.getInt("Progress"));
        entry.put("contents", before);
        report.job(entry);

        if (!before.equals(afterContents)) report.fail(where + ": the first tick changed stored items " + before + " -> " + afterContents);
        if (outcome.equals("unknown_schema_lost")) report.fail(where + ": an unknown job schema was dropped");
        if (current.equals("same") && !outcome.equals("resumed")) report.fail(where + ": unchanged recipe " + savedRecipe + " did not resume (" + outcome + ")");
        if ((current.equals("changed") || current.equals("removed")) && outcome.equals("resumed")) {
            report.fail(where + ": " + current + " recipe " + savedRecipe + " resumed its old job");
        }
    }

    /** "none" (no saved recipe), "same", "changed" (same id, different fingerprint) or "removed". */
    private static String currentRecipe(ServerLevel level, CompoundTag saved) {
        if (!saved.contains("Recipe")) return "none";
        ResourceLocation id = ResourceLocation.tryParse(saved.getString("Recipe"));
        var recipe = id == null ? null : level.getRecipeManager().byKey(id).orElse(null);
        if (!(recipe instanceof ProcessRecipe process)) return "removed";
        return process.fingerprint().equals(saved.getString("Fingerprint")) ? "same" : "changed";
    }

    /** Every stored stack by item and full tag, in slot-independent form. */
    private static Map<String, Integer> contents(ProcessStationEntity station) {
        Map<String, Integer> counts = new TreeMap<>();
        var inventory = station.inventory();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            String key = BuiltInRegistries.ITEM.getKey(stack.getItem()) + (stack.hasTag() ? stack.getTag().toString() : "");
            counts.merge(key, stack.getCount(), Integer::sum);
        }
        return counts;
    }
}
