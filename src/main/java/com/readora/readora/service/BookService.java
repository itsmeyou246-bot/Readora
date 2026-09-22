package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /*
     * =========================================================
     * GET BOOKS FOR READORA BOOKS PAGE
     * =========================================================
     *
     * The Books page should display actual book records.
     *
     * We deliberately use:
     *
     * type = book
     * status = published
     *
     * This prevents magazine/newspaper records from being
     * mixed into the book catalog.
     */
    public List<Book> getAllBooks() {

        return bookRepository
                .findByTypeIgnoreCaseAndStatusIgnoreCase(
                        "book",
                        "published"
                );
    }

    /*
     * =========================================================
     * GET ONE BOOK
     * =========================================================
     */
    public Optional<Book> getBookById(Long id) {

        return bookRepository.findById(id);
    }

    /*
     * =========================================================
     * SEARCH BOOKS
     * =========================================================
     */
    public List<Book> searchBooks(String keyword) {

        List<Book> books =
                bookRepository
                        .findByTypeIgnoreCaseAndStatusIgnoreCase(
                                "book",
                                "published"
                        );

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return books;
        }

        String search =
                keyword.trim().toLowerCase();

        return books.stream()
                .filter(book ->

                        (book.getTitle() != null &&
                                book.getTitle()
                                        .toLowerCase()
                                        .contains(search))

                                ||

                                (book.getAuthor() != null &&
                                        book.getAuthor()
                                                .toLowerCase()
                                                .contains(search))

                                ||

                                (book.getCategory() != null &&
                                        book.getCategory()
                                                .toLowerCase()
                                                .contains(search))

                                ||

                                (book.getLanguage() != null &&
                                        book.getLanguage()
                                                .toLowerCase()
                                                .contains(search))
                )
                .toList();
    }
}