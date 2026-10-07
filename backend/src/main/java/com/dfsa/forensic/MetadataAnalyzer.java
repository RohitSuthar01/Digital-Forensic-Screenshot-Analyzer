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
            return result;
        }

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    Map<String, String> tagInfo = new HashMap<>();
                    tagInfo.put("directory", directory.getName());
                    tagInfo.put("tag", tag.getTagName());
                    tagInfo.put("description", tag.getDescription());
                    metadataList.add(tagInfo);

                    // Check for known editing software signatures
                    String desc = tag.getDescription().toLowerCase();
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
            result.put("error", "Failed to read metadata: " + e.getMessage());
            return result;
        }

        result.put("metadata", metadataList);
        result.put("hasEditingSoftware", hasEditingSoftware);
        result.put("editingSoftwareSignatures", editingSoftwareSignatures);
        result.put("metadataCount", metadataList.size());

        return result;
    }
}