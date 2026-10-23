package io.github.dirtyredz.herbalwitch.magic.wand.use;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
/** Only synchronous M10 local utility changes. Rituals/familiars never pass through this adapter. */
public interface WandCommit extends WitchStatusService.Change {
 /** Emit drops, pickup notifications and sounds after the energy/world transaction succeeds. */
 default void finish(){}
}
