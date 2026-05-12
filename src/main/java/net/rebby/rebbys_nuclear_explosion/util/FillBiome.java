package net.rebby.rebbys_nuclear_explosion.util;

import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

/**
 * Mostly copied from Forge's built-in fill biome command. Adjusted for Neoforge
 */
public class FillBiome {

    private static int quantize(int pValue) {
        return QuartPos.toBlock(QuartPos.fromBlock(pValue));
    }
    private static BlockPos quantize(BlockPos pPos) {
        return new BlockPos(quantize(pPos.getX()), quantize(pPos.getY()), quantize(pPos.getZ()));
    }

    private static BiomeResolver makeResolver(MutableInt pBiomeEntries, ServerLevel level, ChunkAccess pChunk, BoundingBox pTargetRegion, ResourceLocation pReplacementBiome) {
        return (p_262550_, p_262551_, p_262552_, p_262553_) -> {
            int i = QuartPos.toBlock(p_262550_);
            int j = QuartPos.toBlock(p_262551_);
            int k = QuartPos.toBlock(p_262552_);

            Holder<Biome> holder = pChunk.getNoiseBiome(p_262550_, p_262551_, p_262552_);
            Registry<Biome> registries = level.registryAccess().registryOrThrow(Registries.BIOME);
            Biome replacementBiome = registries.get(pReplacementBiome);
            if (replacementBiome == null) {
                return holder;
            }
            Holder<Biome> replacement = registries.wrapAsHolder(replacementBiome);

            //Holder<Biome> replacement = ForgeRegistries.BIOMES.getHolder(pReplacementBiome).orElse(holder);
            if (pTargetRegion.isInside(i, j, k) && !holder.is(pReplacementBiome)) {
                pBiomeEntries.increment();
                //System.out.println("Biome Found!");
                return replacement;
            } else {
                //System.out.println("Biome Not Found!");
                return holder;
            }
        };
    }

    public static int fill(ServerLevel serverlevel, BlockPos pFrom, BlockPos pTo, ResourceLocation pBiome) {
        BlockPos blockpos = quantize(pFrom);
        BlockPos blockpos1 = quantize(pTo);
        BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
        List<ChunkAccess> list = new ArrayList<>();

        for(int k = SectionPos.blockToSectionCoord(boundingbox.minZ()); k <= SectionPos.blockToSectionCoord(boundingbox.maxZ()); ++k) {
            for(int l = SectionPos.blockToSectionCoord(boundingbox.minX()); l <= SectionPos.blockToSectionCoord(boundingbox.maxX()); ++l) {
                ChunkAccess chunkaccess = serverlevel.getChunk(l, k, ChunkStatus.FULL, false);

                if (chunkaccess != null)
                    list.add(chunkaccess);
            }
        }


        MutableInt mutableint = new MutableInt(0);

        for(ChunkAccess chunkaccess1 : list) {
            chunkaccess1.fillBiomesFromNoise(makeResolver(mutableint, serverlevel, chunkaccess1, boundingbox, pBiome), serverlevel.getChunkSource().randomState().sampler());
            chunkaccess1.setUnsaved(true);
        }

        serverlevel.getChunkSource().chunkMap.resendBiomesForChunks(list);
        return mutableint.getValue();
    }
}
