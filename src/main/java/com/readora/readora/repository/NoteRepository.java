package com.readora.readora.repository;

import com.readora.readora.model.Book;
import com.readora.readora.model.Note;
import com.readora.readora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserOrderByUpdatedAtDesc(User user);

    List<Note> findByUserAndBookOrderByPageNumberAsc(User user, Book book);

    Optional<Note> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndBook(User user, Book book);
}
