package com.readora.readora.controller;

import com.readora.readora.model.EditorialGuideline;
import com.readora.readora.service.EditorialGuidelineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/editorial-guidelines")
public class EditorialGuidelineController {

    private final EditorialGuidelineService guidelineService;

    public EditorialGuidelineController(EditorialGuidelineService guidelineService) {
        this.guidelineService = guidelineService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUTHOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getGuidelines() {
        guidelineService.seedDefaultsIfEmpty();
        return ResponseEntity.ok(guidelineService.getGuidelinesForPage());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EditorialGuideline>> getAll() {
        return ResponseEntity.ok(guidelineService.getAllActive());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EditorialGuideline> create(@RequestBody EditorialGuideline guideline) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guidelineService.create(guideline));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EditorialGuideline> update(@PathVariable Long id,
                                                     @RequestBody EditorialGuideline guideline) {
        return ResponseEntity.ok(guidelineService.update(id, guideline));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        guidelineService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Guideline deleted."));
    }
}
