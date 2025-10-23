// src/main/java/com/gymsystem/documents/AdminDocumentController.java
package com.gymsystem.documents;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

/** Admin endpoints to inspect/download a user's documents. */
@RestController
@RequestMapping("/api/v1/admin/users/{userId}/documents")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminDocumentController {

    private final DocumentService service;
    private final LocalStorageService storage;
    private final UserDocumentRepository repository;

    @GetMapping
    public ResponseEntity<List<UserDocument>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @GetMapping("/{docId}/download")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long userId, @PathVariable Long docId) {
        var doc = repository.findById(docId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!doc.getUser().getId().equals(userId)) {
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
