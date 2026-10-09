package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class TamperHeuristics {

    /**
     * Analyze tampering heuristics for a screenshot.
     *
     * @param screenshot the screenshot entity
     * @return a map of tampering heuristic results
     * @throws IOException if an error occurs during image processing
     */
    public Map<String, Object> analyze(Screenshot screenshot) throws IOException {
        Map<String, Object> result = new HashMap<>();
        File file = new File(screenshot.getFilePath());
        if (!file.exists()) {
            throw new IOException("Image unavailable for review");
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Image could not be decoded for review");
        }

        result.put("status", "NOT_ASSESSED");
        result.put("width", image.getWidth());
        result.put("height", image.getHeight());
        result.put("limitation", "No validated manipulation classifier is enabled. Visual texture and compression heuristics are not treated as proof or converted into a tampering probability.");

        return result;
    }
}
