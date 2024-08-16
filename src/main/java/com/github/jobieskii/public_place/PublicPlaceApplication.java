package com.github.jobieskii.public_place;

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

	private final TileRepository tileRepository;

    public PublicPlaceApplication(TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(PublicPlaceApplication.class, args);
	}

	@Override
	public void run(String...args) {
        List<Tile> tiles = tileRepository.findByLevel(1);
		RegenerateMapFiles.RegenerateUpFromTilesList(tiles);
	}

}
