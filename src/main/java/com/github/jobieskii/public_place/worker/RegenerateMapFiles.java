package com.github.jobieskii.public_place.worker;

import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.model.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RegenerateMapFiles {
    static Logger logger = LoggerFactory.getLogger(RegenerateMapFiles.class);

    public static void RegenerateUpFromTilesList(List<Tile> tiles) {
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
}
