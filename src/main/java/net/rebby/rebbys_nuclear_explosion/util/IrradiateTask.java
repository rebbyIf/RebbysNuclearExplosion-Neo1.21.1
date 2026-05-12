package net.rebby.rebbys_nuclear_explosion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentMap;

public record IrradiateTask(ServerLevel level, ConcurrentMap<BlockPos, BlockState> blocks) implements Runnable{
    @Override
    public void run() {
        for (BlockPos blockPos : blocks.keySet()) {
            level.setBlock(blockPos, blocks.get(blockPos), 2);
        }
    }
}
