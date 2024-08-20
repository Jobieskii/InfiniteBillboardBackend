package com.github.jobieskii.public_place.model;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.jobieskii.public_place.PublicPlaceApplication.MAX_LEVEL;

/*
This class is used only for calculations, Use Tile whenever persistence to the DB is required
 */
@Getter
public class TileStruct {
    private final int x;
    private final int y;
    private final int level;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileStruct that = (TileStruct) o;
        return x == that.x && y == that.y && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, level);
    }

    public TileStruct(int x, int y, int level) {
        this.x = x;
        this.y = y;
        this.level = level;
    }
    public TileStruct(Tile tile) {
        this.x = tile.getX();
        this.y = tile.getY();
        this.level = tile.getLevel();
    }

    @Override
    public String toString() {
        return "TileStruct{" +
                "x=" + x +
                ", y=" + y +
                ", level=" + level +
                '}';
    }

    public TileStruct getParent() {
        if (level >= MAX_LEVEL) {
            return null;
        }
        return new TileStruct(Math.floorDiv(this.x, 2), Math.floorDiv(this.y, 2), this.level + 1);
    }
    public List<TileStruct> getChildren() {
        List<TileStruct> children = new ArrayList<>();
        if (level <= 1) return children;
        int cx = this.x * 2;
        int cy = this.y * 2;
        children.add(new TileStruct(cx, cy, this.level - 1));
        children.add(new TileStruct(cx, cy + 1, this.level - 1));
        children.add(new TileStruct(cx + 1, cy, this.level - 1));
        children.add(new TileStruct(cx + 1, cy + 1, this.level - 1));
        return children;
    }

}
