package io.github.dirtyredz.herbalwitch.grimoire.content;

import io.github.dirtyredz.herbalwitch.grimoire.content.reference.DiscoveryForms;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;
import io.github.dirtyredz.herbalwitch.apothecary.botany.grimoire.HerbEntries;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinitionLoader;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.content;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ContentContractGameTests {
    @GameTest(template = TEMPLATE)
    public static void duplicateKeysKeepTheFirstSourceAndDropDanglingLinks(GameTestHelper helper) {
        var key = WitchcraftKeys.key("source_contract");
        var absent = WitchcraftKeys.key("absent_contract");
        var first = GrimoireEntry.page(key, "first", net.minecraft.network.chat.Component.literal("first"),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOOK), List.of(), List.of(absent));
        var second = GrimoireEntry.page(key, "second", net.minecraft.network.chat.Component.literal("second"),
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK), List.of(), List.of());
        var inputs = new GrimoireContent.Inputs(helper.getLevel().getRecipeManager(), java.util.Map.of(), java.util.Map.of());
        var built = GrimoireContent.build(79, inputs, List.of((in, out) -> out.accept(first), (in, out) -> out.accept(second)));
        var actual = built.get(key).orElseThrow();
        check(built.size() == 1 && actual.category().equals("first") && actual.title().getString().equals("first"), "duplicate source precedence");
        check(actual.icon().is(net.minecraft.world.item.Items.BOOK) && actual.links().isEmpty(), "duplicate icon/dangling links");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theCompleteOrderedIndexMatchesTheOriginalCatalogues(GameTestHelper helper) {
        var actual = content(helper).entries().stream().map(e -> e.key().serialized()).toList();
        check(actual.equals(ContentOrderBaseline.KEYS), "complete ordered content differs from 4f2d2de: " + actual);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void magicalGuidanceKeepsItsComponentsFlagsAndCrossLinks(GameTestHelper helper) {
        var inputs = new GrimoireContent.Inputs(helper.getLevel().getRecipeManager(), ServerGrimoireContent.herbSummaries(), java.util.Map.of());
        var index = GrimoireContent.build(78, inputs);
        var brewing = index.get(WitchcraftKeys.key("brewing")).orElseThrow();
        check(brewing.always() && brewing.hint().isEmpty() && brewing.recipe().isEmpty(), "brewing flags");
        var keys = brewing.body().stream().map(c -> ((TranslatableContents) c.getContents()).getKey()).toList();
        check(keys.equals(List.of("grimoire.herbalwitch.witchcraft.brewing.0", "grimoire.herbalwitch.witchcraft.brewing.start",
                "grimoire.herbalwitch.witchcraft.brewing.payer", "grimoire.herbalwitch.witchcraft.brewing.cost",
                "grimoire.herbalwitch.witchcraft.brewing.bottling")), "brewing body keys");
        check(((TranslatableContents) brewing.body().get(2).getContents()).getArgs()[0].equals(8), "range argument/type");
        check(((TranslatableContents) brewing.body().get(3).getContents()).getArgs()[0].equals(WitchRules.current().workings().processingPercent()), "cost argument/type");
        check(brewing.links().equals(List.of(WitchcraftKeys.key("preparations"), WitchcraftKeys.key("familiar"))), "brewing links");
        var preparations = index.get(WitchcraftKeys.key("preparations")).orElseThrow();
        check(preparations.body().size() == 7 && preparations.always() && preparations.hint().isEmpty(), "preparation shape");
        check(((TranslatableContents) preparations.body().get(1).getContents()).getArgs()[0].equals(10), "healing cooldown argument");
        check(preparations.links().equals(List.of(WitchcraftKeys.key("brewing"), WitchcraftKeys.key("familiar"))), "preparation links");
        var familiar = index.get(WitchcraftKeys.key("familiar")).orElseThrow();
        check(familiar.body().size() == 2 && familiar.always() && familiar.hint().isEmpty(), "familiar shape");
        check(familiar.links().equals(List.of(WitchcraftKeys.key("brewing"), WitchcraftKeys.key("preparations"))), "familiar links");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void everyHerbKeepsItsFourOrderedDiscoveryForms(GameTestHelper helper) {
        for (var herb : HerbCatalog.ALL) {
            var expected = List.of(BotanyItems.seeds(herb), BotanyItems.fresh(herb), PreparationItems.bundle(herb), PreparationItems.powder(herb));
            check(DiscoveryForms.forms(HerbEntries.key(herb)).equals(expected), "form order " + herb.id());
            for (var item : expected) check(DiscoveryForms.keyOf(item).orElseThrow().equals(HerbEntries.key(herb)), "reverse form " + herb.id());
        }
        helper.succeed();
    }
}
