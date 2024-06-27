package com.sesame.carpool_backend.service.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;


@Service
public class FileStorageService {
    private final Path rootPicturesUser = Paths.get("uploads/img/user/");
    private final Path rootDefaultPictures = Paths.get("uploads/img/default/");

    public void init() {
        try {
            Files.createDirectories(rootPicturesUser);
            Files.createDirectories(rootDefaultPictures);

            Path staticFolderPath = Paths.get(new ClassPathResource("static").getURI()).toAbsolutePath().normalize();

            Files.walk(staticFolderPath)
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        Path destination = rootDefaultPictures.resolve(staticFolderPath.relativize(source));
                        try {
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to copy files from static to uploads/default", e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload",e);
        }
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootPicturesUser.toFile());
    }

    public String storeImg(MultipartFile file) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = generateUniqueFilename(originalFilename);
            Files.copy(file.getInputStream(), rootPicturesUser.resolve(uniqueFilename));
            return uniqueFilename;
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Resource loadImgUser(String filename) {
        try {
            Path file = rootPicturesUser.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        long timestamp = System.currentTimeMillis();
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        return "img_" + timestamp + (StringUtils.hasText(fileExtension) ? "." + fileExtension : "");
    }

    public Resource loadDefaultImg(String filename) {
        try {
            Path file = rootDefaultPictures.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
