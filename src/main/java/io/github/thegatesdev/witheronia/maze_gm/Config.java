package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.maple.Maple;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

public class Config {
    private static final PathMatcher CONTENT_FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.yml");

    private final Yaml yaml = new Yaml();
    private final Path mainConfigPath;
    private final Path dataPath;
    private DataMap mainConfig;


    public Config(Path dataPath) {
        this.mainConfigPath = dataPath.resolve("config.yml");
        this.dataPath = dataPath;
    }


    public void reloadMainConfig() throws RuntimeException {
        try {
            mainConfig = loadPath(mainConfigPath).requireOf(DataMap.class);
        } catch (Exception e) {
            mainConfig = null;
            throw new RuntimeException("Failed to load config file", e);
        }
    }

    public static Stream<Path> contentFilesForPath(Path path) throws IOException {
        return Files.find(path,
            1,
            (p, attr) -> attr.isRegularFile() && Files.isReadable(p) && CONTENT_FILE_MATCHER.matches(p),
            FileVisitOption.FOLLOW_LINKS);
    }

    public DataElement loadPath(Path path) throws IOException {
        final Object contents;
        try (var input = Files.newBufferedReader(path)) {
            contents = yaml.load(input);
        }
        return Maple.read(contents);
    }

    public Optional<Path> tryResolve(String inputPath) {
        try {
            return Optional.of(dataPath.resolve(inputPath));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    // -- GET / SET

    public Optional<DataMap> mainConfig() {
        return Optional.ofNullable(mainConfig);
    }
}
