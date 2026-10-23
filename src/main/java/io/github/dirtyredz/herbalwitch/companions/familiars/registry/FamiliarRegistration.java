package io.github.dirtyredz.herbalwitch.companions.familiars.registry;

import io.github.dirtyredz.herbalwitch.companions.animals.care.ai.AnimalGoalInstaller;
import io.github.dirtyredz.herbalwitch.companions.animals.care.taming.AnimalOwnership;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.CapturePolicy;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.transaction.FamiliarPlayerReceipt;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarActionCatalog;
import io.github.dirtyredz.herbalwitch.companions.familiars.ritual.BindingAction;
import io.github.dirtyredz.herbalwitch.companions.familiars.species.FamiliarAnimalProfiles;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.AltarActions;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.*;

public final class FamiliarRegistration {
    private FamiliarRegistration() {}
    public static void register(IEventBus bus) {
        FamiliarEntities.TYPES.register(bus); bus.addListener(FamiliarEntities::attributes);
        FamiliarAnimalProfiles.register();
        AnimalOwnership.register(mob -> FamiliarBinding.of(mob).flatMap(FamiliarBinding::reference).map(FamiliarBinding.Reference::owner).orElse(null));
        AnimalGoalInstaller.exclude(FamiliarReconciliation::governed);
        io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.register((action, actor, target) ->
                target instanceof net.minecraft.world.entity.Mob mob && FamiliarReconciliation.suspended(mob)
                        ? io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial.UNSUPPORTED
                        : io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission.Denial.NONE);
        CapturePolicy.exclude(FamiliarReconciliation::governed);
        CaptureReconciliation.lineage((mob, capture) -> mob.level() instanceof ServerLevel level
                && capture.phase() == CaptureRecord.Phase.RELEASED && !capture.storedPayload()
                && FamiliarStore.get(level.getServer()).creature(mob.getUUID()).filter(r -> r.originalType().equals(capture.type())
                && FamiliarReconciliation.matches(mob, r)).isPresent());
        AltarActions.register(FamiliarActionCatalog.BINDING, BindingAction::begin);
        AltarSessionService.handle(FamiliarActionCatalog.BINDING, new BindingAction());
        bus.addListener(FamiliarLifecycleEvents::capabilities);
        var forge = MinecraftForge.EVENT_BUS;
        forge.addGenericListener(Entity.class, FamiliarLifecycleEvents::attach);
        forge.addListener(EventPriority.HIGHEST, true, FamiliarReconciliation::join);
        forge.addListener(FamiliarLifecycleEvents::tick); forge.addListener(FamiliarLifecycleEvents::stopped);
        forge.addListener(EventPriority.HIGHEST, FamiliarLifecycleEvents::living);
        forge.addListener(EventPriority.HIGHEST, FamiliarLifecycleEvents::attack);
        forge.addListener(FamiliarPlayerReceipt::login); forge.addListener(FamiliarPlayerReceipt::clonePlayer);
    }
}
