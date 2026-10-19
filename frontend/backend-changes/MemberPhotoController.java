package com.purohitcommunity.portal.web;

import com.purohitcommunity.portal.service.PortalService;
import java.io.IOException;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MemberPhotoController {
  private final PortalService portal;
  private final Path root;
  public MemberPhotoController(PortalService portal, @Value("${app.storage.private-dir:./uploads/private}") String directory) {
    this.portal = portal;
    this.root = Paths.get(directory).toAbsolutePath().normalize();
  }
  @GetMapping("/api/admin/members/{id}/passport-photo")
  public ResponseEntity<Resource> adminPhoto(@PathVariable Long id) throws IOException {
    return photo(portal.member(id).passportPhotoPath());
  }
  @GetMapping("/api/member/passport-photo")
  public ResponseEntity<Resource> memberPhoto(Authentication authentication) throws IOException {
    return photo(portal.memberView(portal.current(authentication.getName())).passportPhotoPath());
  }
  private ResponseEntity<Resource> photo(String storedPath) throws IOException {
    if (storedPath == null || storedPath.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    Path path = Paths.get(storedPath).toAbsolutePath().normalize();
    if (!path.startsWith(root) || !Files.isRegularFile(path) || !path.toRealPath().startsWith(root.toRealPath()))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    String filename = path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
    MediaType type;
    if (filename.endsWith(".png")) type = MediaType.IMAGE_PNG;
    else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) type = MediaType.IMAGE_JPEG;
    else throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    return ResponseEntity.ok().contentType(type).cacheControl(CacheControl.noStore())
      .header("X-Content-Type-Options", "nosniff").body(new FileSystemResource(path));
  }
}
