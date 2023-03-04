package io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze;

public class Vector3 {

    public int x, y, z;

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 hor() {
        return new Vector3(x, y, 0);
    }

    public Vector3 ver() {
        return new Vector3(0, y, z);
    }
}
