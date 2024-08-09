package com.github.jobieskii.public_place.file_manager;

import lombok.Getter;

import java.awt.image.BufferedImage;

@Getter
public record PatchData(BufferedImage image, int offsetXPx, int offsetYPx) {

}
