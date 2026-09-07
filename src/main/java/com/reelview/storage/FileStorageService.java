package com.reelview.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".mp4", ".mov", ".avi");

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String storeVideo(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1 ) {
            throw new IllegalArgumentException("허용하지 않는 확장자입니다.");
        }

        String extension = originalFilename.substring(dotIndex);
        extension = extension.toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용하지 않는 확장자입니다.");
        }

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
