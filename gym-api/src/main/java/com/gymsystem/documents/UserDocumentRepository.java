// src/main/java/com/gymsystem/documents/UserDocumentRepository.java
package com.gymsystem.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByUserIdOrderByUploadedAtDesc(Long userId);
}
