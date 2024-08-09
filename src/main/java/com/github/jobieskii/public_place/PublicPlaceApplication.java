package com.github.jobieskii.public_place;

import com.github.jobieskii.public_place.file_manager.FileManager;
import com.github.jobieskii.public_place.model.Tile;
import com.github.jobieskii.public_place.repository.TileRepository;
import com.github.jobieskii.public_place.worker.RegenerateMapFiles;
import com.github.jobieskii.public_place.worker.TileWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@SpringBootApplication
public class PublicPlaceApplication implements CommandLineRunner {

	private final TileRepository tileRepository;
	private TaskExecutor taskExecutor;
	private AtomicBoolean threadIsRunning = new AtomicBoolean(true);

    public PublicPlaceApplication(TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public static void main(String[] args) {
		SpringApplication.run(PublicPlaceApplication.class, args);
	}

	@Override
	public void run(String...args) throws Exception {
        List<Tile> tiles = tileRepository.findByLevel(1);
		RegenerateMapFiles.RegenerateUpFromTilesList(tiles);

//		FileManager.regenerateFromLowerLevel(2, 0, 0);
	}

}
