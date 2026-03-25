package com.example.manud_jaya.validation;

import com.example.manud_jaya.exception.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUploadValidatorTest {

    private FileUploadValidator fileValidator;

    @BeforeEach
    void setUp() {
        fileValidator = new FileUploadValidator();
    }

    @Test
    void validateValidImageFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                createImageBytes(200, 200)
        );

        assertDoesNotThrow(() -> fileValidator.validateFile(file));
    }

    @Test
    void validateEmptyFileThrows() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(file));
    }

    @Test
    void validateNullFileThrows() {
        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(null));
    }

    @Test
    void validateNonImageFileThrows() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "text data".getBytes()
        );

        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(file));
    }

    @Test
    void validateOversizedFileThrows() {
        byte[] largeData = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeData
        );

        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(file));
    }

    @Test
    void validateTooSmallDimensionThrows() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "small.png",
                "image/png",
                createImageBytes(50, 50)
        );

        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(file));
    }

    @Test
    void validateNullContentTypeThrows() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                null,
                createImageBytes(120, 120)
        );

        assertThrows(FileUploadException.class, () -> fileValidator.validateFile(file));
    }

    private byte[] createImageBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
