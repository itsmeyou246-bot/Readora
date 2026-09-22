package com.readora.readora.service;

import com.readora.readora.model.Book;
import com.readora.readora.model.Bookmark;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookmarkRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public Bookmark addBookmark(
            User user,
            Book book,
            int pageNumber,
            String title) {

        return bookmarkRepository
                .findByUserAndBookAndPageNumber(
                        user,
                        book,
                        pageNumber
                )
                .orElseGet(() ->
                        bookmarkRepository.save(
                                new Bookmark(
                                        user,
                                        book,
                                        pageNumber,
                                        title
                                )
                        )
                );
    }

    public boolean isBookmarked(
            User user,
            Book book,
            int pageNumber) {

        return bookmarkRepository
                .existsByUserAndBookAndPageNumber(
                        user,
                        book,
                        pageNumber
                );
    }

    @Transactional
    public void removeBookmark(
            User user,
            Book book,
            int pageNumber) {

        bookmarkRepository
                .deleteByUserAndBookAndPageNumber(
                        user,
                        book,
                        pageNumber
                );
    }

    @Transactional
    public void deleteBookmarkById(
            User user,
            Long id) {

        bookmarkRepository
                .deleteByUserAndId(
                        user,
                        id
                );
    }

    public List<Map<String, Object>> getUserBookmarks(
            User user) {

        return bookmarkRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(b -> {

                    Book book = b.getBook();

                    return Map.<String, Object>of(
                            "id",
                            b.getId(),

                            "bookId",
                            book.getId(),

                            "bookTitle",
                            book.getTitle(),

                            "bookAuthor",
                            book.getAuthor(),

                            "bookCover",
                            book.getImage() != null
                                    ? book.getImage()
                                    : "img/Book.png",

                            "page",
                            "Page " +
                                    b.getPageNumber(),

                            "pageNumber",
                            b.getPageNumber(),

                            "savedDate",
                            b.getCreatedAt()
                                    .toString()
                                    .substring(0, 10),

                            "link",
                            "/book-reader?bookId=" +
                                    book.getId()
                    );

                })
                .toList();
    }
}