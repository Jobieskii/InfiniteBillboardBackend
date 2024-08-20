package com.github.jobieskii.public_place;

import com.github.jobieskii.public_place.controller.TileController;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.worker.RegenerateMapFiles;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class PublicPlaceApplication implements CommandLineRunner {

	public static int TILE_SIZE = 512;
	public static int MAX_LEVEL = 4;

	private final TileRepository tileRepository;

    public PublicPlaceApplication(TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(PublicPlaceApplication.class, args);
	}

	@Override
	public void run(String...args) {
		for (int i = -3; i<=2; i++) {
			for (int j = -3; j<=2; j++) {
				TileController.ExpandTilesFor(i, j, MAX_LEVEL, tileRepository, 1);
			}
		}
        List<Tile> tiles = tileRepository.findByLevel(1);
		RegenerateMapFiles.RegenerateUpFromTilesList(tiles);
	}

}
