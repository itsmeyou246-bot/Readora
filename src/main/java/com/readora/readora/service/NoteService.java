package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.Note;
import com.readora.readora.model.User;
import com.readora.readora.repository.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public Note addNote(User user, Book book, int pageNumber, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note content cannot be empty.");
        }
        Note note = new Note(user, book, pageNumber, content.trim());
        return noteRepository.save(note);
    }

    public Note updateNote(User user, Long noteId, String content) {
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or unauthorized."));

        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note content cannot be empty.");
        }
        note.setContent(content.trim());
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    public void deleteNote(User user, Long noteId) {
        Note note = noteRepository.findByIdAndUser(noteId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found or unauthorized."));
        noteRepository.delete(note);
    }

    public List<Map<String, Object>> getUserNotes(User user) {
        return noteRepository.findByUserOrderByUpdatedAtDesc(user).stream().map(n -> {
            Book book = n.getBook();
            return Map.<String, Object>of(
                    "id", n.getId(),
                    "bookId", book.getId(),
                    "bookTitle", book.getTitle(),
                    "bookAuthor", book.getAuthor(),
                    "page", "Page " + n.getPageNumber(),
                    "pageNumber", n.getPageNumber(),
                    "noteText", n.getContent(),
                    "dateCreated", n.getUpdatedAt().toString().substring(0, 10),
                    "link", "/reader/" + book.getId()
            );
        }).toList();
    }

    public List<Note> getNotesForBook(User user, Book book) {
        return noteRepository.findByUserAndBookOrderByPageNumberAsc(user, book);
    }

    public long getNotesCountForUser(User user) {
        return noteRepository.countByUser(user);
    }

    public long getNotesCountForBook(User user, Book book) {
        return noteRepository.countByUserAndBook(user, book);
    }
}
