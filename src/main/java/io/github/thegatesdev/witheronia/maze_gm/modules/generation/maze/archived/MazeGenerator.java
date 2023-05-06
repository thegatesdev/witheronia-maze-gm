package io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.archived;

import io.github.thegatesdev.maze_generator_lib.Maze;
import io.github.thegatesdev.threshold.world.WorldModification;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class MazeGenerator {
    private boolean isGenerated = false;
    private Set<FeatureGenerator> generators;
    private Material[][][] generated;
    private Vector3 generatedSize;

    private static Material[][][] generateFeatures(Collection<FeatureGenerator> generators, Random random, Vector3 size, int corridorWidth, int wallThickness) {
        final Maze maze = new Maze(size.x, size.z, corridorWidth, wallThickness);
        try {
            maze.generate(random);
        } catch (StackOverflowError stackOverflowError) {
            throw new RuntimeException("StackOverFlow: Generated maze too big", stackOverflowError);
        }

        final Context context = new Context(maze.getGenerated(), new Material[size.x][size.z][size.y], size, corridorWidth, wallThickness);
        for (FeatureGenerator generator : generators) {
            generator.onGenerationStart(random, context);
            for (int x = 0, blocksLength = size.x; x < blocksLength; x++) {
                for (int z = 0, blockLength = size.z; z < blockLength; z++) {
                    final boolean filled = context.isWall(x, z);
                    for (int y = 0, materialsLength = size.y; y < materialsLength; y++) {
                        generator.onBlockGenerate(random, context, x, y, z, filled);
                        context.before.y = y;
                    }
                    context.before.z = z;
                }
                context.before.x = x;
            }
            generator.onGenerationEnd(random, context);
        }
        return context.blocks;
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
                    worldModification.set(x + xOff, y + yOff, z + zOff, material);
                }
            }
        }
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public MazeGenerator addFeatureGenerators(FeatureGenerator... input) {
        if (generators == null) generators = new HashSet<>(input.length);
        Collections.addAll(generators, input);
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

        private final Vector3 before = new Vector3(0, 0, 0);

        public Vector3 before() {
            return before;
        }

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

        public Material getBlockAt(Vector3 vector3) {
            return getBlockAt(vector3.x, vector3.y, vector3.z);
        }

        public boolean isWall(int x, int z) {
            return contents[x].get(z);
        }

        public boolean isWallSafe(int x, int z) {
            if (!inMaze(x, z)) return false;
            return isWall(x, z);
        }

        public boolean inMaze(int x, int y, int z) {
            return inMaze(x, z) && y >= 0 && y < mazeSize.y;
        }

        public boolean inMaze(int x, int z) {
            return x >= 0 && z >= 0 && x < mazeSize.x && z < mazeSize.z;
        }

        public WallAxis getWallAxisAt(int x, int y, int z) {
            if (!inMaze(x, y, z)) return WallAxis.NONE;
            final boolean ver = x % sectionSize == 0;
            if (z % sectionSize == 0) {
                if (ver) return WallAxis.BOTH;
                return WallAxis.HOR;
            } else if (ver) return WallAxis.VER;
            else return WallAxis.NONE;
        }

        public void setBlockAt(int x, int y, int z, Material material) {
            if (inMaze(x, y, z)) blocks[x][z][y] = material;
        }

        public int wallDistance(int x, int z, int maxSections) {
            if (!inMaze(x, z) || isWall(x, z)) return 0;
            final int xOffset = (x % sectionSize);
            final int zOffset = (z % sectionSize);

            int low = maxSections * sectionSize;
            boolean found = false;

            for (int sections = 0; sections < maxSections * sectionSize; sections += sectionSize) {
                int xOffsetN = sectionSize - xOffset + sections;
                int zOffsetN = sectionSize - zOffset - sections;

                if (isWallSafe(x + xOffsetN, z)) {
                    low = Math.min(low, xOffsetN); // Wall right
                    if (low == 1) return 1;
                    found = true;
                }
                if (isWallSafe(x - xOffset, z)) {
                    low = Math.min(low, xOffset - 1); // Wall left
                    if (low == 1) return 1;
                    found = true;
                }
                if (isWallSafe(x, z + zOffsetN)) {
                    low = Math.min(low, zOffsetN); // Wall up
                    if (low == 1) return 1;
                    found = true;
                }
                if (isWallSafe(x, z - zOffset)) {
                    return Math.min(low, zOffset - 1); // Wall down
                }
                if (found) return low;
            }
            return low;
        }
    }
}
