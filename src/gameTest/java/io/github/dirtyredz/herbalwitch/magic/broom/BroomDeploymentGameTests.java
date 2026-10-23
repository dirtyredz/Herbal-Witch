package io.github.dirtyredz.herbalwitch.magic.broom;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.*;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.interaction.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.core.*;
import net.minecraftforge.gametest.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.*;
import static io.github.dirtyredz.herbalwitch.magic.broom.fixture.BroomFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
@GameTestHolder(HerbalWitch.MOD_ID) @PrefixGameTestTemplate(false)
public final class BroomDeploymentGameTests {
 @GameTest(template=ARENA) public static void realItemDeploysAndOwnerPacksExactlyOneNamedBroom(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_deploy",AT.offset(0,0,-2));var stack=new ItemStack(BroomItems.BROOM.get());stack.setHoverName(net.minecraft.network.chat.Component.literal("Hazel"));new BroomItemData(250,199,19).write(stack);
  var result=io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn(h,actor,stack,AT.below(),Direction.UP);check(result.consumesAction()&&stack.isEmpty(),"deployment did not transfer");
  var found=h.getLevel().getEntitiesOfClass(BroomEntity.class,new net.minecraft.world.phys.AABB(h.absolutePos(AT)).inflate(1));check(found.size()==1,"wrong deployed count");var broom=found.get(0);broom.tick();
  var foreign=witch(h,"broom_foreign",AT.offset(1,0,0));check(!broom.mount(foreign)&&!BroomPacking.pack(foreign,broom),"foreign access");
  check(BroomPacking.pack(actor,broom)&&broom.isRemoved(),"packing failed");var packed=actor.getInventory().items.stream().filter(s->s.is(BroomItems.BROOM.get())).findFirst().orElseThrow();check(packed.getHoverName().getString().equals("Hazel")&&BroomItemData.read(packed).orElseThrow().equals(new BroomItemData(250,199,19)),"metadata lost");check(!BroomPacking.pack(actor,broom),"packed removed entity twice");h.succeed();
 }
 @GameTest(template=ARENA) public static void blockedDeploymentAndFullInventoryNeverConsumeOrDuplicate(GameTestHelper h){
  floor(h);var actor=witch(h,"broom_blocked",AT.offset(0,0,-2));h.setBlock(AT,net.minecraft.world.level.block.Blocks.STONE);var stack=new ItemStack(BroomItems.BROOM.get());
  check(!io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn(h,actor,stack,AT.below(),Direction.UP).consumesAction()&&stack.getCount()==1,"blocked deployment consumed");h.setBlock(AT,net.minecraft.world.level.block.Blocks.AIR);var b=broom(h,actor);for(int i=0;i<actor.getInventory().items.size();i++)actor.getInventory().items.set(i,new ItemStack(Items.STONE,64));check(!BroomPacking.pack(actor,b)&&!b.isRemoved(),"full packing changed broom");h.succeed();
 }
}
