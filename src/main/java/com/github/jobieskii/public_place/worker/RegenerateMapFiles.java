package com.github.jobieskii.public_place.worker;

import com.github.jobieskii.public_place.controller.TileController;
import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.model.TileStruct;
import com.github.jobieskii.public_place.model.Update;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.repository.UpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jobieskii.public_place.PublicPlaceApplication.MAX_LEVEL;
import static com.github.jobieskii.public_place.file_manager.FileManager.mapfiles_path;

public class RegenerateMapFiles {
    static Logger logger = LoggerFactory.getLogger(RegenerateMapFiles.class);

    public static void AddUnknownFilesToDb(TileRepository tileRepository, UpdateRepository updateRepository) throws IOException {
        Path path = Paths.get(mapfiles_path + "/1");
        OffsetDateTime now = OffsetDateTime.now();
        Map<TileStruct, Tile> tiles = tileRepository.findByLevel(1).stream().collect(Collectors.toMap(TileStruct::new, Function.identity()));
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".png"))
                    .forEach((f) -> {
                        Path rel = path.relativize(f);
                        String[] xy = rel.toString().split("/");
                        int x = Integer.parseInt(xy[0]);
                        int y = Integer.parseInt(xy[1].split("\\.")[0]);
                        Tile t = tiles.get(new TileStruct(x, y, 1));
                        if (t == null) {
                            logger.info("Found orphaned image file at {}/{}, adding to database.", x, y);
                            TileController.ExpandTilesFor(x, y, MAX_LEVEL, tileRepository, null);
                            t = tileRepository.findFirstByXAndYAndLevel(x, y, 1);
                            Update update = new Update(null, now, t, -2);
                            updateRepository.save(update);
                        }
                    });
        }
    }

    public static void RegenerateUpFromBottom(TileRepository tileRepository) {
        for (int i = MAX_LEVEL; i >= 1; i--) {
            logger.info("Looking for parents at level {}", i);
            tileRepository.findByLevel(i).forEach(tile -> {
                if (tile.getParent() == null) {
                    regenerateParentTileInDb(tile.getX(), tile.getY(), tile.getLevel(), tileRepository);
                }
            });
        }

        AtomicInteger sumRegenerated = new AtomicInteger();
        for (int i = 2; i <= MAX_LEVEL; i++) {
            logger.info("Regenerating tiles at level {}", i);
            tileRepository.findByLevel(i).forEach(tile -> {
                try {
                    FileManager.regenerateFromLowerLevel(tile.getLevel(), tile.getX(), tile.getY());
                    sumRegenerated.incrementAndGet();
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
        }
        logger.info("Regenerated {} tiles", sumRegenerated);
    }

    private static void regenerateParentTileInDb(int x, int y, int level, TileRepository tileRepository) {
        if (level > MAX_LEVEL) {
            return;
        }

        Tile parentT = tileRepository.findFirstByXAndYAndLevel(Math.floorDiv(x, 2), Math.floorDiv(y, 2), level + 1);

        Tile newT = tileRepository.findFirstByXAndYAndLevel(x, y, level);
        if (newT != null) {
            newT.setParent(parentT);
            tileRepository.save(newT);
        } else {
            logger.error("No tile found at regenerateParentTileInDb.");
        }

    }
}
