package com.github.jobieskii.public_place.model;

import jakarta.persistence.*;

import java.util.Set;

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

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "parent_x", referencedColumnName = "x"),
            @JoinColumn(name = "parent_y", referencedColumnName = "y"),
            @JoinColumn(name = "parent_level", referencedColumnName = "level")
    })
    private Tile parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Tile> children;

    public Tile(int x, int y, int level, Tile parent) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.parent = parent;
    }

    public Tile() {
        this.x = 0;
        this.y = 0;
        this.level = 0;
        this.parent = null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLevel() {
        return level;
    }

    public Tile getParent() {
        return parent;
    }
    public Set<Tile> getChildren() {return children;}

    @Override
    public String toString() {
        return "Tile{" +
                "level=" + level +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
