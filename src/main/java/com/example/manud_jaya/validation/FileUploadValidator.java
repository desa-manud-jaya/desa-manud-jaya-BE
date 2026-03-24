package com.example.manud_jaya.validation;

import com.example.manud_jaya.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
@Slf4j
public class FileUploadValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MIN_WIDTH = 100;
    private static final int MAX_WIDTH = 4000;
    private static final int MIN_HEIGHT = 100;
    private static final int MAX_HEIGHT = 4000;

    public void validateFile(MultipartFile file) throws FileUploadException {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds 5MB limit");
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("File must be an image");
        }

        // Validate image dimensions
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new FileUploadException("Invalid image file");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width < MIN_WIDTH || width > MAX_WIDTH || height < MIN_HEIGHT || height > MAX_HEIGHT) {
                throw new FileUploadException(
                        String.format(
                                "Image dimensions must be between %dx%d and %dx%d. Got %dx%d",
                                MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT, width, height
                        )
                );
            }

            log.info("File validation passed: {}x{}", width, height);
        } catch (IOException e) {
            throw new FileUploadException("Failed to validate image", e);
        }
    }
}
