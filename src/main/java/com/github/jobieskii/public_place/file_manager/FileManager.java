package com.github.jobieskii.public_place.file_manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.github.jobieskii.public_place.PublicPlaceApplication.TILE_SIZE;

public class FileManager {

    static Logger logger = LoggerFactory.getLogger(FileManager.class);


    private static String path = System.getenv("MAPFILES_PATH");

    public static void createFile(int level, int x, int y) {
        String filePath = String.format("%s/%d/%d/%d.png", path, level, x, y);
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    boolean b = parentDir.mkdirs();
                    if (!b) throw new IOException("Unable to create directory " + parentDir);
                }
                int width = TILE_SIZE;
                int height = TILE_SIZE;
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(Color.WHITE); // set the color to white
                g2d.fillRect(0, 0, width, height); // fill the image with white
                g2d.dispose();

                // Write the image to the file
                ImageIO.write(image, "png", file);
                logger.info("File created: {}", filePath);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.warn("File for {}/{}/{} already exists", level, x, y);
        }
    }

    public static void patchFile(int x, int y, BufferedImage image, int offsetXPx, int offsetYPx) {
        String filePath = String.format("%s/%d/%d/%d.png", path, 1, x, y);
        File file = new File(filePath);

        if (file.exists()) {
            try {
                BufferedImage oldImage = ImageIO.read(file);
                Graphics2D g2d = oldImage.createGraphics();
                g2d.drawImage(image, offsetXPx, offsetYPx, null);
                g2d.dispose();
                ImageIO.write(oldImage, "png", file);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("File for {}/{}/{} doesn't exist", 1, x, y);
        }
    }

    public static void regenerateFromLowerLevel(int level, int x, int y) throws Exception {
        if (level == 1) {
            throw new Exception("Can't regenerate for the lowest level");
        }
        String filePath = String.format("%s/%d/%d/%d.png", path, level, x, y);
        File file = new File(filePath);

        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            boolean b = parentDir.mkdirs();
            if (!b) throw new IOException("Unable to create directory " + parentDir);
        }

        BufferedImage newImage = new BufferedImage(TILE_SIZE*2, TILE_SIZE*2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();
        for (int cx = x*2; cx <= x*2 + 1; cx += 1) {
            for (int cy = y*2; cy <= y*2 + 1; cy += 1) {
                File lowerFile = new File(String.format("%s/%d/%d/%d.png", path, level-1, cx, cy));
                if (lowerFile.exists()) {
                    g2d.drawImage(ImageIO.read(lowerFile), (cx-x*2)*TILE_SIZE, (cy-y*2)*TILE_SIZE, null);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect( (cx-x*2)*TILE_SIZE, (cy-y*2)*TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    logger.info("File for {}/{}/{} doesn't exist", level-1, cx, cy);
                }
            }
        }
        g2d.dispose();

        BufferedImage scaledImage = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(newImage, 0, 0, TILE_SIZE, TILE_SIZE, null);
        g.dispose();

        ImageIO.write(scaledImage, "png", file);
        logger.info("Regenerated file at {}/{}/{}", level, x, y);
    }
}
