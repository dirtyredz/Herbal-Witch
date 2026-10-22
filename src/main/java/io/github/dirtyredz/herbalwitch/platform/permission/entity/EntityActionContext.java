package io.github.dirtyredz.herbalwitch.platform.permission.entity;

/** Explicit action purpose; features register policy without reverse imports into brewing/platform. */
public final class EntityActionContext {
    public enum Action { HARM, ANIMAL_CARE, ANIMAL_ADOPT, ANIMAL_SEDATE, ANIMAL_CAPTURE, ANIMAL_RELEASE }
    private EntityActionContext() {}
}
