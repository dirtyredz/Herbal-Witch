package io.github.dirtyredz.herbalwitch.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Ponder's hooks for HerbalWitch: storyboards come from {@link PonderSceneRegistry}, captions from the lesson catalogues. */
final class HerbalWitchPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return HerbalWitch.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistry.register(helper);
    }

    /** English defaults for Ponder's editing mode; players read the same keys from the generated language file. */
    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        for (var caption : FirstPreparationText.ALL) helper.registerSharedText(caption.id(), caption.english());
    }
}
