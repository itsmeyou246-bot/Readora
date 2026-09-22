package com.readora.readora.controller;

import com.readora.readora.dto.NoteRequest;
import com.readora.readora.model.Book;
import com.readora.readora.model.Note;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.service.LibraryService;
import com.readora.readora.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final LibraryService libraryService;
    private final BookRepository bookRepository;

    public NoteController(NoteService noteService,
                          LibraryService libraryService,
                          BookRepository bookRepository) {
        this.noteService = noteService;
        this.libraryService = libraryService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNotes(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(noteService.getUserNotes(user));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Note>> getNotesForBook(@PathVariable Long bookId,
                                                      Authentication authentication) {
        User user = currentUser(authentication);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        return ResponseEntity.ok(noteService.getNotesForBook(user, book));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNote(@RequestBody NoteRequest request,
                                                          Authentication authentication) {
        User user = currentUser(authentication);
        if (request.getBookId() == null || request.getPageNumber() == null || request.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book ID, Page Number, and Content are required.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        Note note = noteService.addNote(user, book, request.getPageNumber(), request.getContent());
        return ResponseEntity.ok(Map.of(
                "id", note.getId(),
                "bookId", book.getId(),
                "pageNumber", note.getPageNumber(),
                "content", note.getContent(),
                "message", "Note saved."
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNote(@PathVariable Long id,
                                                          @RequestBody Map<String, String> body,
                                                          Authentication authentication) {
        User user = currentUser(authentication);
        String content = body.get("content");
        Note updated = noteService.updateNote(user, id, content);

        return ResponseEntity.ok(Map.of(
                "id", updated.getId(),
                "content", updated.getContent(),
                "message", "Note updated."
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable Long id,
                                                          Authentication authentication) {
        User user = currentUser(authentication);
        noteService.deleteNote(user, id);
        return ResponseEntity.ok(Map.of("message", "Note deleted."));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return libraryService.user(authentication.getName());
    }
}
