// src/main/java/com/gymsystem/documents/DocumentController.java
package com.gymsystem.documents;

import com.gymsystem.documents.dto.UploadResponse;
import com.gymsystem.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

/** User-facing endpoints for managing personal documents. */
@RestController
@RequestMapping("/api/v1/profile/documents")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DocumentController {

    private final DocumentService service;
    private final LocalStorageService storage;
    private final UserDocumentRepository repository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserDocument>> listMy() {
        return ResponseEntity.ok(service.myDocuments());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestPart("title") @NotBlank String title,
            @RequestPart("category") @NotBlank String category,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        return ResponseEntity.ok(service.uploadMy(title, category, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        service.deleteMy(id);
        return ResponseEntity.noContent().build();
    }

    /** Optional: direct download by id if owner; otherwise use admin route. */
    @GetMapping("/{id}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) {
        var doc = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Document not found"));

        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        var me = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
        if (!doc.getUser().getId().equals(me.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Path path = storage.resolve(doc.getStoragePath());
        var resource = new FileSystemResource(path.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getTitle() + "\"")
                .body(resource);
    }
}
