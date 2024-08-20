package com.github.jobieskii.public_place.controller;

import com.github.jobieskii.public_place.component.SessionValidator;
import com.github.jobieskii.public_place.component.UserRateLimiter;
import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.file_manager.PatchData;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.model.TileStruct;
import com.github.jobieskii.public_place.model.Update;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.repository.UpdateRepository;
import com.github.jobieskii.public_place.worker.TileWorker;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.github.jobieskii.public_place.PublicPlaceApplication.MAX_LEVEL;
import static com.github.jobieskii.public_place.PublicPlaceApplication.TILE_SIZE;

@CrossOrigin(origins = "https://bib.localhost.com", maxAge = 3600, allowCredentials = "true", methods = {RequestMethod.GET, RequestMethod.PATCH})
@RestController
@RequestMapping("/")
public class TileController {
    public static final int LIMIT_IDX = 500000/TILE_SIZE;
    private final TileRepository tileRepository;
    private final UpdateRepository updateRepository;
    private final SessionValidator sessionValidator;
    private final UserRateLimiter userRateLimiter;

    public TileController(TileRepository tileRepository, UpdateRepository updateRepository, SessionValidator sessionValidator, UserRateLimiter userRateLimiter) {
        this.tileRepository = tileRepository;
        this.updateRepository = updateRepository;
        this.sessionValidator = sessionValidator;
        this.userRateLimiter = userRateLimiter;
    }

    @GetMapping("session")
    public ResponseEntity getSessionStatus(@Nullable @CookieValue("sessionid") String sessionid) {
        if (sessionid == null) {
            return ResponseEntity.badRequest().build();
        }
        SessionValidator.UserData user = sessionValidator.checkSession(sessionid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PatchMapping("tiles/{xPx}/{yPx}")
    public ResponseEntity patchTiles(
            @PathVariable int xPx,
            @PathVariable int yPx,
            @RequestPart("image") MultipartFile file,
            @RequestParam("scale") Float scale,
            @Nullable @CookieValue("sessionid") String sessionid
    ) throws IOException {
        if (sessionid == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        SessionValidator.UserData user = sessionValidator.checkSession(sessionid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!userRateLimiter.tryAccess(user.id())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (scale == null) {
            scale = 1.0f;
        }
        BufferedImage image = ImageIO.read(file.getInputStream());
        int width = (int) (image.getWidth() * scale);
        int height = (int) (image.getHeight() * scale);
        if (width > 1024 || height > 1024 || image.getWidth() > 10240 || image.getHeight() > 10240 || file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().build();
        }
        if (scale != 1.0f) {
            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledImage.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            image = scaledImage;
        }
        List<Pair<Tile, PatchData>> tobePatched = new ArrayList<>();

        int widthSoFar = 0;
        for (int i = Math.floorDiv(xPx, TILE_SIZE); i <= Math.floorDiv((xPx + width), TILE_SIZE); i++) {

            int dx = 0;
            if (xPx > i * TILE_SIZE) {
                dx = xPx % TILE_SIZE;
                if (dx < 0) {
                    dx += TILE_SIZE;
                }
            }
            int dw = TILE_SIZE - dx;
            if (dx + width - widthSoFar <= TILE_SIZE) {
                dw = width - widthSoFar;
            }

            int heightSoFar = 0;
            for (int j = Math.floorDiv(yPx, TILE_SIZE); j <= Math.floorDiv((yPx + height), TILE_SIZE); j++) {
                int dy = 0;
                if (yPx > j * TILE_SIZE) {
                    dy = yPx % TILE_SIZE;
                    if (dy < 0) {
                        dy += TILE_SIZE;
                    }
                }
                int dh = TILE_SIZE - dy;
                if (dy + height - heightSoFar <= TILE_SIZE) {
                    dh = height - heightSoFar;
                }
                Tile t = tileRepository.findFirstByXAndYAndLevel(i, j, 1);
                if (t == null) {
                    ExpandTilesFor(i, j, MAX_LEVEL, tileRepository, null);
                    t = tileRepository.findFirstByXAndYAndLevel(i, j, 1);
                }
                if (t.getProtectedFor() == null &&
                    i <= LIMIT_IDX && i >= -LIMIT_IDX &&
                    j <= LIMIT_IDX && j >= -LIMIT_IDX
                ) {
                    tobePatched.add(Pair.of(t, new PatchData(image.getSubimage(widthSoFar, heightSoFar, dw, dh), dx, dy)));
                }

                heightSoFar += dh;
            }
            widthSoFar += dw;
        }
        if (tobePatched.isEmpty()) {
            return ResponseEntity.status(HttpStatus.LOCKED).build();
        } else tobePatched.forEach(e -> {
            TileWorker.addToPatchQueue(new TileStruct(e.getFirst()), e.getSecond());
            Update u = new Update(null, OffsetDateTime.now(), e.getFirst(), user.id());
            updateRepository.save(u);
        });

        return ResponseEntity.ok().build();
    }

    @GetMapping("tiles") //TODO: remove this or protect
    public List<Tile> getTiles() {
        ArrayList<Tile> arr = new ArrayList<>();
        tileRepository.findAll().forEach(arr::add);
        return arr;
    }

    public static void ExpandTilesFor(int x, int y, int maxLevel, TileRepository tileRepository, Integer protectFor) {

        ExpandTilesForInner(x, y, maxLevel, 1, tileRepository, protectFor);
    }

    private static void ExpandTilesForInner(int x, int y, int maxLevel, int level, TileRepository tileRepository, Integer protectFor) {
        if (level > maxLevel) {
            return;
        }

        Tile parrentT = tileRepository.findFirstByXAndYAndLevel(Math.floorDiv(x, 2), Math.floorDiv(y, 2), level + 1);
        if (parrentT == null) {
            ExpandTilesForInner(Math.floorDiv(x, 2), Math.floorDiv(y, 2), maxLevel, level + 1, tileRepository, protectFor);
            parrentT = tileRepository.findFirstByXAndYAndLevel(Math.floorDiv(x, 2), Math.floorDiv(y, 2), level + 1);
        }
        Tile newT = new Tile(x, y, level, parrentT, protectFor);
        FileManager.createFile(level, x, y);
        tileRepository.save(newT);
    }
}
