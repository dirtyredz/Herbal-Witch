package io.github.dirtyredz.herbalwitch.apothecary.botany.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec.Form;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.DRY;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.SHADE;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.TEMPERATE;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.WET;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.WOODLAND;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.ATTRACTION;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.BINDING;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.CALMING;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.CONCEALMENT;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.GROWTH;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.PROTECTIVE;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.RESTORATIVE;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.SIGHT;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.SPEED;
import static io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbTrait.TOXIC;

/** The fourteen herbs (docs/catalogue/herbs.md). The only place herbs are defined. */
public final class HerbCatalog {
    public static final HerbSpec LAVENDER = new HerbSpec("lavender", "Lavender", TEMPERATE, Form.BUSH, List.of(CALMING, ATTRACTION), true);
    public static final HerbSpec SAGE = new HerbSpec("sage", "Sage", TEMPERATE, Form.BUSH, List.of(PROTECTIVE, RESTORATIVE), true);
    public static final HerbSpec CHAMOMILE = new HerbSpec("chamomile", "Chamomile", TEMPERATE, Form.CROP, List.of(CALMING, RESTORATIVE), true);
    public static final HerbSpec YARROW = new HerbSpec("yarrow", "Yarrow", TEMPERATE, Form.CROP, List.of(RESTORATIVE, PROTECTIVE), false);
    public static final HerbSpec ROSEMARY = new HerbSpec("rosemary", "Rosemary", DRY, Form.BUSH, List.of(PROTECTIVE, SIGHT), false);
    public static final HerbSpec THYME = new HerbSpec("thyme", "Thyme", DRY, Form.BUSH, List.of(PROTECTIVE, GROWTH), false);
    public static final HerbSpec MINT = new HerbSpec("mint", "Mint", WET, Form.BUSH, List.of(RESTORATIVE, SPEED), true);
    public static final HerbSpec NETTLE = new HerbSpec("nettle", "Nettle", WET, Form.CROP, List.of(GROWTH, RESTORATIVE), false);
    public static final HerbSpec VALERIAN = new HerbSpec("valerian", "Valerian", SHADE, Form.CROP, List.of(CALMING, ATTRACTION), false);
    public static final HerbSpec MUGWORT = new HerbSpec("mugwort", "Mugwort", SHADE, Form.CROP, List.of(SIGHT, BINDING), false);
    public static final HerbSpec BELLADONNA = new HerbSpec("belladonna", "Belladonna", WOODLAND, Form.BUSH, List.of(TOXIC, SIGHT), false);
    public static final HerbSpec NIGHTSHADE = new HerbSpec("nightshade", "Nightshade", WOODLAND, Form.BUSH, List.of(CONCEALMENT, BINDING), false);
    public static final HerbSpec WOLFSBANE = new HerbSpec("wolfsbane", "Wolfsbane", WOODLAND, Form.CROP, List.of(TOXIC, PROTECTIVE), false);
    public static final HerbSpec MANDRAKE = new HerbSpec("mandrake", "Mandrake", WOODLAND, Form.CROP, List.of(BINDING, GROWTH), false);

    /** Group by group, in creative-tab, texture and showcase order. */
    public static final List<HerbSpec> ALL = List.of(LAVENDER, SAGE, CHAMOMILE, YARROW, ROSEMARY, THYME, MINT, NETTLE,
            VALERIAN, MUGWORT, BELLADONNA, NIGHTSHADE, WOLFSBANE, MANDRAKE);

    /** The starter four, in the order the design introduces them. */
    public static final List<HerbSpec> STARTERS = List.of(LAVENDER, SAGE, MINT, CHAMOMILE);

    private HerbCatalog() {
    }

    public static HerbSpec byId(String id) {
        for (HerbSpec herb : ALL) {
            if (herb.id().equals(id)) {
                return herb;
            }
        }
        throw new IllegalArgumentException("unknown herb " + id);
    }
}
