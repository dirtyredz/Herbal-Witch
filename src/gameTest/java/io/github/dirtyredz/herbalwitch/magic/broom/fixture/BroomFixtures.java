package io.github.dirtyredz.herbalwitch.magic.broom.fixture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.*;
import io.github.dirtyredz.herbalwitch.magic.broom.network.*;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.broom.config.BroomServerConfig;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
public final class BroomFixtures {
 public static final String ARENA="broom_flight";
 public static final BlockPos AT=new BlockPos(8,1,8);
 private BroomFixtures(){}
 public static ServerPlayer witch(GameTestHelper h,String name,BlockPos feet){var actor=io.github.dirtyredz.herbalwitch.companions.animals.fixture.AnimalFixtures.realPlayer(h,name);actor.moveTo(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(feet)));check(io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService.convert(actor,java.util.UUID.randomUUID()),"witch setup");return actor;}
 public static void floor(GameTestHelper h){for(int x=1;x<30;x++)for(int z=1;z<30;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);}
 public static BroomEntity broom(GameTestHelper h,ServerPlayer actor){var b=BroomEntities.BROOM.get().create(h.getLevel());var at=h.absolutePos(AT);b.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);b.initialize(actor.getUUID(),new ItemStack(BroomItems.BROOM.get()));check(h.getLevel().addFreshEntity(b),"insertion");b.tick();return b;}
 public static void up(ServerPlayer actor,BroomEntity broom){check(broom.mount(actor),"mount refused");BroomPackets.handle(actor,new BroomInput(broom.controls().nonce(),0,BroomControlSession.UP));}
 public static void cost(int units){var r=WitchRules.server();var b=r.broom();check(WitchRules.applyServer(new WitchRules(r.foundingEnabled(),r.initiationEnabled(),r.foundingTicks(),r.initiationTicks(),r.attunementTicks(),r.inviteExpiry(),r.inviteCooldown(),r.energy(),r.workings(),new BroomServerConfig(b.enabled(),b.dimensions(),b.speed(),b.verticalSpeed(),b.acceleration(),b.turnRate(),b.landingSpeed(),units,b.wearInterval(),b.fallProtection()),r.wand())).isEmpty(),"rules");}
}
