package tfar.cursedearth.mixin;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfar.cursedearth.CursedEarth;

import static tfar.cursedearth.CursedEarth.MODID;

@Mixin(BlockTags.class)
public abstract class BlockTagsMixin {
	@Shadow private static Tag.Identified<Block> register(String id){
		throw new IllegalArgumentException();
	}

	static {
		CursedEarth.spawner_activators = register(MODID+":spawner_activators");
		CursedEarth.spreadable = register(MODID+":spreadable");
	}
}
