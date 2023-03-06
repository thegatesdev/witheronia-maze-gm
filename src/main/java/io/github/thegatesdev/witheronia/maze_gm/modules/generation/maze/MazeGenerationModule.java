package io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze;

import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.Material;

public class MazeGenerationModule extends PluginModule<MazeGamemode> {

    private final Material[] materials = new Material[]{Material.STONE, Material.ANDESITE, Material.COBBLESTONE, Material.DEAD_BRAIN_CORAL};
    private final MazeGenerator basicGenerator = new MazeGenerator()
            .addFeatureGenerators((random, context, x, y, z, filled) -> {
                if (!filled) return;
                MazeGenerator.WallAxis wallAxis = context.getWallAxisAt(x, y, z);
                final boolean both = wallAxis == MazeGenerator.WallAxis.BOTH;
                if (both || wallAxis == MazeGenerator.WallAxis.HOR) {
                    Material at = context.getBlockAt(context.before().hor());
                    if (at == Material.STONE || at == Material.ANDESITE && random.nextBoolean()) {
                        context.setBlockAt(x, y, z, Material.STONE);
                    } else context.setBlockAt(x, y, z, materials[random.nextInt(materials.length)]);
                }
                if (both || wallAxis == MazeGenerator.WallAxis.VER) {
                    Material at = context.getBlockAt(context.before().ver());
                    if (at == Material.STONE || at == Material.ANDESITE && random.nextBoolean()) {
                        context.setBlockAt(x, y, z, Material.STONE);
                    } else context.setBlockAt(x, y, z, materials[random.nextInt(materials.length)]);
                }
            });

    public MazeGenerationModule(final MazeGamemode plugin) {
        super("maze_generation", plugin);
    }

    // -- GET / SET

    public MazeGenerator basicGenerator() {
        return basicGenerator;
    }
}
