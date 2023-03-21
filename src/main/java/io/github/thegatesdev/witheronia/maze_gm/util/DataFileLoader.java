package io.github.thegatesdev.witheronia.maze_gm.util;

import io.github.thegatesdev.maple.data.DataMap;

@FunctionalInterface
public interface DataFileLoader {
    void onDataFileLoad(DataMap data);
}
