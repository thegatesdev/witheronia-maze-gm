package io.github.thegatesdev.witheronia.maze_gm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldModification {
    private final World world;
    private final ServerLevel serverLevel;

    private final List<ModifiedBlock> modifications = new ArrayList<>();

    public WorldModification(World world) {
        this.world = world;
        this.serverLevel = ((CraftWorld) world).getHandle();
    }

    public void setBlock(int x, int y, int z, Material material) {
        modifications.add(new ModifiedBlock(x, y, z, ((CraftBlockData) material.createBlockData()).getState()));
    }


    public int update() {
        final Set<LevelChunk> modifiedChunks = new HashSet<>();
        final var lightEngine = serverLevel.getChunkSource().getLightEngine();

        final BlockPos.MutableBlockPos currentBlockPos = new BlockPos.MutableBlockPos();

        int placedBlocks = 0;

        for (final ModifiedBlock modification : modifications) {
            currentBlockPos.set(modification.posX, modification.posY, modification.posZ);
            final LevelChunk blockChunk = serverLevel.getChunkAt(currentBlockPos);
            modifiedChunks.add(blockChunk);
            // Set block
            blockChunk.setBlockState(currentBlockPos, modification.state, false);
            placedBlocks++;
            // Update lighting
            lightEngine.checkBlock(currentBlockPos);
        }

        // Reload player chunks
        for (final LevelChunk chunk : modifiedChunks) {
            final ChunkPos pos = chunk.getPos();

            final var unloadPacket = new ClientboundForgetLevelChunkPacket(pos.x, pos.z);
            final var loadPacket = new ClientboundLevelChunkWithLightPacket(chunk, lightEngine, null, null, true);

            for (final Player player : world.getPlayers()) {
                final ServerPlayer handle = ((CraftPlayer) player).getHandle();
                final int view = Bukkit.getViewDistance();
                final ChunkPos playerChunk = handle.chunkPosition();
                if (pos.x < playerChunk.x - view ||
                        pos.x > playerChunk.x + view ||
                        pos.z < playerChunk.z - view ||
                        pos.z > playerChunk.z + view
                ) continue;
                handle.connection.send(unloadPacket);
                handle.connection.send(loadPacket);
            }
        }

        modifications.clear();
        return placedBlocks;
    }


    private record ModifiedBlock(int posX, int posY, int posZ, BlockState state) {
    }
}
