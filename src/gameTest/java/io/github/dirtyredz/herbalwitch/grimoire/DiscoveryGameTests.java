package io.github.dirtyredz.herbalwitch.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbForms;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryCapability;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryCodec;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.network.DiscoveryUpdate;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.capture;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.carry;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.recipe;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Personal discovery on a real server player: acquisition from the carried inventory, known herb
 * forms making recipes familiar, carried results, the real capability's saved form, cloning, and
 * that updates go only to the player who gained them.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DiscoveryGameTests {
    private static final EntryKey DRY_LAVENDER = recipe("processing/drying/temperate/lavender");
    private static final EntryKey GRIND_LAVENDER = recipe("processing/grinding/herbs/temperate/lavender");
    private static final EntryKey RECOVERY_TEA = recipe("processing/steeping/recovery_tea");

    private DiscoveryGameTests() {
    }

    private static boolean knows(Player player, EntryKey key) {
        return DiscoveryService.knows(player, key);
    }

    @GameTest(template = TEMPLATE)
    public static void carryingAnyFormOfAHerbRecordsTheHerbAndItsDrying(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "disc_forms", new BlockPos(1, 1, 1));
        check(!knows(player, HerbForms.key(HerbCatalog.LAVENDER)), "a fresh player knows nothing");
        carry(player, new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER)));
        player.getInventory().offhand.set(0, new ItemStack(PreparationItems.bundle(HerbCatalog.SAGE)));
        carry(player, new ItemStack(PreparationItems.powder(HerbCatalog.MINT)), new ItemStack(BotanyItems.seeds(HerbCatalog.YARROW)));
        DiscoveryService.observe(player);
        for (var herb : List.of(HerbCatalog.LAVENDER, HerbCatalog.SAGE, HerbCatalog.MINT, HerbCatalog.YARROW)) {
            check(knows(player, HerbForms.key(herb)), herb.id() + " not recorded");
        }
        check(knows(player, DRY_LAVENDER), "carrying fresh lavender makes its drying familiar");
        check(!knows(player, HerbForms.key(HerbCatalog.CHAMOMILE)), "an uncarried herb was recorded");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aKnownHerbsFormsMakeItsChainFamiliar(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "disc_known", new BlockPos(1, 1, 1));
        DiscoveryService.grant(player, HerbForms.key(HerbCatalog.LAVENDER));
        DiscoveryService.observe(player); // carrying nothing at all
        check(knows(player, DRY_LAVENDER), "known lavender makes fresh lavender familiar");
        check(knows(player, GRIND_LAVENDER), "known lavender makes its bundle familiar");
        check(!knows(player, RECOVERY_TEA), "unrelated recipes stay unknown");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void multiIngredientRecipesNeedEveryPrimaryOrTheirResult(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "disc_multi", new BlockPos(1, 1, 1));
        carry(player, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE)));
        DiscoveryService.observe(player);
        check(!knows(player, RECOVERY_TEA), "one of two primaries is not enough");
        carry(player, new ItemStack(PreparationItems.powder(HerbCatalog.YARROW)));
        DiscoveryService.observe(player);
        check(knows(player, RECOVERY_TEA), "both powders make the tea familiar (no water needed)");

        ServerPlayer other = standing(helper, "disc_result", new BlockPos(3, 1, 1));
        carry(other, new ItemStack(PreparationItems.product(EverydayProductCatalog.RECOVERY_TEA)));
        DiscoveryService.observe(other);
        check(knows(other, RECOVERY_TEA), "carrying the result records its recipe");
        check(!knows(other, HerbForms.key(HerbCatalog.CHAMOMILE)), "a result teaches no herb");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void armourAndTheCursorAreNotCarried(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "disc_scope", new BlockPos(1, 1, 1));
        player.getInventory().armor.set(3, new ItemStack(BotanyItems.fresh(HerbCatalog.MINT)));
        player.containerMenu.setCarried(new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE)));
        DiscoveryService.observe(player);
        check(!knows(player, HerbForms.key(HerbCatalog.MINT)), "armour slots counted");
        check(!knows(player, HerbForms.key(HerbCatalog.SAGE)), "the cursor stack counted");
        player.containerMenu.setCarried(ItemStack.EMPTY);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theRealCapabilitySavesWithThePlayer(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "disc_save", new BlockPos(1, 1, 1));
        DiscoveryService.grant(player, RECOVERY_TEA);
        DiscoveryService.grant(player, HerbForms.key(HerbCatalog.MINT));
        CompoundTag saved = player.saveWithoutId(new CompoundTag());
        CompoundTag caps = saved.getCompound("ForgeCaps").getCompound(DiscoveryCapability.ID.toString());
        check(caps.getInt(DiscoveryCodec.SCHEMA_KEY) == DiscoveryCodec.SCHEMA, "no schema in " + caps);
        DiscoveryCapability restored = new DiscoveryCapability();
        restored.load(caps);
        check(restored.state().known().equals(Set.of(RECOVERY_TEA, HerbForms.key(HerbCatalog.MINT))), "round trip " + restored.state().known());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void knowledgeSurvivesDeathAndEndReturnCloning(GameTestHelper helper) {
        for (boolean death : new boolean[] {true, false}) {
            ServerPlayer original = standing(helper, "disc_clone_old_" + death, new BlockPos(1, 1, 1));
            ServerPlayer copy = standing(helper, "disc_clone_new_" + death, new BlockPos(2, 1, 1));
            DiscoveryService.grant(original, RECOVERY_TEA);
            MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone(copy, original, death));
            check(knows(copy, RECOVERY_TEA), "clone (death=" + death + ") lost the discovery");
            check(DiscoveryCapability.of(original).isEmpty(), "the old player's capability was left valid");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void updatesGoOnlyToTheirOwnerAndOnlyWhenNew(GameTestHelper helper) {
        ServerPlayer a = standing(helper, "disc_private_a", new BlockPos(1, 1, 1));
        ServerPlayer b = standing(helper, "disc_private_b", new BlockPos(2, 1, 1));
        var sent = capture(() -> {
            DiscoveryService.grant(a, RECOVERY_TEA);
            DiscoveryService.grant(a, RECOVERY_TEA); // a duplicate is a no-op
        });
        check(sent.size() == 1, "expected one delta, got " + sent.size());
        check(sent.get(0).to() == a && sent.get(0).message() instanceof DiscoveryUpdate u && !u.snapshot()
                && u.keys().equals(List.of(RECOVERY_TEA)), "wrong delta " + sent);
        check(sent.stream().noneMatch(s -> s.to() == b), "another player received a private update");
        var snapshot = capture(() -> DiscoveryService.sendSnapshot(b));
        check(snapshot.size() == 1 && snapshot.get(0).to() == b && ((DiscoveryUpdate) snapshot.get(0).message()).keys().isEmpty(),
                "b's snapshot " + snapshot);
        helper.succeed();
    }
}
