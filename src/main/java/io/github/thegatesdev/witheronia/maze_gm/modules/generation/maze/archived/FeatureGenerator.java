package io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.archived;

import java.util.Random;

@FunctionalInterface
public interface FeatureGenerator {
    void onBlockGenerate(Random random, MazeGenerator.Context context, int x, int y, int z, boolean filled);

    default void onGenerationStart(Random random, MazeGenerator.Context context) {
    }

    default void onGenerationEnd(Random random, MazeGenerator.Context context) {
    }
}
