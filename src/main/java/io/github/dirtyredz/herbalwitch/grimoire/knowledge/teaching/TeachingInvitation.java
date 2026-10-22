package io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;

import java.util.UUID;

/**
 * One pending, transient offer to teach a recipe. It never survives a restart and is consumed by
 * acceptance, refusal, withdrawal, expiry or any event that invalidates it.
 *
 * @param teacher        who offers
 * @param recipient      who may accept
 * @param recipe         the recipe offered
 * @param fingerprint    the recipe's content when offered; a changed recipe voids the offer
 * @param createdTick    server tick of the offer
 * @param expiresTick    first server tick at which it is no longer valid
 * @param teacherSession the teacher's reader session; closing that reader voids the offer
 */
public record TeachingInvitation(UUID teacher, UUID recipient, EntryKey recipe, String fingerprint, long createdTick,
                                 long expiresTick, long teacherSession) {
    public boolean expired(long now) {
        return now >= expiresTick;
    }

    public int ticksLeft(long now) {
        return (int) Math.max(0, expiresTick - now);
    }

    public boolean involves(UUID player) {
        return teacher.equals(player) || recipient.equals(player);
    }
}
