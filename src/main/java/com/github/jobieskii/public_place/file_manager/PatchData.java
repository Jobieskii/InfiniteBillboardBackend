package com.github.jobieskii.public_place.file_manager;

import java.awt.image.BufferedImage;

public class PatchData {
    private BufferedImage image;
    private int offsetXPx;
    private int offsetYPx;
    public PatchData(BufferedImage image, int offsetXPx, int offsetYPx) {
        this.image = image;
        this.offsetXPx = offsetXPx;
        this.offsetYPx = offsetYPx;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getOffsetXPx() {
        return offsetXPx;
    }

    public int getOffsetYPx() {
        return offsetYPx;
    }
}
