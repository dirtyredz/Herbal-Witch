package io.github.dirtyredz.herbalwitch.magic.wand;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseLimits;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class WandBoundsTest {
 @Test void cooldownAndReentrancyBelongToThePlayer(){var limits=new WandUseLimits();var actor=UUID.randomUUID();assertTrue(limits.begin(actor));assertFalse(limits.begin(actor));limits.end(actor);assertTrue(limits.begin(actor));limits.end(actor);assertTrue(limits.ready(actor,10));limits.committed(actor,10,10);assertFalse(limits.ready(actor,19));assertTrue(limits.ready(actor,20));assertTrue(limits.ready(UUID.randomUUID(),10));limits.forget(actor);assertTrue(limits.ready(actor,10));}
 @Test void attemptedNoOpsCannotFloodTheServer(){var limits=new WandUseLimits();var actor=UUID.randomUUID();for(int i=0;i<10;i++)assertTrue(limits.attempt(actor,0));assertFalse(limits.attempt(actor,0));assertTrue(limits.attempt(actor,20));limits.clear();assertTrue(limits.attempt(actor,0));}
}
