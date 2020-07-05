package tfar.cursedearth.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfar.cursedearth.CursedEarth;

import static tfar.cursedearth.CursedEarth.MODID;

@Mixin(EntityTypeTags.class)
public abstract class EntityTypeTagsMixin {
	@Shadow private static Tag.Identified<EntityType<?>> register(String id){
		throw new IllegalArgumentException();
	}

	static {
		CursedEarth.blacklisted_entities = register(MODID+":blacklisted");
	}
}
