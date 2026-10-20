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

    @Test void returnsPrivatePdfButDoesNotTreatItAsPassportPhoto() throws Exception {
        var storage = new PrivateFileStorageService(directory.toString());
        var file = directory.resolve("aadhaar.pdf");
        Files.writeString(file, "%PDF-1.4 test");
        var response = storage.document(file.toString());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("no-store", response.getHeaders().getCacheControl());
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertThrows(NoSuchElementException.class, () -> storage.passportPhoto(file.toString()));
    }

    @Test void documentsRejectOutsidePathsUnsupportedTypesAndOversizedFiles() throws Exception {
        var storage = new PrivateFileStorageService(directory.resolve("private").toString());
        var outside = directory.resolve("outside.pdf");
        Files.writeString(outside, "%PDF");
        assertThrows(NoSuchElementException.class, () -> storage.document(outside.toString()));
        var html = directory.resolve("private/unsafe.html");
        Files.writeString(html, "<script>alert(1)</script>");
        assertThrows(NoSuchElementException.class, () -> storage.document(html.toString()));
        var oversized = directory.resolve("private/large.pdf");
        Files.write(oversized, new byte[5 * 1024 * 1024 + 1]);
        assertThrows(NoSuchElementException.class, () -> storage.document(oversized.toString()));
        assertThrows(NoSuchElementException.class, () -> storage.document(null));
    }
}
