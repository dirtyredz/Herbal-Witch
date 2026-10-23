package io.github.dirtyredz.herbalwitch.client.familiars;

import com.google.gson.JsonParser;
import io.github.dirtyredz.herbalwitch.client.familiars.model.*;
import net.minecraft.client.model.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarRigGeometryTest {
    @Test void compiledRigsBakeTheirRealBonesAndEquipmentHierarchyHeadlessly() throws Exception {
        for (String species : List.of("raven", "owl", "snake")) {
            var json = JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/herbalwitch/entity_models/familiars/" + species + "/rig.json"))).getAsJsonObject();
            var rig = FamiliarRigLoader.bake(species, json);
            assertEquals(species.equals("snake") ? 5 : 7, rig.bones().size());
            assertEquals(24, rig.root().y); assertSame(rig.root(), rig.equipmentPath().get(0));
            assertSame(rig.bones().get(json.get("equipmentBone").getAsString()), rig.equipmentPath().get(rig.equipmentPath().size() - 1));
            assertEquals("herbalwitch", rig.appearances().get(species).getNamespace());
            assertTrue(rig.appearances().get(species).getPath().startsWith("textures/entity/familiars/"));
            rig.root().getAllParts().forEach(p -> p.resetPose());
            FamiliarAnimations.apply(rig.bones(), species, species.equals("snake") ? "walk" : "flight", 2);
            if (species.equals("snake")) assertNotEquals(0, rig.bones().get("middle").yRot);
            else assertTrue(Math.abs(rig.bones().get("wing_left").zRot) > 1);
            rig.root().getAllParts().forEach(p -> p.resetPose());
            assertEquals(0, rig.bones().get(species.equals("snake") ? "middle" : "wing_left").yRot);
        }
    }
    @Test void vanillaEquipmentAnchorsExistOnThePinnedModels() {
        assertTrue(WolfModel.createBodyLayer().bakeRoot().hasChild("upper_body"));
        assertTrue(FoxModel.createBodyLayer().bakeRoot().hasChild("body"));
        assertTrue(RabbitModel.createBodyLayer().bakeRoot().hasChild("body"));
        assertTrue(BatModel.createBodyLayer().bakeRoot().hasChild("body"));
    }
}
