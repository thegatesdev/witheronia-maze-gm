package io.github.thegatesdev.witheronia.maze_gm.generation.maze;

import io.github.thegatesdev.maze_generator_lib.Maze;
import io.github.thegatesdev.witheronia.maze_gm.util.WorldModification;
import org.bukkit.Location;
import org.bukkit.Material;
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

    public void generate(long seed, Vector3 size, int corridorWidth, int wallThickness) {
        generated = generateFeatures(generators, new Random(seed), size, corridorWidth, wallThickness);
        generatedSize = size;
        isGenerated = true;
    }

    public void place(WorldModification worldModification, Location location) {
        if (!isGenerated) throw new RuntimeException("Not generated");
        final int xOff = location.getBlockX(), yOff = location.getBlockY(), zOff = location.getBlockZ();
        for (int x = 0; x < generatedSize.x; x++) {
            for (int z = 0; z < generatedSize.z; z++) {
                for (int y = 0; y < generatedSize.y; y++) {
                    final Material material = generated[x][z][y];
                    if (material == null) continue;
                    worldModification.setBlock(x + xOff, y + yOff, z + zOff, material);
                }
            }
        }
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
        private final int sectionSize;

        private Context(BitSet[] contents, Material[][][] blocks, Vector3 mazeSize, int corridorWidth, int wallThickness) {
            this.contents = contents;
            this.blocks = blocks;
            this.mazeSize = mazeSize;
            this.corridorWidth = corridorWidth;
            this.wallThickness = wallThickness;
            this.sectionSize = corridorWidth + wallThickness;
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
            return inMaze(x, z) && y >= 0 && y < mazeSize.y;
        }

        public boolean inMaze(int x, int z) {
            return x >= 0 && z >= 0 && x < mazeSize.x && z < mazeSize.z;
        }

        public WallAxis getWallAxisAt(int x, int y, int z) {
            if (!inMaze(x, y, z)) return WallAxis.NONE;
            boolean hor = z % sectionSize == 0;
            boolean ver = x % sectionSize == 0;
            if (hor) {
                if (ver) return WallAxis.BOTH;
                return WallAxis.HOR;
            } else if (ver) return WallAxis.VER;
            else return WallAxis.NONE;
        }

        public void setBlockAt(int x, int y, int z, Material material) {
            if (inMaze(x, y, z)) blocks[x][z][y] = material;
        }

        // 0 0 -1 -2 -3 3 2 1 0 0
        public int alignmentDistance(int i) {
            final int dist = (i % sectionSize) - wallThickness + 1;
            if (dist <= 0) return 0;
            return -(dist > corridorWidth / 2d ? dist - corridorWidth - 1 : dist);
        }
    }
}
