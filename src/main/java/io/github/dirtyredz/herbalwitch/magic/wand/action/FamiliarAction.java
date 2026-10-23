package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation;
import io.github.dirtyredz.herbalwitch.companions.familiars.menu.*;
/** No commands, bond IDs, recall state or prices are owned by the wand. */
public final class FamiliarAction {
 private FamiliarAction(){}
 public static boolean roster(ServerPlayer actor){FamiliarRosterMenu.open(actor);return actor.containerMenu instanceof FamiliarRosterMenu;}
 public static boolean detail(ServerPlayer actor,Mob mob){var record=FamiliarReconciliation.record(mob).orElse(null);return record!=null&&record.owner().equals(actor.getUUID())&&FamiliarMenuAccess.detail(actor,record)&&AnimalInteractionEvents.inspect(actor,mob)&&actor.containerMenu instanceof FamiliarDetailMenu;}
}
