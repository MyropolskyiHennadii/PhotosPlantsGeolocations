package photos.service;

import com.drew.imaging.ImageProcessingException;
import photos.model.ImageFileWithMetadata;

import java.io.File;

public interface ImageFilesSelection {

    static ImageFileWithMetadata imageWithMetadata(File file) {
        ImageFileWithMetadata imageFile = null;
        try {
            imageFile = new ImageFileWithMetadata(file);
        } catch (ImageProcessingException e) {
            return null;
        }
        return imageFile;
    }

    boolean checkTags(ImageFileWithMetadata file);
}
