package tfar.cursedearth.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.cursedearth.CursedEarth;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
	@Shadow public abstract BlockPos getPos();

	@Shadow public abstract World getWorld();

	@Inject(method = "isPlayerInRange",at = @At("RETURN"),cancellable = true)
	private void cursedEarthEffect(CallbackInfoReturnable<Boolean> ci){
		if (ci.getReturnValue())return;
		BlockPos pos = getPos();
		World world = getWorld();
		ci.setReturnValue(world.getBlockState(pos.down()).getBlock().matches(CursedEarth.spawner_activators));
	}
}
