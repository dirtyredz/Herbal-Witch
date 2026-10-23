package io.github.dirtyredz.herbalwitch.apothecary.stations.catalog;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProcessingCategoriesTest {
    @Test
    void categoriesLoadWithoutRegistriesAndRetainOrderAndHeat() {
        assertEquals(List.of("drying", "grinding", "steeping", "infusion", "cauldron", "distillation", "bottling", "charm_work", "broom_work", "wand_carving"),
                ProcessingCategories.ALL.stream().map(ProcessingCategories.Category::id).toList());
        assertEquals(List.of(false, false, true, true, true, true, false, false, false, false),
                ProcessingCategories.ALL.stream().map(ProcessingCategories.Category::heated).toList());
        assertTrue(ProcessingCategories.byStation(StationCatalog.HEARTH).isEmpty());
        assertEquals(ProcessingCategories.STEEPING, ProcessingCategories.byStation(StationCatalog.KETTLE).orElseThrow());
    }
}
