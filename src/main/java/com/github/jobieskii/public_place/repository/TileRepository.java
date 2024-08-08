package com.github.jobieskii.public_place.repository;

import com.github.jobieskii.public_place.model.Tile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TileRepository extends CrudRepository<Tile, Long> {
    Tile findFirstByXAndYAndLevel(int x, int y, int level);
    List<Tile> findByLevel(int level);
}
