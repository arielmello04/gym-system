// src/main/java/com/gymsystem/documents/DocumentService.java
package com.gymsystem.documents;

import com.gymsystem.documents.dto.UploadResponse;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/** Handles validation, persistence and storage operations for user documents. */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Set<String> ALLOWED_MIME = Set.of(
            MediaType.APPLICATION_PDF_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE
    );

    private final UserDocumentRepository repository;
    private final LocalStorageService storage;
    private final UserRepository userRepository;

    /** Lists current user's documents ordered by most recent. */
    public List<UserDocument> myDocuments() {
        User me = currentUser();
        return repository.findByUserIdOrderByUploadedAtDesc(me.getId());
    }

    /** Admin lists documents from a given user id. */
    public List<UserDocument> listByUser(Long userId) {
        return repository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    /** Uploads a file for the current user and returns a small response. */
    @Transactional
    public UploadResponse uploadMy(String title, String category, MultipartFile file) throws Exception {
        User me = currentUser();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String mime = detectMime(file);
        if (!ALLOWED_MIME.contains(mime)) {
            throw new IllegalArgumentException("Unsupported file type");
        }

        // Persist bytes first to disk, then metadata to DB
        String relativePath = storage.save(file.getBytes(), file.getOriginalFilename());
        var now = Instant.now();

        UserDocument doc = UserDocument.builder()
                .user(me)
                .title(title)
                .category(category)
                .mimeType(mime)
                .sizeBytes(file.getSize())
                .storagePath(relativePath)
                .uploadedAt(now)
                .uploadedBy(me)
                .build();

        var saved = repository.save(doc);
        return new UploadResponse(saved.getId(), saved.getTitle(), saved.getCategory(), saved.getMimeType(), saved.getSizeBytes(), saved.getUploadedAt());
    }

    /** Soft authorization: only owner or admin should be allowed (admin route uses separate controller). */
    @Transactional
    public void deleteMy(Long documentId) throws Exception {
        User me = currentUser();
        var doc = repository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!doc.getUser().getId().equals(me.getId())) {
            throw new SecurityException("Not allowed to delete this document");
        }
        storage.deleteIfExists(doc.getStoragePath());
        repository.delete(doc);
    }

    private User currentUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    /** Uses provided content type and probes as a fallback to avoid spoofed headers. */
    private String detectMime(MultipartFile file) throws Exception {
        String headerType = file.getContentType();
        if (headerType != null && ALLOWED_MIME.contains(headerType)) return headerType;
        String probed = Files.probeContentType(file.getResource().getFile().toPath());
        return probed != null ? probed : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
