package com.github.jobieskii.public_place.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Entity
@IdClass(TileId.class)
@Table(name = "tiles")
public class Tile {
    @Id
    private int x;
    @Id
    private int y;
    @Id
    private int level;

    @Nullable
    private Integer protectedFor;

    @Setter
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "parent_x", referencedColumnName = "x"),
            @JoinColumn(name = "parent_y", referencedColumnName = "y"),
            @JoinColumn(name = "parent_level", referencedColumnName = "level")
    })
    private Tile parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Tile> children;

    public Tile(int x, int y, int level, Tile parent, Integer protectedFor) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.parent = parent;
        this.protectedFor = protectedFor;
    }

    public Tile() {
        this.x = 0;
        this.y = 0;
        this.level = 0;
        this.parent = null;
        protectedFor = null;
    }

    @Override
    public String toString() {
        return "Tile{" +
                "level=" + level +
                ", x=" + x +
                ", y=" + y +
                ", protected=" + (protectedFor != null) +
                '}';
    }
}
