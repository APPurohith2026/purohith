package com.purohitcommunity.portal.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PrivateFileStorageService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> DOC_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private final Path root;

    public PrivateFileStorageService(@Value("${app.storage.private-dir:./uploads/private}") String privateDir) throws IOException {
        this.root = Paths.get(privateDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public String storeImage(MultipartFile file, String category) { return store(file, category, IMAGE_TYPES); }
    public String storeDocument(MultipartFile file, String category) { return store(file, category, DOC_TYPES); }

    public org.springframework.http.ResponseEntity<byte[]> passportPhoto(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) throw new java.util.NoSuchElementException();
        try {
            Path photo = Paths.get(storedPath).toAbsolutePath().normalize();
            if (!photo.startsWith(root) || !Files.isRegularFile(photo)
                    || !photo.toRealPath().startsWith(root.toRealPath())) {
                throw new java.util.NoSuchElementException();
            }
            String name = photo.getFileName().toString().toLowerCase(Locale.ROOT);
            var type = name.endsWith(".png") ? org.springframework.http.MediaType.IMAGE_PNG
                    : name.endsWith(".jpg") || name.endsWith(".jpeg") ? org.springframework.http.MediaType.IMAGE_JPEG : null;
            if (type == null || Files.size(photo) > MAX_BYTES) throw new java.util.NoSuchElementException();
            return org.springframework.http.ResponseEntity.ok().contentType(type)
                    .cacheControl(org.springframework.http.CacheControl.noStore())
                    .header("X-Content-Type-Options", "nosniff").body(Files.readAllBytes(photo));
        } catch (IOException | InvalidPathException e) {
            throw new java.util.NoSuchElementException();
        }
    }

    private String store(MultipartFile file, String category, Set<String> allowedTypes) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException(category + " is required");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException(category + " must be 5 MB or smaller");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!allowedTypes.contains(contentType)) throw new IllegalArgumentException(category + " has an unsupported file type");
        String ext = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
        String safeCategory = category.replaceAll("[^a-zA-Z0-9_-]", "_");
        String name = safeCategory + "-" + UUID.randomUUID() + ext;
        Path target = root.resolve(name).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid file path");
        try { file.transferTo(target); }
        catch (IOException e) { throw new IllegalStateException("Could not store " + category, e); }
        return target.toString();
    }
}
