package io.github.dirtyredz.herbalwitch.client.grimoire.ponder.scenes;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationLayout;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText.Caption;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import static io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationLayout.FACING;

/**
 * The first-preparation storyboard: harvest lavender, dry it, grind it, show valerian, light the stove, load the
 * kettle, brew and drink the bundled Restful Infusion. Illustration only: block states, transient item entities,
 * control overlays and particles. It never calls a station, inventory, processing, fuel or payment service and
 * never fills a station inventory; the stations' tickers stay off on Ponder's client level. Rack {@code active}
 * means occupied: it is set when the herb goes in and cleared only when the bundle is taken out.
 *
 * <p>Ponder's default camera looks from the north-west, so the north-facing stations show their fronts.
 */
public final class FirstPreparationScene {
    /** Caption pause per beat, in ticks. */
    private static final int READ = 90;
    private static final int SHORT = 60;

    private FirstPreparationScene() {
    }

    public static void program(SceneBuilder scene, SceneBuildingUtil util) {
        Cast cast = new Cast(util);
        scene.title(FirstPreparationText.SCENE, FirstPreparationText.TITLE);
        scene.configureBasePlate(0, (FirstPreparationLayout.SIZE.getZ() - FirstPreparationLayout.SIZE.getX()) / 2,
                FirstPreparationLayout.SIZE.getX());
        scene.scaleSceneView(0.7f);
        introduce(scene, cast);
        harvest(scene, cast);
        dry(scene, cast);
        grind(scene, cast);
        heat(scene, cast);
        brew(scene, cast);
        drink(scene, cast);
        scene.markAsFinished();
    }

    private static void introduce(SceneBuilder scene, Cast cast) {
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(cast.lavender, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cast.rack, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cast.mortar, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cast.stove, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(cast.kettle, Direction.DOWN);
        scene.idle(15);
        scene.overlay().showControls(cast.top(FirstPreparationLayout.KETTLE).add(0, 0.5, 0), Pointing.DOWN, READ)
                .withItem(cast.infusion());
        caption(scene, FirstPreparationText.INTRO, READ + 20).independent();
        scene.idle(READ + 30);
    }

    private static void harvest(SceneBuilder scene, Cast cast) {
        BlockPos plant = FirstPreparationLayout.LAVENDER;
        caption(scene, FirstPreparationText.HARVEST, READ).pointAt(cast.top(plant)).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(cast.top(plant), Pointing.DOWN, 40).rightClick();
        scene.idle(20);
        scene.world().modifyBlock(plant, state -> withInt(state, "age", 1), false);
        scene.world().createItemEntity(cast.top(plant), new Vec3(0.05, 0.25, -0.05), cast.fresh(HerbCatalog.LAVENDER));
        scene.world().createItemEntity(cast.top(plant), new Vec3(-0.05, 0.25, -0.05), cast.seeds(HerbCatalog.LAVENDER));
        scene.idle(READ - 10);
        scene.world().modifyEntities(ItemEntity.class, Entity::discard);
        scene.idle(10);
    }

    private static void dry(SceneBuilder scene, Cast cast) {
        Vec3 front = cast.front(FirstPreparationLayout.RACK_ORIGIN);
        caption(scene, FirstPreparationText.DRY, READ).pointAt(front).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(front, Pointing.DOWN, 50).rightClick().withItem(cast.fresh(HerbCatalog.LAVENDER));
        scene.idle(60);
        scene.world().modifyBlocks(cast.rack, state -> withBoolean(state, "active", true), false);
        caption(scene, FirstPreparationText.DRY_WAIT, READ).pointAt(front).placeNearTarget();
        scene.idle(READ);
        scene.overlay().showControls(front, Pointing.UP, 50).withItem(cast.bundle(HerbCatalog.LAVENDER));
        scene.idle(40);
        scene.world().modifyBlocks(cast.rack, state -> withBoolean(state, "active", false), false);
        scene.idle(20);
    }

    private static void grind(SceneBuilder scene, Cast cast) {
        BlockPos mortar = FirstPreparationLayout.MORTAR;
        caption(scene, FirstPreparationText.GRIND, READ).pointAt(cast.front(mortar)).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(cast.top(mortar), Pointing.DOWN, 40).rightClick()
                .withItem(cast.bundle(HerbCatalog.LAVENDER));
        scene.idle(45);
        var crumbs = scene.effects().simpleParticleEmitter(
                new ItemParticleOption(ParticleTypes.ITEM, cast.bundle(HerbCatalog.LAVENDER)), new Vec3(0, 0.1, 0));
        scene.effects().emitParticles(cast.top(mortar), crumbs, 2, 10);
        scene.idle(25);
        scene.overlay().showControls(cast.top(mortar), Pointing.UP, 50).withItem(cast.powder(HerbCatalog.LAVENDER));
        scene.idle(60);
        caption(scene, FirstPreparationText.VALERIAN, READ).pointAt(cast.top(mortar)).placeNearTarget().attachKeyFrame();
        scene.idle(20);
        scene.overlay().showControls(cast.top(mortar), Pointing.UP, READ - 20).withItem(cast.powder(HerbCatalog.VALERIAN));
        scene.idle(READ);
    }

    private static void heat(SceneBuilder scene, Cast cast) {
        Vec3 stove = cast.front(FirstPreparationLayout.STOVE_ORIGIN);
        caption(scene, FirstPreparationText.HEAT, READ).pointAt(stove).placeNearTarget().attachKeyFrame();
        scene.idle(30);
        scene.overlay().showControls(stove, Pointing.DOWN, 50).rightClick().withItem(new ItemStack(Items.COAL));
        scene.idle(55);
        scene.world().modifyBlocks(cast.stove, state -> withBoolean(state, "lit", true), false);
        var smoke = scene.effects().simpleParticleEmitter(ParticleTypes.SMOKE, new Vec3(0, 0.02, 0));
        scene.effects().emitParticles(stove, smoke, 1, 8);
        scene.idle(READ - 50);
    }

    private static void brew(SceneBuilder scene, Cast cast) {
        BlockPos kettle = FirstPreparationLayout.KETTLE;
        Vec3 top = cast.top(kettle);
        caption(scene, FirstPreparationText.LOAD, READ + 30).pointAt(top).placeNearTarget().attachKeyFrame();
        scene.idle(25);
        scene.overlay().showControls(top, Pointing.DOWN, 30).rightClick().withItem(cast.powder(HerbCatalog.LAVENDER));
        scene.idle(35);
        scene.overlay().showControls(top, Pointing.DOWN, 30).rightClick().withItem(cast.powder(HerbCatalog.VALERIAN));
        scene.idle(35);
        scene.overlay().showControls(top, Pointing.DOWN, 40).rightClick().withItem(cast.waterBottle());
        scene.idle(50);
        caption(scene, FirstPreparationText.CONTAINER, SHORT + 10).pointAt(top).placeNearTarget();
        scene.idle(SHORT + 20);
        caption(scene, FirstPreparationText.BREW, READ).pointAt(top).placeNearTarget().attachKeyFrame();
        var steam = scene.effects().simpleParticleEmitter(ParticleTypes.CLOUD, new Vec3(0, 0.04, 0));
        scene.effects().emitParticles(top, steam, 0.5f, 60);
        scene.idle(READ - 20);
        scene.overlay().showControls(top, Pointing.UP, 50).withItem(cast.infusion());
        scene.idle(SHORT);
    }

    private static void drink(SceneBuilder scene, Cast cast) {
        Vec3 hand = cast.top(FirstPreparationLayout.MORTAR).add(0, 1, 0);
        caption(scene, FirstPreparationText.DRINK, READ).independent().attachKeyFrame();
        scene.idle(20);
        scene.overlay().showControls(hand, Pointing.DOWN, 40).rightClick().withItem(cast.infusion());
        scene.idle(50);
        scene.overlay().showControls(hand, Pointing.UP, 40).withItem(new ItemStack(Items.GLASS_BOTTLE));
        scene.idle(READ - 50);
        caption(scene, FirstPreparationText.REPLAY, READ).independent().attachKeyFrame();
        scene.idle(READ + 10);
    }

    private static net.createmod.ponder.api.element.TextElementBuilder caption(SceneBuilder scene, Caption caption, int ticks) {
        return scene.overlay().showText(ticks).sharedText(caption.id());
    }

    private static BlockState withBoolean(BlockState state, String name, boolean value) {
        return state.getBlock().getStateDefinition().getProperty(name) instanceof BooleanProperty property
                ? state.setValue(property, value) : state;
    }

    private static BlockState withInt(BlockState state, String name, int value) {
        return state.getBlock().getStateDefinition().getProperty(name) instanceof IntegerProperty property
                && property.getPossibleValues().contains(value) ? state.setValue(property, value) : state;
    }

    /** The scene's selections and props, resolved once per compile (no static scene state). */
    private static final class Cast {
        final SceneBuildingUtil util;
        final Selection lavender;
        final Selection rack;
        final Selection mortar;
        final Selection stove;
        final Selection kettle;

        Cast(SceneBuildingUtil util) {
            this.util = util;
            this.lavender = util.select().position(FirstPreparationLayout.LAVENDER);
            this.rack = cells(StationCatalog.DRYING_RACK, FirstPreparationLayout.RACK_ORIGIN);
            this.mortar = util.select().position(FirstPreparationLayout.MORTAR);
            this.stove = cells(StationCatalog.STOVE, FirstPreparationLayout.STOVE_ORIGIN);
            this.kettle = util.select().position(FirstPreparationLayout.KETTLE);
        }

        /** Every cell of a multiblock station, from its own layout (never restated offsets). */
        private Selection cells(StationSpec spec, BlockPos origin) {
            var layout = spec.layout();
            Selection selection = util.select().position(layout.cellPos(origin, FACING, 0));
            for (int i = 1; i < layout.size(); i++) selection = selection.add(util.select().position(layout.cellPos(origin, FACING, i)));
            return selection;
        }

        Vec3 top(BlockPos pos) {
            return util.vector().topOf(pos);
        }

        Vec3 front(BlockPos pos) {
            return util.vector().blockSurface(pos, FACING);
        }

        ItemStack fresh(HerbSpec herb) {
            return item(herb.freshPath());
        }

        ItemStack seeds(HerbSpec herb) {
            return item(herb.seedsPath());
        }

        ItemStack bundle(HerbSpec herb) {
            return item(HerbalMaterialCatalog.bundlePath(herb));
        }

        ItemStack powder(HerbSpec herb) {
            return item(HerbalMaterialCatalog.powderPath(herb));
        }

        ItemStack infusion() {
            return item(EverydayProductCatalog.RESTFUL_INFUSION.path());
        }

        ItemStack waterBottle() {
            return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        }

        private static ItemStack item(String path) {
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("herbalwitch", path));
            return item == null ? ItemStack.EMPTY : new ItemStack(item);
        }
    }
}
