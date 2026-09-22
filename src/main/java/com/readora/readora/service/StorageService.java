package com.readora.readora.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    // Local storage path for uploads
    private static final String UPLOAD_DIR = "uploads";

    public StorageService() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Could not initialize upload directory: " + e.getMessage());
        }
    }

    public String storeFile(MultipartFile file, String subfolder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path targetDir = Paths.get(UPLOAD_DIR, subfolder != null ? subfolder : "");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + (subfolder != null ? subfolder + "/" : "") + filename;
    }
}
