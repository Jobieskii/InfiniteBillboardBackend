package com.github.jobieskii.public_place.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

public class TileId implements Serializable {
    private int x;
    private int y;
    private int level;
    public TileId(int x, int y, int level) {
        this.x = x;
        this.y = y;
        this.level = level;
    }

    public TileId() {
        this.x = 0;
        this.y = 0;
        this.level = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileId tileId = (TileId) o;
        return x == tileId.x && y == tileId.y && level == tileId.level;
    }

    public int getLevel() {
        return level;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, level);
    }
}
