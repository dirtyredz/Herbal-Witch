package io.github.dirtyredz.herbalwitch.client.familiars;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.*;
import io.github.dirtyredz.herbalwitch.client.familiars.model.FamiliarNativePose;
import io.github.dirtyredz.herbalwitch.client.familiars.render.FamiliarNativeAttachment;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Export actual vanilla cubes/UVs and the runtime attachment transform without a game client or GL. */
class FamiliarNativePoseTest {
    @Test void nativePosesResetAndEveryAccessoryFollowsItsActualBone() throws Exception {
        Path output=Path.of("build/familiar-native-previews");Files.createDirectories(output);
        var scenes=new ArrayList<Map<String,Object>>();
        var textures=Map.of("cat","cat/tabby","fox","fox/fox","wolf","wolf/wolf","rabbit","rabbit/brown","bat","bat");
        for(String species:List.of("cat","fox","wolf","rabbit","bat")) {
            var root=root(species);var anchor=FamiliarNativeAttachment.of(species);assertTrue(root.hasChild(anchor.bone()));
            var initial=root.getChild("head").storePose();
            for(int i=0;i<3;i++){FamiliarNativePose.reset(root);FamiliarNativePose.apply(root,species,true,false);}
            var rested=root.getChild("head").storePose();assertNotEquals(initial.xRot,rested.xRot);
            FamiliarNativePose.reset(root);assertEquals(initial.xRot,root.getChild("head").xRot); assertEquals(initial.y,root.getChild("head").y);
            String resource="/assets/minecraft/textures/entity/"+textures.get(species)+".png";
            try(var input=getClass().getResourceAsStream(resource)){assertNotNull(input,resource);Files.copy(input,output.resolve(species+".png"),StandardCopyOption.REPLACE_EXISTING);}
            for(String pose:List.of("idle",species.equals("bat")?"flight":"walk","rest","guard")) {
                FamiliarNativePose.reset(root);sampleNativeLocomotion(root,species,pose);
                FamiliarNativePose.apply(root,species,pose.equals("rest"),pose.equals("guard"));
                var vertices=new io.github.dirtyredz.herbalwitch.client.fixture.NativeVertices("native:"+species);root.render(new PoseStack(),vertices,0,0);
                assertTrue(vertices.quads.size()>=12);assertTrue(vertices.pending.isEmpty());
                var matrix=new PoseStack();root.getChild(anchor.bone()).translateAndRotate(matrix);float[] transform=new float[16];matrix.last().pose().get(transform);
                for(float value:transform)assertTrue(Float.isFinite(value));
                var gear=anchor.transform();
                scenes.add(Map.of("species",species,"pose",pose,"quads",vertices.quads,"anchor",transform,"attachment",List.of(gear.x(),gear.y(),gear.z(),gear.scale(),gear.pitch())));
            }
        }
        Files.writeString(output.resolve("poses.json"),new Gson().toJson(scenes));
        assertEquals(20,scenes.size());
    }
    private static ModelPart root(String species){return switch(species){
        case "cat"->LayerDefinition.create(OcelotModel.createBodyMesh(CubeDeformation.NONE),64,32).bakeRoot();
        case "fox"->FoxModel.createBodyLayer().bakeRoot();case "wolf"->WolfModel.createBodyLayer().bakeRoot();
        case "rabbit"->RabbitModel.createBodyLayer().bakeRoot();case "bat"->BatModel.createBodyLayer().bakeRoot();default->throw new IllegalArgumentException();};}
    // Representative native animation samples: actual cubes/UVs, no claim to replace in-game animation review.
    private static void sampleNativeLocomotion(ModelPart root,String species,String pose){
        if(Set.of("cat","fox","wolf").contains(species))root.getChild("body").xRot=(float)Math.PI/2;
        if(species.equals("wolf"))root.getChild("upper_body").xRot=(float)Math.PI/2;
        if(pose.equals("walk"))for(String name:List.of("left_hind_leg","right_hind_leg","left_front_leg","right_front_leg","left_hind_foot","right_hind_foot"))
            if(root.hasChild(name))root.getChild(name).xRot=name.startsWith("left")?.45F:-.45F;
        if(species.equals("bat")){
            var body=root.getChild("body");body.xRot=.785F;
            body.getChild("right_wing").yRot=pose.equals("flight")?.8F:.2F;
            body.getChild("left_wing").yRot=pose.equals("flight")?-.8F:-.2F;
        }
    }
}
