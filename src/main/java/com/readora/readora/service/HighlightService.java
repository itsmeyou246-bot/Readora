package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.Highlight;
import com.readora.readora.model.User;
import com.readora.readora.repository.HighlightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HighlightService {

    private final HighlightRepository highlightRepository;

    public HighlightService(HighlightRepository highlightRepository) {
        this.highlightRepository = highlightRepository;
    }

    public Highlight addHighlight(User user, Book book, int pageNumber, String text, String color) {
        if (text == null || text.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Highlighted text cannot be empty.");
        }
        Highlight highlight = new Highlight(user, book, pageNumber, text.trim(), color);
        return highlightRepository.save(highlight);
    }

    public List<Highlight> getHighlightsForBook(User user, Book book) {
        return highlightRepository.findByUserAndBookOrderByPageNumberAsc(user, book);
    }

    @Transactional
    public void deleteHighlight(User user, Long highlightId) {
        highlightRepository.deleteByUserAndId(user, highlightId);
    }
}
