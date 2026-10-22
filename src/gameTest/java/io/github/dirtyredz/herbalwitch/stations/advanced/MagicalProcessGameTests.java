package io.github.dirtyredz.herbalwitch.stations.advanced;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import io.github.dirtyredz.herbalwitch.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessJob;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.stations.process.authorization.StationOperations.Operation;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.energy;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.operate;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.setEnergy;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Witch recipes on a station (the mortar's blinding dust): nothing reserved or run until a witch
 * starts one batch, which binds that payer; progress only while the payer attends; energy spent once,
 * at completion, with practice; a new start for every batch; cancelling consumes nothing; other
 * players cannot take over or cancel a live batch; saved jobs keep their schema.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicalProcessGameTests {
    private static final BlockPos AT = new BlockPos(4, 1, 4);
    private static final BlockPos NEAR = new BlockPos(4, 1, 2);
    private static final int DUST = 100;

    private MagicalProcessGameTests() {
    }

    private static ProcessInventory mortar(GameTestHelper helper) {
        floor(helper);
        place(helper, StationBlocks.MORTAR.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.NIGHTSHADE), 2));
        inv.setStackInSlot(MortarBlockEntity.INPUT_1, new ItemStack(Items.INK_SAC, 2));
        inv.setStackInSlot(MortarBlockEntity.INPUT_2, new ItemStack(Items.SAND, 2));
        return inv;
    }

    private static ProcessJob job(GameTestHelper helper) {
        return ((ProcessStationEntity) helper.getBlockEntity(AT)).station().job();
    }

    @GameTest(template = TEMPLATE)
    public static void aWitchRecipeWaitsUnreservedUntilAWitchStartsIt(GameTestHelper helper) {
        ProcessInventory inv = mortar(helper);
        ServerPlayer novice = player(helper, "m7_novice", NEAR);
        tick(helper, AT, DUST * 2);
        check(job(helper).status() == ProcessStatus.NEEDS_WITCH, "status " + job(helper).status());
        check(inv.getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty(), "ran without a witch (automation cannot start it)");
        check(inv.reserved(MortarBlockEntity.INPUT_0) == 0, "unstarted ingredients stay removable");
        check(operate(helper, novice, AT, Operation.START).equals("not_witch"), "a non-witch started it");
        check(job(helper).progress() == 0, "progress without a start");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void oneStartRunsOneBatchAndPaysOnceAtCompletion(GameTestHelper helper) {
        ProcessInventory inv = mortar(helper);
        ServerPlayer w = witch(helper, "m7_grinder", NEAR);
        int before = energy(w);
        int practice = practice(w);
        check(operate(helper, w, AT, Operation.START).equals("started"), "start refused");
        check(inv.reserved(MortarBlockEntity.INPUT_0) == 0, "reserved before its first tick");
        tick(helper, AT, 1);
        check(inv.reserved(MortarBlockEntity.INPUT_0) == 1, "a started batch holds its inputs");
        tick(helper, AT, DUST - 2);
        check(energy(w) == before, "energy spent before completion");
        check(inv.getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty(), "finished early");
        tick(helper, AT, 1);
        ItemStack out = inv.getStackInSlot(MortarBlockEntity.OUTPUT);
        check(out.is(AdvancedPreparations.item(AdvancedProductCatalog.BLINDING_DUST)) && out.getCount() == 4, "made " + out);
        check(energy(w) == before - EnergyService.units(5), "charged " + (before - energy(w)) + " units, not once");
        check(practice(w) == practice + EnergyService.units(5), "practice follows the spend");
        tick(helper, AT, DUST * 2);
        check(inv.getStackInSlot(MortarBlockEntity.OUTPUT).getCount() == 4, "a second batch ran without a new start");
        check(job(helper).status() == ProcessStatus.NEEDS_WITCH, "the next batch needs its own start: " + job(helper).status());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anAbsentPayerPausesAndOnlyThePayerResumesOrCancels(GameTestHelper helper) {
        ProcessInventory inv = mortar(helper);
        ServerPlayer payer = witch(helper, "m7_payer", NEAR);
        ServerPlayer other = witch(helper, "m7_rival", NEAR);
        check(operate(helper, payer, AT, Operation.START).equals("started"), "start refused");
        tick(helper, AT, 10);
        int progress = job(helper).progress();
        io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.moveTo(helper, payer, new BlockPos(4, 1, 40));
        tick(helper, AT, 20);
        check(job(helper).status() == ProcessStatus.NEEDS_OPERATOR, "status " + job(helper).status());
        check(job(helper).progress() == progress, "progressed while the payer was away");
        check(operate(helper, other, AT, Operation.START).equals("busy"), "a second witch took over a live batch");
        check(operate(helper, other, AT, Operation.CANCEL).equals("not_yours"), "another witch cancelled a live batch");
        check(operate(helper, other, AT, Operation.RESUME).equals("not_yours"), "another witch resumed it");
        io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.moveTo(helper, payer, NEAR);
        tick(helper, AT, 1);
        check(job(helper).status() == ProcessStatus.WORKING, "back in range it works on: " + job(helper).status());
        check(operate(helper, payer, AT, Operation.CANCEL).equals("cancelled"), "the payer could not cancel");
        tick(helper, AT, 1);
        check(job(helper).progress() == 0 && inv.reserved(MortarBlockEntity.INPUT_0) == 0, "cancel keeps progress or holds inputs");
        check(inv.getStackInSlot(MortarBlockEntity.INPUT_0).getCount() == 2 && inv.getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty(),
                "cancelling consumed something");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void tooLittleEnergyPausesAndSpendsNothing(GameTestHelper helper) {
        ProcessInventory inv = mortar(helper);
        ServerPlayer w = witch(helper, "m7_tired", NEAR);
        setEnergy(w, EnergyService.units(4));
        check(operate(helper, w, AT, Operation.START).equals("not_enough_energy"), "started without the energy");
        setEnergy(w, EnergyService.units(5));
        check(operate(helper, w, AT, Operation.START).equals("started"), "start refused");
        tick(helper, AT, 10);
        setEnergy(w, EnergyService.units(4));
        tick(helper, AT, DUST);
        check(job(helper).status() == ProcessStatus.NEEDS_ENERGY, "status " + job(helper).status());
        check(inv.getStackInSlot(MortarBlockEntity.OUTPUT).isEmpty() && energy(w) == EnergyService.units(4), "spent or made something");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void savedJobsKeepTheirSchema(GameTestHelper helper) {
        floor(helper);
        place(helper, StationBlocks.MORTAR.get(), AT);
        ProcessJob job = job(helper);
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("Schema", ProcessJob.LEGACY_SCHEMA);
        job.load(legacy);
        check(!job.unsupported() && job.save().getInt("Schema") == ProcessJob.SCHEMA, "a Milestone 3 job did not load");
        CompoundTag future = new CompoundTag();
        future.putInt("Schema", 99);
        future.putString("Mystery", "kept");
        job.load(future);
        check(job.unsupported() && job.save().equals(future), "an unknown job was not kept byte for byte");
        tick(helper, AT, 5);
        check(job.status() == ProcessStatus.MISSING_INPUT, "an unknown job ran");
        inventory(helper, AT).setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(Blocks.DIRT));
        tick(helper, AT, 1);
        check(!job.unsupported(), "changing the contents releases the station");
        helper.succeed();
    }
}
