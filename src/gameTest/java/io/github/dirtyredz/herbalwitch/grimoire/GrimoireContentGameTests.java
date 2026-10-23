package io.github.dirtyredz.herbalwitch.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.botany.grimoire.HerbContentFacts;

import io.github.dirtyredz.herbalwitch.grimoire.content.recipe.RecipeReferences;

import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;

import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.ProcessingRecipeBindings;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionReloadListener;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.GuideEntries;
import io.github.dirtyredz.herbalwitch.apothecary.botany.grimoire.HerbEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.network.ContentUpdate;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.SteepingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeIndex;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.content;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.recipe;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Content from the live server: every supported recipe projected (any namespace), recipe edits
 * and removals reflected by the next build, tag reloads producing a new revision, guides and
 * station notes from the editorial data, and herb pages from the server's own conditions.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrimoireContentGameTests {
    private static final EntryKey TEA = recipe("processing/steeping/recovery_tea");

    private GrimoireContentGameTests() {
    }

    private static GrimoireContent build(GameTestHelper helper, List<Recipe<?>> recipes) {
        RecipeManager manager = new RecipeManager(ICondition.IContext.EMPTY);
        manager.replaceRecipes(recipes);
        return GrimoireContent.build(99, new GrimoireContent.Inputs(manager, ServerGrimoireContent.herbSummaries(),
                GrimoireDefinitionLoader.current()));
    }

    private static List<Recipe<?>> serverRecipes(GameTestHelper helper) {
        return new ArrayList<>(helper.getLevel().getRecipeManager().getRecipes());
    }

    @GameTest(template = TEMPLATE)
    public static void everyLiveRecipeHasAPageKeyedByItsId(GameTestHelper helper) {
        GrimoireContent content = content(helper);
        int recipes = 0;
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            for (ProcessRecipe r : helper.getLevel().getRecipeManager().getAllRecipesFor(ProcessingRecipeBindings.type(category))) {
                GrimoireEntry entry = content.get(RecipeReferences.key(r.getId())).orElse(null);
                check(entry != null, r.getId() + " has no page");
                check(entry.recipe().orElseThrow() == r, r.getId() + " page does not hold the live recipe");
                check(entry.category().equals(category.id()), r.getId() + " filed under " + entry.category());
                check(entry.hint().isPresent(), r.getId() + " has no discovery hint");
                recipes++;
            }
        }
        check(recipes == 129 && content.recipes().size() == 129, "M9 has 129 recipes, saw " + recipes + "/" + content.recipes().size());
        // the editorial guides, the three witchcraft pages, the six altar action pages (Milestones 6 and 7), the three
        // advanced preparation pages, wards and charms (Milestone 7), and ordinary animal care (Milestone 8)
        long guides = content.entries().stream().filter(e -> e.key().kind() == EntryKey.Kind.GUIDE).count();
        check(guides == GuideEntries.ORDER.size() + 3 + 6 + 3 + 2 + 1, "guides: " + guides);
        check(content.entries().stream().anyMatch(e -> e.key().serialized().equals("guide|herbalwitch:animals/care")), "animal care guide missing");
        check(content.entries().stream().filter(e -> e.key().kind() == EntryKey.Kind.HERB).count() == HerbCatalog.ALL.size(), "herbs");
        long stations = content.entries().stream().filter(e -> e.key().kind() == EntryKey.Kind.STATION).count();
        check(stations == 10 + 2, "stations, the altar and the circle: " + stations);
        check(content.entries().get(0).key().equals(GuideEntries.key("getting_started")), "the book opens on getting started");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aRecipeFromAnotherNamespaceGetsAFallbackPage(GameTestHelper helper) {
        List<Recipe<?>> recipes = serverRecipes(helper);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("othermod", "drying/mint_twice");
        recipes.add(new DryingRecipe(id, new ProcessRecipeDescription(List.of(new ProcessRecipeDescription.Counted(
                Ingredient.of(BotanyItems.fresh(HerbCatalog.MINT)), 2)), Optional.empty(), Optional.empty(),
                new ItemStack(PreparationItems.bundle(HerbCatalog.MINT), 2), List.of(), 600, false)));
        GrimoireEntry entry = build(helper, recipes).get(RecipeReferences.key(id)).orElse(null);
        check(entry != null, "the external recipe has no page");
        check(entry.category().equals("drying") && entry.hint().isPresent(), "fallback page " + entry);
        check(entry.links().contains(HerbEntries.key(HerbCatalog.MINT)), "its herb is linked");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void changedRemovedAndReintroducedRecipesFollowTheLiveSet(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "content_reload", new BlockPos(1, 1, 1));
        DiscoveryService.grant(player, TEA);
        List<Recipe<?>> recipes = serverRecipes(helper);
        Recipe<?> tea = recipes.stream().filter(r -> r.getId().toString().equals(TEA.id())).findFirst().orElseThrow();
        List<Recipe<?>> without = new ArrayList<>(recipes);
        without.remove(tea);
        check(build(helper, without).get(TEA).isEmpty(), "a removed recipe kept a page");
        check(DiscoveryService.knows(player, TEA), "removing the recipe dropped the saved discovery");
        check(build(helper, recipes).get(TEA).isPresent(), "a reintroduced recipe has no page");
        ProcessRecipeDescription d = ((ProcessRecipe) tea).description();
        List<Recipe<?>> changed = new ArrayList<>(without);
        SteepingRecipe slower = new SteepingRecipe(tea.getId(), new ProcessRecipeDescription(d.ingredients(), d.base(), d.packaging(),
                d.result(), d.returns(), d.duration() * 2, d.heated()));
        changed.add(slower);
        RecipeView shown = build(helper, changed).recipe(TEA).orElseThrow();
        check(shown.description().duration() == d.duration() * 2, "the page shows a stale duration");
        check(!shown.fingerprint().equals(((ProcessRecipe) tea).fingerprint()), "the changed recipe kept its fingerprint");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aTagReloadBuildsANewRevision(GameTestHelper helper) {
        GrimoireContent before = content(helper);
        ProcessRecipeIndex.onTagsUpdated(new TagsUpdatedEvent(helper.getLevel().registryAccess(), false, false));
        GrimoireContent after = content(helper);
        check(after.revision() > before.revision(), "revision " + before.revision() + " -> " + after.revision());
        check(after.size() == before.size(), "the rebuilt content differs in size");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void herbPagesUseTheServersConditionsAndSyncIntact(GameTestHelper helper) {
        GrimoireContent content = content(helper);
        HerbConditions.Condition lavender = HerbConditionReloadListener.current().get(HerbCatalog.LAVENDER);
        GrimoireEntry page = content.get(HerbEntries.key(HerbCatalog.LAVENDER)).orElseThrow();
        String expected = String.valueOf(Math.round(1 / lavender.discoveryChance()));
        boolean found = page.body().stream().map(Component::getContents).anyMatch(c -> c instanceof TranslatableContents t
                && Arrays.asList(t.getArgs()).contains(expected));
        check(found, "lavender's page does not state the server's 1 in " + expected);
        check(ServerGrimoireContent.herbSummaries().size() == HerbCatalog.ALL.size(), "summaries");
        // what clients receive assembles back to the same inputs
        List<ContentUpdate> updates = ContentUpdate.of(content.revision(), GrimoireDefinitionLoader.current(), ServerGrimoireContent.herbSummaries());
        ChunkAssembly<ContentUpdate.Record> assembly = new ChunkAssembly<>();
        List<ContentUpdate.Record> received = null;
        for (ContentUpdate update : updates) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            update.encode(buf);
            ContentUpdate decoded = ContentUpdate.decode(buf);
            received = assembly.offer(decoded.header(), decoded.records()).orElse(received);
        }
        check(received != null && received.size() == GrimoireDefinitionLoader.current().size() + HerbCatalog.ALL.size(),
                "content sync lost records");
        check(received.contains(new ContentUpdate.Herb(HerbContentFacts.summary(lavender))), "lavender's conditions did not survive the wire");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void editorialProseComesFirstAndNeverReplacesMechanics(GameTestHelper helper) {
        GrimoireContent content = content(helper);
        GrimoireEntry kettle = content.entries().stream().filter(e -> e.key().id().equals("herbalwitch:stations/kettle/station"))
                .findFirst().orElseThrow();
        check(kettle.body().get(0).getContents() instanceof TranslatableContents t && t.getKey().startsWith("grimoire.herbalwitch.stations."),
                "the kettle's editorial note is not first: " + kettle.body());
        GrimoireEntry tea = content.get(TEA).orElseThrow();
        check(tea.recipe().orElseThrow() == helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse(TEA.id())).orElseThrow(),
                "the tea page does not read the live recipe");
        helper.succeed();
    }
}
