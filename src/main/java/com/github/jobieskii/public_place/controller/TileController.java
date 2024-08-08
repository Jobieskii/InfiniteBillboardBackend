package com.github.jobieskii.public_place.controller;

import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.repository.UpdateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/tiles")
public class TileController {
    public static final int TILE_SIZE = 512;
    private final TileRepository tileRepository;
    private final UpdateRepository updateRepository;

    public TileController(TileRepository tileRepository, UpdateRepository updateRepository) {
        this.tileRepository = tileRepository;
        this.updateRepository = updateRepository;
    }

    @PatchMapping("{xPx}/{yPx}")
    public ResponseEntity patchTiles(@PathVariable int xPx, @PathVariable int yPx, @RequestParam("image") MultipartFile file, @RequestParam("scale") Float scale) throws IOException {
        if (scale == null) {
            scale = 1.0f;
        }
        BufferedImage image = ImageIO.read(file.getInputStream());
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);
        if (width >= 1024 || height >= 1024) {
            return ResponseEntity.badRequest().build();
        }

        int x = xPx - xPx % TILE_SIZE;
        int y = yPx - yPx % TILE_SIZE;
        Set<Point> idxs = new HashSet<>();

        int widthSoFar = 0;
        for (int i = xPx / TILE_SIZE; i <= (xPx + width) / TILE_SIZE; i++) {

            int dx = 0;
            if (xPx > i*TILE_SIZE) {
                dx = xPx %TILE_SIZE;
            }
            int dw = TILE_SIZE - dx;
            if (dx + width - widthSoFar < TILE_SIZE) {
                dw = width - widthSoFar;
            }

            int heightSoFar = 0;
            for (int j = yPx / TILE_SIZE; j <= (yPx + height) / TILE_SIZE; j++) {
                int dy = 0;
                if (yPx > j*TILE_SIZE) {
                    dy = yPx %TILE_SIZE;
                }
                int dh = TILE_SIZE - dy;
                if (dy + height - heightSoFar < TILE_SIZE) {
                    dh = height - heightSoFar;
                }
                Tile t = tileRepository.findFirstByXAndYAndLevel(i, j, 1);
                if (t == null) {
                    this.ExpandTilesFor(i, j, 3);
                }
                FileManager.patchFile(i, j, image.getSubimage(widthSoFar, heightSoFar, dw, dh), dx, dy);

                idxs.add(new Point(i, j));
                heightSoFar += dh;
            }
            widthSoFar += dw;
        }

        return ResponseEntity.ok().build();
    }
    @GetMapping()
    public List<Tile> getTiles() {
        ArrayList<Tile> arr = new ArrayList<>();
        tileRepository.findAll().forEach(arr::add);
        return arr;
    }

    private void ExpandTilesFor(int x, int y, int maxLevel) {

        ExpandTilesForInner(x, y, maxLevel, 1);
    }
    private Tile ExpandTilesForInner(int x, int y, int maxLevel, int level) {
        if (level > maxLevel) {return null;}

        Tile parrentT = tileRepository.findFirstByXAndYAndLevel(x/2, y/2, level + 1);
        if (parrentT == null) {
            parrentT = ExpandTilesForInner(x/2, y/2, maxLevel, level+1);
        }
        Tile newT = new Tile(x, y, level, parrentT);
        tileRepository.save(newT);
        FileManager.createFile(level, x, y);
        return newT;
    }
}
