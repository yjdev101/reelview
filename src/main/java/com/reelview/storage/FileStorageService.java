package com.reelview.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String storeVideo(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        int dotIndex = originalFilename.lastIndexOf(".");
        String extension = originalFilename.substring(dotIndex);

        String uuid = UUID.randomUUID().toString();
        String newFilename = uuid + extension;

        Path targetPath = Paths.get(uploadDir, newFilename);

        try {
            Files.createDirectories(Paths.get(uploadDir));
            file.transferTo(targetPath.toAbsolutePath().toFile());
        } catch (IOException e) {
            throw new RuntimeException("영상 파일을 저장할 수 없습니다.", e);
        }

        return targetPath.toString();
    }
}
