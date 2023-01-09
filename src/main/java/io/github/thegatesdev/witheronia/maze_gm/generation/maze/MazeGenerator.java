package io.github.thegatesdev.witheronia.maze_gm.generation.maze;

import io.github.thegatesdev.maze_generator_lib.Maze;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R2.block.data.CraftBlockData;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MazeGenerator {

    private boolean isGenerated = false;
    private Set<FeatureGenerator> generators;
    private BukkitRunnable runnable;
    private Material[][][] generated;
    private Vector3 generatedSize;

    private static Material[][][] generateFeatures(Collection<FeatureGenerator> generators, Random random, Vector3 size, int corridorWidth, int wallThickness) {
        final Maze maze = new Maze(size.x, size.z, corridorWidth, wallThickness);
        maze.generate(random);

        final Material[][][] materialGrid = new Material[size.x][size.z][size.y];
        final Context context = new Context(maze.getGenerated(), materialGrid, size, corridorWidth, wallThickness);
        for (FeatureGenerator generator : generators) {
            generator.onGenerationStart(random, context);
            for (int x = 0, blocksLength = materialGrid.length; x < blocksLength; x++) {
                final Material[][] block = materialGrid[x];
                for (int z = 0, blockLength = block.length; z < blockLength; z++) {
                    final Material[] materials = block[z];
                    final boolean filled = context.isWall(x, z);
                    for (int y = 0, materialsLength = materials.length; y < materialsLength; y++) {
                        generator.onBlockGenerate(random, context, x, y, z, filled);
                    }
                }
            }
            generator.onGenerationEnd(random, context);
        }
        return materialGrid;
    }

    private static void placeMore(World world, Material[][][] grid, Vector3 offset, Iterator<BlockPos> positions, int amount) {
        while (positions.hasNext() && amount-- > 0) {
            final BlockPos next = positions.next();
            final Material material = grid[next.getX() - offset.x][next.getZ() - offset.z][next.getY() - offset.y];
            setBlock(world, next, material, false);
        }
    }

    private static void setBlock(World world, BlockPos pos, Material material, boolean applyPhysics) {
        final ServerLevel handle = ((CraftWorld) world).getHandle();
        final LevelChunk nmsChunk = handle.getChunkAt(pos);
        nmsChunk.setBlockState(pos, ((CraftBlockData) material.createBlockData()).getState(), applyPhysics);
    }

    public void generate(long seed, Vector3 size, int corridorWidth, int wallThickness) {
        generated = generateFeatures(generators, new Random(seed), size, corridorWidth, wallThickness);
        generatedSize = size;
        isGenerated = true;
    }

    public void placeBlocksThreaded(Plugin plugin, Location location, int perTick) throws RuntimeException {
        if (!isGenerated) throw new RuntimeException("No maze generated");
        if (perTick <= 0) throw new RuntimeException("Blocks per tick cannot be less than zero");
        if (runnable.isCancelled()) runnable = null;
        else if (runnable != null) throw new RuntimeException("Placing already in progress");
        final World world = location.getWorld();
        if (!location.isWorldLoaded() || world == null) throw new RuntimeException("Invalid world");

        final Vector3 offset = new Vector3(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        final Iterator<BlockPos> positions = BlockPos.betweenClosed(location.getBlockX(), location.getBlockY(), location.getBlockZ(), generatedSize.x, generatedSize.y, generatedSize.z).iterator();

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                placeMore(world, generated, offset, positions, perTick);
                if (!positions.hasNext() && !runnable.isCancelled()) runnable.cancel();
            }
        };
        runnable.runTaskTimer(plugin, 20, 1);
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public MazeGenerator addFeatureGenerators(FeatureGenerator... input) {
        if (generators == null) generators = new HashSet<>(input.length);
        generators.addAll(List.of(input));
        return this;
    }

    public enum WallAxis {
        BOTH, NONE, HOR, VER
    }

    public static class Context {
        private final BitSet[] contents;
        private final Material[][][] blocks;
        private final Vector3 mazeSize;
        private final int corridorWidth;
        private final int wallThickness;

        private Context(BitSet[] contents, Material[][][] blocks, Vector3 mazeSize, int corridorWidth, int wallThickness) {
            this.contents = contents;
            this.blocks = blocks;
            this.mazeSize = mazeSize;
            this.corridorWidth = corridorWidth;
            this.wallThickness = wallThickness;
        }

        public int getCorridorWidth() {
            return corridorWidth;
        }

        public int getWallThickness() {
            return wallThickness;
        }

        public Material getBlockAt(int x, int y, int z) {
            if (!inMaze(x, y, z)) return null;
            return blocks[x][z][y];
        }

        public boolean isWall(int x, int z) {
            return contents[x].get(z);
        }

        public boolean inMaze(int x, int y, int z) {
            return x >= 0 && y >= 0 && z >= 0 && x < mazeSize.x && y < mazeSize.y && z < mazeSize.z;
        }

        public WallAxis getWallAxisAt(int x, int y, int z) {
            if (!inMaze(x, y, z)) return WallAxis.NONE;
            boolean hor = z % (corridorWidth + wallThickness) == 0;
            boolean ver = x % (corridorWidth + wallThickness) == 0;
            if (hor) {
                if (ver) return WallAxis.BOTH;
                return WallAxis.HOR;
            } else if (ver) return WallAxis.VER;
            else return WallAxis.NONE;
        }

        public void setBlockAt(int x, int y, int z, Material material) {
            if (inMaze(x, y, z)) blocks[x][z][y] = material;
        }
    }
}
