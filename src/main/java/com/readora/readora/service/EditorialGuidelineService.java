package com.readora.readora.service;

import com.readora.readora.model.EditorialGuideline;
import com.readora.readora.repository.EditorialGuidelineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditorialGuidelineService {

    private final EditorialGuidelineRepository repository;

    public EditorialGuidelineService(EditorialGuidelineRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> getGuidelinesForPage() {
        List<EditorialGuideline> reviewSteps =
                repository.findByCategoryIgnoreCaseAndActiveTrueOrderBySortOrderAsc("REVIEW_STEP");
        List<EditorialGuideline> preflight =
                repository.findByCategoryIgnoreCaseAndActiveTrueOrderBySortOrderAsc("PREFLIGHT");
        List<EditorialGuideline> sections =
                repository.findByCategoryIgnoreCaseAndActiveTrueOrderBySortOrderAsc("SECTION");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reviewSteps", toStepList(reviewSteps));
        result.put("preflightItems", toPreflightList(preflight));
        result.put("sections", toSectionList(sections));
        return result;
    }

    public List<EditorialGuideline> getAllActive() {
        return repository.findByActiveTrueOrderBySortOrderAsc();
    }

    public EditorialGuideline create(EditorialGuideline guideline) {
        if (guideline.getTitle() == null || guideline.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required.");
        }
        if (guideline.getCategory() == null || guideline.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required.");
        }
        if (guideline.getContent() == null) {
            guideline.setContent("");
        }
        return repository.save(guideline);
    }

    public EditorialGuideline update(Long id, EditorialGuideline incoming) {
        EditorialGuideline existing = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Guideline not found."));

        if (incoming.getTitle() != null && !incoming.getTitle().isBlank()) {
            existing.setTitle(incoming.getTitle().trim());
        }
        if (incoming.getContent() != null) {
            existing.setContent(incoming.getContent());
        }
        if (incoming.getCategory() != null && !incoming.getCategory().isBlank()) {
            existing.setCategory(incoming.getCategory().trim());
        }
        if (incoming.getSortOrder() != null) {
            existing.setSortOrder(incoming.getSortOrder());
        }
        existing.setActive(incoming.isActive());
        existing.setCheckedItem(incoming.isCheckedItem());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guideline not found.");
        }
        repository.deleteById(id);
    }

    public void seedDefaultsIfEmpty() {
        if (repository.countByCategoryIgnoreCase("REVIEW_STEP") == 0) {
            repository.save(new EditorialGuideline("REVIEW_STEP", "1. Intake Scan",
                    "Format, integrity, and plagiarism check (24–48 hrs).", 1, false));
            repository.save(new EditorialGuideline("REVIEW_STEP", "2. Curator Review",
                    "Editorial board qualitative analysis (3–5 days).", 2, false));
            repository.save(new EditorialGuideline("REVIEW_STEP", "3. Revisions (if any)",
                    "Author addresses editorial notes & comments.", 3, false));
            repository.save(new EditorialGuideline("REVIEW_STEP", "4. Typesetting",
                    "Digital reader optimization & device rendering.", 4, false));
            repository.save(new EditorialGuideline("REVIEW_STEP", "5. Final Approval",
                    "Signoff by Senior Literary Curator.", 5, false));
            repository.save(new EditorialGuideline("REVIEW_STEP", "6. Public Launch",
                    "Live in Store & Premium Library.", 6, false));
        }

        if (repository.countByCategoryIgnoreCase("PREFLIGHT") == 0) {
            repository.save(new EditorialGuideline("PREFLIGHT", "Manuscript is complete with all chapters", "", 1, true));
            repository.save(new EditorialGuideline("PREFLIGHT", "Title and author information match bio", "", 2, true));
            repository.save(new EditorialGuideline("PREFLIGHT", "Correct category and sub-genre selected", "", 3, true));
            repository.save(new EditorialGuideline("PREFLIGHT", "Language & script UTF-8 verified", "", 4, true));
            repository.save(new EditorialGuideline("PREFLIGHT", "Curated synopsis (150+ words) provided", "", 5, false));
            repository.save(new EditorialGuideline("PREFLIGHT", "High-res 300 DPI cover artwork attached", "", 6, false));
            repository.save(new EditorialGuideline("PREFLIGHT", "Originality & copyright declaration verified", "", 7, false));
        }
    }

    private List<Map<String, Object>> toStepList(List<EditorialGuideline> items) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EditorialGuideline g : items) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("title", g.getTitle());
            m.put("desc", g.getContent());
            list.add(m);
        }
        return list;
    }

    private List<Map<String, Object>> toPreflightList(List<EditorialGuideline> items) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EditorialGuideline g : items) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("label", g.getTitle());
            m.put("checked", g.isCheckedItem());
            list.add(m);
        }
        return list;
    }

    private List<Map<String, Object>> toSectionList(List<EditorialGuideline> items) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (EditorialGuideline g : items) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", g.getId());
            m.put("title", g.getTitle());
            m.put("content", g.getContent());
            m.put("category", g.getCategory());
            list.add(m);
        }
        return list;
    }
}
