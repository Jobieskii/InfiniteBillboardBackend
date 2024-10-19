package com.github.jobieskii.public_place;

import com.github.jobieskii.public_place.controller.TileController;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.worker.RegenerateMapFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class PublicPlaceApplication implements CommandLineRunner {

	public static int TILE_SIZE = 512;
	public static int MAX_LEVEL = 4;

	private final TileRepository tileRepository;
	@Value("${REGENERATE_ALL_TILES:1}")
	private String regenerateAllTiles;

	static Logger logger = LoggerFactory.getLogger(PublicPlaceApplication.class);

    public PublicPlaceApplication(TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(PublicPlaceApplication.class, args);
	}

	@Override
	public void run(String...args) {
		ImageIO.scanForPlugins();
		IIORegistry.getDefaultInstance().registerApplicationClasspathSpis();

		checkImageIO();

		if (regenerateAllTiles.equals("1")) {
			for (int i = -3; i<=2; i++) {
				for (int j = -3; j<=2; j++) {
					TileController.ExpandTilesFor(i, j, MAX_LEVEL, tileRepository, 1);
				}
			}
			List<Tile> tiles = tileRepository.findByLevel(1);
			RegenerateMapFiles.RegenerateUpFromTilesList(tiles, tileRepository);
		}
	}
	private static final String[] FORMATS = {"jpeg", "png", "webp", "gif", "tiff", "bmp"};
	private void checkImageIO() {
		for (String format : FORMATS) {
			Iterator<ImageReader> ir = ImageIO.getImageReadersByFormatName(format);
			if (!ir.hasNext()) {
				logger.warn("No image reader found for format {}", format);
			}
		}

	}
}
