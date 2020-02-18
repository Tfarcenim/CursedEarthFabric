package tfar.cursedearth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;

public class CursedEarthClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> 0x8000ff, CursedEarth.cursed_earth);
		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0x8000ff, CursedEarth.cursed_earth_item);
		BlockRenderLayerMap.INSTANCE.putBlock(CursedEarth.cursed_earth, RenderLayer.getCutout());
	}
}
