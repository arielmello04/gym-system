// src/main/java/com/gymsystem/booking/AdminClassTypeController.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.CreateClassTypeRequest;
import com.gymsystem.booking.dto.UpdateClassTypeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin endpoints to manage class types (create/list/update). */
@RestController
@RequestMapping("/api/v1/admin/classes/types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminClassTypeController {

    private final ClassTypeRepository repo;

    @PostMapping
    public ResponseEntity<ClassType> create(@Valid @RequestBody CreateClassTypeRequest req) {
        // Enforce uppercase code to keep data consistent
        String code = req.getCode().trim().toUpperCase();

        // Defensive: validate uniqueness at app-level (DB also has unique index)
        if (repo.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Class type code already exists: " + code);
        }

        ClassType ct = ClassType.builder()
                .code(code)
                .name(req.getName().trim())
                .description(req.getDescription())
                .active(req.getActive() == null ? true : req.getActive())
                .build();

        return ResponseEntity.ok(repo.save(ct));
    }

    @GetMapping
    public ResponseEntity<List<ClassType>> listAll() {
        // Admin sees all (active and inactive)
        return ResponseEntity.ok(repo.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassType> update(@PathVariable Long id,
                                            @Valid @RequestBody UpdateClassTypeRequest req) {
        ClassType ct = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Class type not found"));
        // Only update provided fields
        if (req.getName() != null) ct.setName(req.getName());
        if (req.getDescription() != null) ct.setDescription(req.getDescription());
        if (req.getActive() != null) ct.setActive(req.getActive());
        return ResponseEntity.ok(repo.save(ct));
    }
}
