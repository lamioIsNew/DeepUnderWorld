package com.wcnm.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Writes sign text directly while a chunk is still a ProtoChunk.
 *
 * <p>{@link SignBlockEntity#setText} calls markUpdated(), which needs a loaded Level.  During
 * chunk generation there is no Level yet, so the normal setter throws.  The accessor writes the
 * same fields without touching the level; the block entity is serialised with the chunk later.</p>
 */
@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
	@Accessor("frontText")
	void wcnm$setFrontText(SignText text);

	@Accessor("backText")
	void wcnm$setBackText(SignText text);
}
