package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

@Service
public class MetadataAnalyzer {

    /**
     * Analyze the metadata of a screenshot.
     *
     * @param screenshot the screenshot entity (we need the file path)
     * @return a map of metadata findings
     */
    public Map<String, Object> analyze(Screenshot screenshot) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> metadataList = new ArrayList<>();
        List<String> editingSoftwareSignatures = new ArrayList<>();
        boolean hasEditingSoftware = false;

        File file = new File(screenshot.getFilePath());
        if (!file.exists()) {
            result.put("error", "File not found");
            result.put("metadataStatus", "FAILED");
            return result;
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    Map<String, String> tagInfo = new HashMap<>();
                    tagInfo.put("directory", directory.getName());
                    tagInfo.put("tag", tag.getTagName());
                    tagInfo.put("description", tag.getDescription() == null ? "" : tag.getDescription());
                    metadataList.add(tagInfo);

                    // Check for known editing software signatures
                    String desc = tag.getDescription() == null ? "" : tag.getDescription().toLowerCase(java.util.Locale.ROOT);
                    if (desc.contains("photoshop") ||
                            desc.contains("gimp") ||
                            desc.contains("canva") ||
                            desc.contains("snapseed") ||
                            desc.contains("lightroom") ||
                            desc.contains("paint shop")) {
                        editingSoftwareSignatures.add(tag.getTagName() + ": " + tag.getDescription());
                        hasEditingSoftware = true;
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            // A valid image may have no supported metadata; report a module result,
            // rather than failing independent forensic modules.
            result.put("metadataStatus", "UNAVAILABLE");
            result.put("metadataMessage", "Metadata could not be extracted from this image format.");
        }

        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                result.put("width", image.getWidth());
                result.put("height", image.getHeight());
                result.put("resolution", image.getWidth() + " × " + image.getHeight() + " pixels");
                result.put("format", com.dfsa.util.FileUtil.getImageFormat(file));
                ColorModel colorModel = image.getColorModel();
                String colorMode;
                if (colorModel instanceof java.awt.image.IndexColorModel) {
                    colorMode = "Indexed color";
                } else if (colorModel.getNumColorComponents() == 1) {
                    colorMode = colorModel.hasAlpha() ? "Grayscale + alpha" : "Grayscale";
                } else {
                    colorMode = colorModel.hasAlpha() ? "RGBA" : "RGB";
                }
                result.put("colorMode", colorMode);
            }
        } catch (IOException e) {
            result.put("imagePropertiesStatus", "UNAVAILABLE");
        }
        result.put("metadata", metadataList);
        result.put("metadataStatus", metadataList.isEmpty() ? "UNAVAILABLE" : "AVAILABLE");
        if (metadataList.isEmpty()) result.put("metadataMessage", "No supported metadata was present; this alone is not evidence of editing.");
        result.put("hasEditingSoftware", hasEditingSoftware);
        result.put("editingSoftwareSignatures", editingSoftwareSignatures);
        result.put("metadataCount", metadataList.size());

        return result;
    }
}
