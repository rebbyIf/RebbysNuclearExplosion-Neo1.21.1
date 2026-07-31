package net.rebby.rebbys_nuclear_explosion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.rebby.rebbys_nuclear_explosion.Config;

import java.util.concurrent.ConcurrentMap;

public record IrradiateTask(ServerLevel level, ConcurrentMap<BlockPos, BlockState> blocks,
                            ConcurrentMap<ChunkPos, LevelChunk> detectableChunks) implements Runnable{
    @Override
    public void run() {
        for (BlockPos blockPos : blocks.keySet()) {
            LevelChunk chunk = detectableChunks.get(new ChunkPos(blockPos));
            if (chunk == null) {
                throw new NullPointerException("Block position at "+blockPos.getX()+","+blockPos.getZ()+
                        " has no associated chunk!");
            }
            BlockState newState = blocks.get(blockPos);
            BlockState old = chunk.setBlockState(blockPos, newState, false);
            if (old != null) {
                level.sendBlockUpdated(blockPos, old, newState, 2);
            }

        }
    }
}
