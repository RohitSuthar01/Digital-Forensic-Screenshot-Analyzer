package com.dfsa.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {
    @TempDir Path tempDir;

    @Test
    void acceptsDecodablePngByContent() throws Exception {
        File image = tempDir.resolve("image.bin").toFile();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image);
        assertEquals("png", FileUtil.getImageFormat(image));
        assertTrue(FileUtil.isAllowedFileType(image));
    }

    @Test
    void rejectsTruncatedAndUnsupportedContent() throws Exception {
        File image = tempDir.resolve("broken.png").toFile();
        java.nio.file.Files.write(image.toPath(), new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
        assertFalse(FileUtil.isAllowedFileType(image));
        File unknown = tempDir.resolve("unknown.bin").toFile();
        java.nio.file.Files.write(unknown.toPath(), new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        assertNull(FileUtil.getImageFormat(unknown));
    }
}
