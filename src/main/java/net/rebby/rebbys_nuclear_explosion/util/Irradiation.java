package net.rebby.rebbys_nuclear_explosion.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.rebby.rebbys_nuclear_explosion.Config;
import net.rebby.rebbys_nuclear_explosion.RebbysNuclearExplosion;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Irradiation {

    public static final Codec<Vector3i> VECTOR_3_I_CODEC = Vec3i.CODEC.xmap(
                    vec3i -> new Vector3i(vec3i.getX(), vec3i.getY(), vec3i.getZ()),
                    vector3i -> new Vec3i(vector3i.x, vector3i.y, vector3i.z));

    public static final Codec<Irradiation> IRRADIATION_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    VECTOR_3_I_CODEC.fieldOf("origin").forGetter(Irradiation::getOrigin),
                    VECTOR_3_I_CODEC.fieldOf("outerMax").forGetter(Irradiation::getOuterMax),
                    VECTOR_3_I_CODEC.fieldOf("innerMax").forGetter(Irradiation::getInnerMax),
                    VECTOR_3_I_CODEC.fieldOf("outerMin").forGetter(Irradiation::getOuterMin),
                    VECTOR_3_I_CODEC.fieldOf("innerMin").forGetter(Irradiation::getInnerMin),
                    Codec.INT.fieldOf("threadX").forGetter(Irradiation::getThreadX)
            ).apply(instance, Irradiation::new)
    );

    private static final BlockState COBBLE_BLOCK_STATE = getDefaultBlockState("minecraft:cobblestone");
    private static final BlockState AIR_BLOCK_STATE = getDefaultBlockState("minecraft:air");

    private static final BlockState WATER_LIQUID_STATE = getDefaultBlockState("minecraft:water");

    private static final int SURFACE_SCAN_Y = 8;

    private static final List<TagKey<Block>> identifiedTagKeys = new ArrayList<>();
    private static Thread [] threads = null;

    private Vector3i origin;
    private Vector3i outerMax;
    private Vector3i innerMax;
    private Vector3i outerMin;
    private Vector3i innerMin;
    private int threadX;

    public static void initThreads() {
        threads = new Thread[Config.maxExplosionThreads];
    }

    public static BlockState getDefaultBlockState(String blockId) {
        return getDefaultBlockState(ResourceLocation.parse(blockId));
    }

    public static BlockState getDefaultBlockState(ResourceLocation blockId) {
        return BuiltInRegistries.BLOCK.get(blockId).defaultBlockState();

    }

    private static boolean checkBlockStateHasTag(BlockState state, String tag) {
        TagKey<Block> tagKey = null;
        for (int i = 0; i < identifiedTagKeys.size(); i++) {
            if (identifiedTagKeys.get(i).location().toString().equals(tag)) {
                tagKey = identifiedTagKeys.get(i);
                break;
            }
        }
        if (tagKey == null) {
            tagKey = BlockTags.create(ResourceLocation.parse(tag));
            identifiedTagKeys.add(tagKey);
        }

        return state.is(tagKey);
    }

    private static boolean isExposed(BlockPos pos, ServerLevel level) {
        return level.getBlockState(pos.north()).isAir() || level.getBlockState(pos.south()).isAir() ||
                level.getBlockState(pos.east()).isAir() || level.getBlockState(pos.west()).isAir() ||
                level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.below()).isAir() ||
                level.getBlockState(pos.above()).is(Blocks.FIRE);
    }

    private static boolean isExposed(BlockPos pos, Map<BlockPos, BlockState> blocks) {
        return blocks.get(pos.north()) != null || blocks.get(pos.south()) != null ||
                blocks.get(pos.east()) != null || blocks.get(pos.west()) != null ||
                blocks.get(pos.above()) != null || blocks.get(pos.below()) != null ||
                blocks.get(pos.north()).isAir() || blocks.get(pos.south()).isAir() ||
                blocks.get(pos.east()).isAir() || blocks.get(pos.west()).isAir() ||
                blocks.get(pos.above()).isAir() || blocks.get(pos.below()).isAir() ||
                blocks.get(pos.above()).is(Blocks.FIRE);
    }

    public Irradiation(Vector3i origin, Vector3i outerMax, Vector3i innerMax, Vector3i outerMin, Vector3i innerMin, int threadX) {
        this.origin = new Vector3i(origin);
        this.outerMax = new Vector3i(outerMax);
        this.innerMax = new Vector3i(innerMax);
        this.outerMin = new Vector3i(outerMin);
        this.innerMin = new Vector3i(innerMin);
        this.threadX = threadX;
    }

    public Vector3i getOrigin() {
        return new Vector3i(origin);
    }

    public void setOrigin(Vector3i origin) {
        this.origin = new Vector3i(origin);
    }

    public Vector3i getOuterMax() {
        return new Vector3i(outerMax);
    }

    public Vector3i getInnerMax() {
        return new Vector3i(innerMax);
    }

    public Vector3i getOuterMin() {
        return new Vector3i(outerMin);
    }

    public Vector3i getInnerMin() {
        return new Vector3i(innerMin);
    }

    public int getThreadX() {
        return threadX;
    }

    public boolean irradiateThreaded(ServerLevel level, Entity source) {

        if (origin.y + 3 > level.getMaxBuildHeight() || origin.y - 3 < level.getMinBuildHeight())
            return false;


        innerMax.add(2, 2, 2, outerMax);
        innerMin.sub(2,2,2, outerMin);
        outerMax.y = Math.min(outerMax.y, level.getMaxBuildHeight() - 1);
        outerMin.y = Math.max(outerMin.y, level.getMinBuildHeight());

        int width = Math.min(outerMax.x - outerMin.x, threads.length);
        ConcurrentMap<BlockPos, BlockState> detectableBlocks = new ConcurrentHashMap<>();
        ConcurrentMap<String, Integer> yMap = new ConcurrentHashMap<>();
        ConcurrentMap<ChunkPos, LevelChunk> detectableChunks = new ConcurrentHashMap<>();

        boolean xzBeyondR2 = false;
        for (int x = outerMin.x; x < outerMax.x; x++) {

            for (int z = outerMin.z; z < outerMax.z; z++) {
                if (xzBeyondR2 && x >= innerMin.x && x < innerMax.x && z == innerMin.z)
                    z = innerMax.z;

                ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);

                if (!detectableChunks.containsKey(chunkPos))
                    detectableChunks.put(chunkPos, level.getChunk(chunkPos.x, chunkPos.z));

                if (x % 16 == 0 && z % 16 == 0) {

                    level.setChunkForced(x > origin.x ? x >> 4 : (x >> 4) - 1,
                            z > origin.z ? z >> 4 : (z >> 4) - 1, true);
                }

                //level.getChunk(x >> 4, z >> 4).setBlockState()
                //level.getChunk(x >> 4, z >> 4).runPostLoad();


                Vector3f centerYPos = new Vector3f(x, origin.y, z);
                xzBeyondR2 = centerYPos.distance(origin.x, origin.y, origin.z) > Config.r2;
                String xz = x + "," + z;
                yMap.put(xz, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z));
                int loopYMax = xzBeyondR2 ? yMap.get(xz)
                        + SURFACE_SCAN_Y / 2 : outerMax.y;
                loopYMax = Math.min(loopYMax, level.getMaxBuildHeight() - 1);
                int loopYMin = xzBeyondR2 ? yMap.get(xz)
                        - SURFACE_SCAN_Y / 2 + 1 : outerMin.y;
                loopYMin = Math.max(loopYMin, level.getMinBuildHeight());

                for (int y = loopYMax - 1; y >= loopYMin; y--) {

                    if (!xzBeyondR2 && x >= innerMin.x && x < innerMax.x && y == loopYMax - 3 && z >= innerMin.z && z < innerMax.z) {
                        y = loopYMin + 2;
                    }

                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState blockState = level.getBlockState(blockPos);

                    detectableBlocks.put(blockPos, blockState);

                    if (blockPos.getX() % 16 == 0 && blockPos.getY() % 16 == 0 && blockPos.getZ() % 16 == 0){

                        FillBiome.fill(level, blockPos, blockPos.offset(16,16,16), RebbysNuclearExplosion.getResource("irradiated_wasteland"));

                    }

                }
            }
        }
        outerMax.sub(1,1,1);
        outerMin.add(1,1,1);

        boolean added = false;
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == null || !threads[i].isAlive()) {
                threadX = (threadX + 1) % width;

                threads[i] = new Thread(new Irradiator(threadX, level, new Vector3i(origin), new Vector3i(outerMin), new Vector3i(outerMax), new Vector3i(innerMin),
                        new Vector3i(innerMax), detectableBlocks, level.getMinBuildHeight(), level.getMaxBuildHeight(), yMap,detectableChunks, RandomSource.createNewThreadLocalInstance()));
                threads[i].start();

                if (threadX == 0) {
                    added = true;
                    break;
                }
            }
        }

        if (added) {
            innerMax = new Vector3i(outerMax);
            innerMin = new Vector3i(outerMin);

            double currentR = innerMax.x - origin.x;
            double step = Math.asin(Config.explosionStep / currentR);

            if (currentR % Config.explosionStep == 0 && currentR > Config.r3){
                for (double angle = 0; angle < Math.PI * 2; angle += step) {
                    double x = origin.x + currentR * Math.cos(angle);
                    double z = origin.z + currentR * Math.sin(angle);
                    double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z);
                    if (level.getEntities(source, AABB.ofSize(new Vec3(x,y,z), 64, 64 ,64)).isEmpty()) {
                        continue;
                    }
                    level.explode(
                            source,
                            x,
                            y,
                            z,
                            (float) (Config.maxExplosionPower * Math.pow((Config.r0 - currentR) / (Config.r0 - Config.r2), 2)),
                            Level.ExplosionInteraction.NONE
                    );
                }
            }
        }

        return innerMax.x - origin.x < Config.r0;
    }

    private record Irradiator(int threadX, ServerLevel level, Vector3i origin, Vector3i outerMin, Vector3i outerMax,
                              Vector3i innerMin, Vector3i innerMax, ConcurrentMap<BlockPos, BlockState> blocks,
                              int levelMinY, int levelMaxY, ConcurrentMap<String, Integer> yMap,
                              ConcurrentMap<ChunkPos, LevelChunk> detectableChunks, RandomSource random) implements Runnable {
        @Override
        public void run() {
            ConcurrentMap<BlockPos, BlockState> changedBlocks = new ConcurrentHashMap<>(blocks.size() / 9);

            // Initialization

            boolean xzBeyondR2 = false;
            for (int x = outerMin.x + threadX; x < outerMax.x; x += threads.length) {
                for (int z = outerMin.z; z < outerMax.z; z++) {

                    if (xzBeyondR2 && x >= innerMin.x && x < innerMax.x && z == innerMin.z)
                        z = innerMax.z;

                    Vector3f centerYPos = new Vector3f(x, origin.y, z);
                    xzBeyondR2 = centerYPos.distance(origin.x, origin.y, origin.z) > Config.r2;
                    String xz = x + "," + z;
                    int loopYMax = xzBeyondR2 ? yMap.get(xz)
                            + SURFACE_SCAN_Y / 2 - 1 : outerMax.y;
                    loopYMax = Math.min(loopYMax, levelMaxY - 1);
                    int loopYMin = xzBeyondR2 ? yMap.get(xz)
                            - SURFACE_SCAN_Y / 2 : outerMin.y;
                    loopYMin = Math.max(loopYMin, levelMinY);

                    for (int y = loopYMax - 1; y >= loopYMin; y--) {

                        if (!xzBeyondR2 && x >= innerMin.x && x < innerMax.x && y == loopYMax - 2 && z >= innerMin.z && z < innerMax.z) {
                            y = loopYMin;
                        }

                        BlockPos blockPos = new BlockPos(x,y,z);
                        Vector3f pos = new Vector3f(x,y,z);
                        BlockState blockState = blocks.get(blockPos);
                        boolean changeBlockState = false;
                        BlockState blockStateUp = blocks.get(blockPos.above());
                        boolean changeBlockStateUp = false;


                        if (blockState == null || blockStateUp == null || blockState.isAir() || blockState.equals(Config.fireBlockState)) {
                            continue;
                        }

                        float distance = pos.distance(origin.x, origin.y, origin.z);
                        float destruction = distance < Config.r3 ? (float) Math.pow((Config.r3 - distance)/4, 2)*1.5f + 2.25f * random.nextFloat() : -1;

                        if (blockState.getBlock().defaultDestroyTime() >= 0 &&
                                blockState.getBlock().defaultDestroyTime() < destruction){
                            changedBlocks.put(blockPos, AIR_BLOCK_STATE);
                            continue;
                        }

                        if (distance <= Config.r0 &&
                                isExposed(blockPos, blocks)){
                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_melts/ice")) {
                                blockState = WATER_LIQUID_STATE;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, WATER_LIQUID_STATE));
                            }

                            if (((FireBlock)Blocks.FIRE).getBurnOdds(blockState) > 5 && blockStateUp.isAir()) {
                                blockStateUp = Config.fireBlockState;
                                changeBlockStateUp = true;
                            }
                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_vaporizes/snow")) {
                                //block.set("minecraft:air");
                                blockState = AIR_BLOCK_STATE;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, AIR_BLOCK_STATE));
                            }

                            // For testing purposes
                            if (Config.isTestingEnvironment) {
                                blockState = COBBLE_BLOCK_STATE;
                                changeBlockState = true;
                            }
                            //level.getServer().execute(new SetBlockTask(level, blockPos, COAL_BLOCK_STATE));
                        }
                        if (distance <= Config.r1){
                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_vaporizes/liquid")
                                    || checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_melts/ice")) {
                                blockState = AIR_BLOCK_STATE;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, AIR_BLOCK_STATE));
                            }

                            if (checkBlockStateHasTag(blockState, "rebbys_nuclear_explosion:nuke_burns/grass") && isExposed(blockPos, blocks)){
                                blockState = Config.scorchedGrassBlockState;
                                changeBlockState = true;
                            }
                            if (checkBlockStateHasTag(blockState, "rebbys_nuclear_explosion:nuke_burns/moss") && isExposed(blockPos, blocks)){
                                blockState = Config.scorchedMossBlockState;
                                changeBlockState = true;
                            }

                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_melts/sand") && isExposed(blockPos, blocks)) {
                                blockState = Config.meltedGlassBlockState;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, BLACK_GLASS_BLOCK_STATE));
                            }

                            if (blockStateUp.isAir()) {
                                blockStateUp = Config.fireBlockState;
                                changeBlockStateUp = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos.above(), FIRE_BLOCK_STATE));
                            }
                            // For testing purposes
                            if (Config.isTestingEnvironment) {
                                blockState = COBBLE_BLOCK_STATE;
                                changeBlockState = true;
                            }
                        }
                        if (distance <= Config.r2){


                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_melts/sand")) {
                                blockState = Config.meltedGlassBlockState;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, BLACK_GLASS_BLOCK_STATE));
                            }

                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_diamondizes")) {
                                blockState = Config.diamondizedBlockState;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, DIAMOND_BLOCK_STATE));
                            }

                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_irradiates")) {
                                blockState = Config.irradiatedBlockState;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, AUTUNITE_BLOCK_STATE));
                            }

                            if (checkBlockStateHasTag(blockState,"rebbys_nuclear_explosion:nuke_irradiates/liquid")) {
                                blockState = Config.irradiatedLiquidState;
                                changeBlockState = true;
                                //level.getServer().execute(new SetBlockTask(level, blockPos, URANIUM_BLOCK_STATE));
                            }

                            // For testing purposes
                            if (Config.isTestingEnvironment) {
                                blockState = COBBLE_BLOCK_STATE;
                                changeBlockState = true;
                            }



                        }

//                        if (x < innerMax.x && x > innerMin.x && z < innerMax.z && z > innerMin.z){
//                            if (blockState.equals(DIAMOND_BLOCK_STATE)) {
//                                blockState = AIR_BLOCK_STATE;
//                            }
//                            else {
//                                blockState = DIAMOND_BLOCK_STATE;
//                            }
//                        }


                        if (changeBlockState)
                            changedBlocks.put(blockPos, blockState);
                        if (changeBlockStateUp)
                            changedBlocks.put(blockPos.above(), blockStateUp);
                    }
                }
            }

            level.getServer().execute(new IrradiateTask(level, changedBlocks, detectableChunks));
        }
    }
}
