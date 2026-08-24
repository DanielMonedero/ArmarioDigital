package com.example.wardrobe.image.storage;

import com.example.wardrobe.common.exception.BadRequestException;
import com.example.wardrobe.common.exception.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileSystemStorage {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorage.class);

    private final Path rootPath;

    public FileSystemStorage(@Value("${app.storage.images-path}") String imagesPath) {
        this.rootPath = Paths.get(imagesPath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootPath);
            log.info("Image storage directory: {}", rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create storage directory: " + rootPath, e);
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("EMPTY_FILE", "Uploaded file is empty");
        }
        String original = file.getOriginalFilename();
        if (original == null) {
            original = "image";
        }
        String safeOriginal = sanitize(original);
        String extension = extensionFrom(safeOriginal);
        String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path target = rootPath.resolve(storedName).normalize();
        if (!target.startsWith(rootPath)) {
            throw new BadRequestException("INVALID_FILENAME", "Invalid filename");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InternalErrorException("Could not store file", e);
        }
        return new StoredFile(storedName, safeOriginal);
    }

    public Path resolve(String filename) {
        Path resolved = rootPath.resolve(filename).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new BadRequestException("INVALID_FILENAME", "Invalid filename");
        }
        return resolved;
    }

    public boolean delete(String filename) {
        try {
            return Files.deleteIfExists(resolve(filename));
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", filename, e.getMessage());
            return false;
        }
    }

    private String sanitize(String name) {
        String n = Paths.get(name).getFileName().toString();
        StringBuilder sb = new StringBuilder();
        for (char c : n.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private String extensionFrom(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        String ext = name.substring(dot + 1).toLowerCase();
        if (ext.length() > 10) {
            return "";
        }
        return ext;
    }

    public record StoredFile(String filename, String originalFilename) {
    }
}
