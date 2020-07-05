package tfar.cursedearth;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class CursedEarth implements ModInitializer {
	public static final String MODID = "cursedearth";

	public static final Block cursed_earth = new CursedEarthBlock(Block.Settings.copy(Blocks.GRASS_BLOCK));
	public static final Item cursed_earth_item = new BlockItem(cursed_earth,new Item.Settings().group(ItemGroup.BUILDING_BLOCKS));
	public static Tag<EntityType<?>> blacklisted_entities;
	public static Tag<Block> spreadable;
	public static Tag<Block> spawner_activators;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registry.BLOCK,new Identifier(MODID,"cursed_earth"),cursed_earth);
		Registry.register(Registry.ITEM,new Identifier(MODID,"cursed_earth"),cursed_earth_item);
		UseBlockCallback.EVENT.register((PlayerEntity p, World w, Hand h, BlockHitResult hit) -> {
			ItemStack stack = p.getStackInHand(h);
			if (p.isSneaking() && !w.isClient && stack.getItem() == Items.WITHER_ROSE
							&& w.getBlockState(hit.getBlockPos()).getBlock() == Blocks.DIRT) {
				w.setBlockState(hit.getBlockPos(), cursed_earth.getDefaultState());
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});
	}
}
