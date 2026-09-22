package com.readora.readora.service;

import com.readora.readora.dto.ReviewRequest;
import com.readora.readora.dto.ReviewResponse;
import com.readora.readora.model.Book;
import com.readora.readora.model.Review;
import com.readora.readora.model.User;
import com.readora.readora.repository.BookRepository;
import com.readora.readora.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public ReviewResponse addOrUpdateReview(User user, ReviewRequest request) {
        if (request.getBookId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book ID is required.");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5 stars.");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        // Check if user already reviewed this book (Anti-duplicate logic)
        Review review = reviewRepository.findByUserAndBook(user, book)
                .orElseGet(() -> new Review(user, book, request.getRating(), request.getComment()));

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUpdatedAt(Instant.now());

        Review saved = reviewRepository.save(review);

        // Recalculate average rating for this book
        Double avgRating = reviewRepository.getAverageRatingForBook(book.getId());
        long count = reviewRepository.countByBook(book);

        book.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        book.setRatingCount((int) count);
        bookRepository.save(book);

        return ReviewResponse.fromEntity(saved);
    }

    public List<ReviewResponse> getReviewsForBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));

        return reviewRepository.findByBookOrderByCreatedAtDesc(book).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findByIdAndUser(reviewId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found or unauthorized."));

        Book book = review.getBook();
        reviewRepository.delete(review);

        // Recalculate average rating
        Double avgRating = reviewRepository.getAverageRatingForBook(book.getId());
        long count = reviewRepository.countByBook(book);

        book.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        book.setRatingCount((int) count);
        bookRepository.save(book);
    }
}
