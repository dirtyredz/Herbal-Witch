package io.github.dirtyredz.herbalwitch.magic.wand.use;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
/** Actor-wide limits shared by both hands, all copies and every local utility. */
public final class WandUseLimits {
 private RateLimit attempts=new RateLimit(10,20);
 private final java.util.Set<UUID> active=new java.util.HashSet<>();
 private final Map<UUID,Long> next=new HashMap<>();
 public boolean attempt(UUID player,long tick){return attempts.allow(player,tick);}
 public boolean begin(UUID player){return active.add(player);}
 public void end(UUID player){active.remove(player);}
 public boolean ready(UUID player,long tick){return tick>=next.getOrDefault(player,Long.MIN_VALUE);}
 public void committed(UUID player,long tick,int cooldown){if(cooldown<5||cooldown>100)throw new IllegalArgumentException("Wand cooldown");next.put(player,tick+cooldown);}
 public void forget(UUID player){attempts.forget(player);next.remove(player);active.remove(player);}
 public void clear(){attempts=new RateLimit(10,20);next.clear();active.clear();}
}
