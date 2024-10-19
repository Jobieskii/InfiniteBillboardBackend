package com.github.jobieskii.public_place.worker;

import com.github.jobieskii.public_place.controller.TileController;
import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.repository.TileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.jobieskii.public_place.PublicPlaceApplication.MAX_LEVEL;

public class RegenerateMapFiles {
    static Logger logger = LoggerFactory.getLogger(RegenerateMapFiles.class);

    public static void RegenerateUpFromTilesList(List<Tile> tiles, TileRepository tileRepository) {
        tiles.forEach(tile -> {
            regenerateParentTileInDb(tile.getX(), tile.getY(), tile.getLevel(), tileRepository, tile.getProtectedFor());
        });

        Set<Tile> set = tiles.stream()
                .map(Tile::getParent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        while(!set.isEmpty()) {
            Set<Tile> parents = set.stream()
                .map(Tile::getParent)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            set.forEach(x -> {
                try {
                    FileManager.regenerateFromLowerLevel(x.getLevel(), x.getX(), x.getY());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            });
            set = parents;
        }
    }
    private static void regenerateParentTileInDb(int x, int y, int level, TileRepository tileRepository, Integer protectFor) {
        if (level > MAX_LEVEL) {
            return;
        }

        regenerateParentTileInDb(Math.floorDiv(x, 2), Math.floorDiv(y, 2), level + 1, tileRepository, protectFor);
        Tile parrentT = tileRepository.findFirstByXAndYAndLevel(Math.floorDiv(x, 2), Math.floorDiv(y, 2), level + 1);

        Tile newT = tileRepository.findFirstByXAndYAndLevel(x, y, level);
        if (newT == null) {
            newT = new Tile(x, y, level, parrentT, protectFor);
            tileRepository.save(newT);
        } else {
            newT.setParent(parrentT);
            tileRepository.save(newT);
        }

    }
}
