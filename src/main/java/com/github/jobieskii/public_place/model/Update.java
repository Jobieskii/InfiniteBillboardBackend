package com.github.jobieskii.public_place.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "updates")
public class Update {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime datetime;

    private int userId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "tile_x", referencedColumnName = "x"),
            @JoinColumn(name = "tile_y", referencedColumnName = "y"),
            @JoinColumn(name = "tile_level", referencedColumnName = "level")
    })
    private Tile tile;

    public Update(Long id, OffsetDateTime datetime, Tile tile, int userId) {
        this.id = id;
        this.datetime = datetime;
        this.userId = userId;
        this.tile = tile;
    }

    public Update() {
        this.id = 0L;
        this.datetime = OffsetDateTime.now();
        this.tile = null;
        this.userId = -1;
    }
}
