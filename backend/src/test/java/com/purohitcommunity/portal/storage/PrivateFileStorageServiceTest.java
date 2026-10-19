package com.purohitcommunity.portal.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

class PrivateFileStorageServiceTest {
    @TempDir Path directory;

    @Test void returnsStoredPhotoWithPrivateCacheHeaders() throws Exception {
        var storage = new PrivateFileStorageService(directory.toString());
        var photo = directory.resolve("passport-photo.png");
        byte[] bytes = {1, 2, 3};
        Files.write(photo, bytes);
        var response = storage.passportPhoto(photo.toString());
        assertArrayEquals(bytes, response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertEquals("no-store", response.getHeaders().getCacheControl());
    }

    @Test void rejectsMissingPhotosAndPathsOutsideStorage() throws Exception {
        var root = directory.resolve("private");
        var storage = new PrivateFileStorageService(root.toString());
        var outside = directory.resolve("outside.jpg");
        Files.write(outside, new byte[]{1});
        assertThrows(NoSuchElementException.class, () -> storage.passportPhoto(outside.toString()));
        assertThrows(NoSuchElementException.class, () -> storage.passportPhoto(null));
        assertThrows(NoSuchElementException.class, () -> storage.passportPhoto(root.resolve("missing.png").toString()));
    }
}
