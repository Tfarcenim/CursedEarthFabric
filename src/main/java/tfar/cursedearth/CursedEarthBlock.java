package tfar.cursedearth;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.cursedearth.mixin.ServerWorldAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class CursedEarthBlock extends GrassBlock {
  public CursedEarthBlock(Settings properties) {
    super(properties);
  }

  private static final Logger logger = LogManager.getLogger();

  @Override
  public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
    world.getBlockTickScheduler().schedule(pos, state.getBlock(), world.random.nextInt(125));
  }

  @Override
  public ActionResult onUse(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult p_225533_6_) {
    if (player.getStackInHand(hand).isEmpty() && player.isSneaking() && !world.isClient && hand == Hand.MAIN_HAND) {

      ServerChunkManager s = (ServerChunkManager) world.getChunkManager();

      List<SpawnDetail> spawnInfo = new ArrayList<>();

      BlockPos up = pos.up();

      List<Biome.SpawnEntry> entries = s.getChunkGenerator().getEntitySpawnList(EntityCategory.MONSTER, up);
      // nothing can spawn, occurs in places such as mushroom biomes
      if (entries.size() == 0) {
        player.sendMessage(new TranslatableText("text.cursedearth.nospawns"));
        return ActionResult.SUCCESS;
      } else {
        for (Biome.SpawnEntry entry : entries) {
          spawnInfo.add(new SpawnDetail(entry));
        }
        Text names1 = new TranslatableText("Names: ");
        for (SpawnDetail detail : spawnInfo) {
          names1.append(new TranslatableText(detail.displayName)).append(new LiteralText(", "));
        }
        player.sendMessage(names1);
      }
      return ActionResult.SUCCESS;
    }
    return ActionResult.FAIL;
  }

  public static class SpawnDetail {

    private String displayName;

    //    private boolean lightEnabled = true;
    public SpawnDetail(Biome.SpawnEntry entry) {
      displayName = entry.type.getTranslationKey().replace("Entity", "");
    }
  }

  protected final Predicate<Entity> p = a -> true;

  @Override
  public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
    if (!world.isClient) {
      //if (!world.isAreaLoaded(pos, 3)) return;
      if (isInDaylight(world, pos) && true/*diesInSunlight.get()*/) {
        world.setBlockState(pos, Blocks.DIRT.getDefaultState());
      } else {
        if (world.getLightLevel(pos.up()) <= 7 && /*naturallySpreads.get()*/true && world.getBlockState(pos.up()).isAir()) {
          BlockState blockstate = this.getDefaultState();
          for (int i = 0; i < 4; ++i) {
            BlockPos pos1 = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (world.getBlockState(pos1).getBlock().matches(CursedEarth.spreadable) && world.getBlockState(pos1.up()).isAir()) {
              world.setBlockState(pos1, blockstate.with(SNOWY, world.getBlockState(pos1.up()).getBlock() == Blocks.SNOW));
            }
          }
        }
      }

      world.getBlockTickScheduler().schedule(pos, state.getBlock(), random.nextInt(125));
      //dont spawn in water
      if (!world.getFluidState(pos.up()).isEmpty()) {
        log("there appears to be water blocking the spawns");
        return;
      }
      //don't spawn in peaceful
      if (world.getLevelProperties().getDifficulty() == Difficulty.PEACEFUL) return;
      //mobcap used because mobs are laggy in large numbers todo: how well does this work on servers
      long mobcount = ((ServerWorldAccessor) world).entitiesById().values().stream().filter(Monster.class::isInstance).count();
      if (mobcount > 250) {
        log("there are too many mobs in world to spawn more");
        return;
      }
      int r = 1;//spawnRadius.get();
      if (world.getEntities(EntityType.PLAYER, new Box(-r, -r, -r, r, r, r),p).size() > 0) return;
      MobEntityWithAi mob = findMonsterToSpawn(world, pos.up(), random);
      if (mob != null) {
        mob.updatePosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
        if (!world.doesNotCollide(mob) || !world.intersectsEntities(mob,world.getBlockState(pos.up()).getCollisionShape(world,pos))) {
          log("colliding blocks or entities preventing spawn");
          return;
        }
        log("spawn successful");
        world.spawnEntity(mob);
      }
    }
  }

  public static void log(String message){
    //if (debugLogging.get()){
    //  logger.info(message);
   // }
  }

  @Override
  public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
    return false;//no
  }

  @Override
  public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
    return false;//no
  }

  public boolean isInDaylight(World world, BlockPos pos) {
    return world.isDay() && world.getBrightness(pos.up()) > 0.5F && world.isSkyVisible(pos.up());
  }

  private MobEntityWithAi findMonsterToSpawn(World world, BlockPos pos, Random rand) {
    //required to account for structure based mobs such as wither skeletons
    ServerChunkManager s = (ServerChunkManager) world.getChunkManager();
    List<Biome.SpawnEntry> spawnOptions = s.getChunkGenerator().getEntitySpawnList(EntityCategory.MONSTER, pos);
    //there is nothing to spawn
    if (spawnOptions.size() == 0) {
      return null;
    }
    int found = rand.nextInt(spawnOptions.size());
    Biome.SpawnEntry entry = spawnOptions.get(found);
    //can the mob actually spawn here naturally, filters out mobs such as slimes which have more specific spawn requirements but
    // still show up in spawnlist; ignore them when force spawning
    if (!SpawnRestriction.canSpawn(entry.type, world, SpawnType.NATURAL, pos, world.random)
            && true/*!forceSpawn.get()*/) {
      log("entity "+entry.type.toString()+" cannot naturally spawn here");
      return null;
    } else if (CursedEarth.blacklisted_entities.contains(entry.type)) {
      return null;
    }
    EntityType type = entry.type;
    Entity ent = type.create(world);
    //cursed earth only works with hostiles
    if (!(ent instanceof MobEntityWithAi)) return null;
    ((MobEntityWithAi) ent).initialize(world, world.getLocalDifficulty(pos), SpawnType.NATURAL, null, null);
    return (MobEntityWithAi) ent;
  }
}