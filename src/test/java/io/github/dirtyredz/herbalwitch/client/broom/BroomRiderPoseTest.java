package io.github.dirtyredz.herbalwitch.client.broom;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.*;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dirtyredz.herbalwitch.client.fixture.NativeVertices;
import io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** Static vanilla riding pose only; live animation, camera and latency still need the owner client. */
class BroomRiderPoseTest {
 @Test void authoredBroomMeetsTheVanillaRidingSeat() throws Exception {
  var root=LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,false),64,64).bakeRoot();
  for(String name:List.of("ear","cloak"))if(root.hasChild(name))root.getChild(name).visible=false;
  for(String side:List.of("right","left")){float sign=side.equals("right")?1:-1;for(String part:List.of("leg","pants")){var limb=root.getChild(side+"_"+part);limb.xRot=-1.4137167F;limb.yRot=sign*(float)Math.PI/10;limb.zRot=sign*.07853982F;}for(String part:List.of("arm","sleeve"))root.getChild(side+"_"+part).xRot=-(float)Math.PI/5;}
  double playerOffset=BroomSpec.RIDER_OFFSET-.35,scale=.9375;assertEquals(BroomSpec.MODEL_HEIGHT,playerOffset+12D/16*scale,.01,"broom must meet the seated hips");
  var vertices=new NativeVertices("native:player");root.render(new PoseStack(),vertices,0,0);assertTrue(vertices.quads.size()>30);
  var output=Path.of("build/broom-rider-preview");Files.createDirectories(output);try(var input=getClass().getResourceAsStream("/assets/minecraft/textures/entity/player/wide/steve.png")){assertNotNull(input,"cached vanilla player skin");Files.copy(input,output.resolve("player.png"),StandardCopyOption.REPLACE_EXISTING);}
  Files.writeString(output.resolve("pose.json"),new com.google.gson.Gson().toJson(Map.of("quads",vertices.quads,"playerOffset",playerOffset,"playerScale",scale,"broomHeight",BroomSpec.MODEL_HEIGHT)));
 }
}
