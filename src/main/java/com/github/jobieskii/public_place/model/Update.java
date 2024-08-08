package com.github.jobieskii.public_place.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "updates")
public class Update {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime datetime;

    private String ip;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "tile_x", referencedColumnName = "x"),
            @JoinColumn(name = "tile_y", referencedColumnName = "y"),
            @JoinColumn(name = "tile_level", referencedColumnName = "level")
    })
    private Tile tile;

    public Long getId() {
        return id;
    }

    public OffsetDateTime getDatetime() {
        return datetime;
    }

    public String getIp() {
        return ip;
    }

    public Tile getTile() {
        return tile;
    }

    public Update(Long id, OffsetDateTime datetime, String ip, Tile tile) {
        this.id = id;
        this.datetime = datetime;
        this.ip = ip;
        this.tile = tile;
    }

    public Update() {
        this.id = 0l;
        this.datetime = OffsetDateTime.now();
        this.ip = "";
        this.tile = null;
    }
}
