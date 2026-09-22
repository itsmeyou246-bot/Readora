package com.readora.readora.service;

import com.readora.readora.dto.BookSearchRequest;
import com.readora.readora.model.Book;
import com.readora.readora.repository.BookRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookSearchService {

    private final BookRepository bookRepository;

    public BookSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> search(BookSearchRequest request) {

        Specification<Book> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ==========================================
            // 1. KEYWORD SEARCH
            // Title, Author, Category, Description
            // ==========================================
            if (request != null
                    && request.getQ() != null
                    && !request.getQ().trim().isEmpty()) {

                String term = "%" +
                        request.getQ().trim().toLowerCase() +
                        "%";

                Predicate titleMatch =
                        cb.like(
                                cb.lower(root.get("title")),
                                term
                        );

                Predicate authorMatch =
                        cb.like(
                                cb.lower(root.get("author")),
                                term
                        );

                Predicate categoryMatch =
                        cb.like(
                                cb.lower(root.get("category")),
                                term
                        );

                Predicate descriptionMatch =
                        cb.like(
                                cb.lower(root.get("description")),
                                term
                        );

                predicates.add(
                        cb.or(
                                titleMatch,
                                authorMatch,
                                categoryMatch,
                                descriptionMatch
                        )
                );
            }


            // ==========================================
            // 2. CATEGORY FILTER
            // ==========================================
            if (request != null
                    && request.getCategory() != null
                    && !request.getCategory().trim().isEmpty()) {

                String category =
                        request.getCategory().trim().toLowerCase();

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("category")),
                                category
                        )
                );
            }


            // ==========================================
            // 3. CONTENT TYPE FILTER
            // ==========================================
            if (request != null
                    && request.getContentType() != null
                    && !request.getContentType().trim().isEmpty()) {

                String type =
                        request.getContentType().trim().toLowerCase();

                if (type.equals("books")
                        || type.equals("book")) {

                    predicates.add(
                            cb.equal(
                                    cb.lower(root.get("type")),
                                    "book"
                            )
                    );

                } else if (type.equals("nepal")
                        || type.equals("read nepal")) {

                    Predicate byType =
                            cb.equal(
                                    cb.lower(root.get("type")),
                                    "nepal"
                            );

                    Predicate byCategory =
                            cb.equal(
                                    cb.lower(root.get("category")),
                                    "read nepal"
                            );

                    predicates.add(
                            cb.or(
                                    byType,
                                    byCategory
                            )
                    );

                } else {

                    predicates.add(
                            cb.equal(
                                    cb.lower(root.get("type")),
                                    type
                            )
                    );
                }
            }


            // ==========================================
            // 4. LANGUAGE FILTER
            // ==========================================
            if (request != null
                    && request.getLanguage() != null
                    && !request.getLanguage().trim().isEmpty()) {

                String language =
                        request.getLanguage().trim().toLowerCase();

                predicates.add(
                        cb.equal(
                                cb.lower(root.get("language")),
                                language
                        )
                );
            }


            // ==========================================
            // 5. FREE / PREMIUM FILTER
            // ==========================================
            if (request != null
                    && request.getPremium() != null
                    && !request.getPremium().trim().isEmpty()) {

                String premium =
                        request.getPremium().trim();

                if (premium.equalsIgnoreCase("free")) {

                    predicates.add(
                            cb.isFalse(
                                    root.get("premium")
                            )
                    );

                } else if (premium.equalsIgnoreCase("premium")) {

                    predicates.add(
                            cb.isTrue(
                                    root.get("premium")
                            )
                    );
                }
            }


            // ==========================================
            // 6. MINIMUM RATING
            // ==========================================
            if (request != null
                    && request.getMinRating() != null
                    && request.getMinRating() > 0) {

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("averageRating"),
                                request.getMinRating()
                        )
                );
            }


            // ==========================================
            // 7. ONLY PUBLISHED BOOKS
            // ==========================================
            //
            // This prevents draft/unpublished books
            // from appearing in public search results.
            //
            predicates.add(
                    cb.equal(
                            cb.lower(root.get("status")),
                            "published"
                    )
            );


            // ==========================================
            // RETURN ALL CONDITIONS
            // ==========================================
            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };


        // ==========================================
        // SORTING
        // ==========================================

        Sort sort =
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                );


        if (request != null
                && request.getSortBy() != null
                && !request.getSortBy().trim().isEmpty()) {

            String sortBy =
                    request.getSortBy()
                            .trim()
                            .toLowerCase();

            switch (sortBy) {

                case "rating":

                    sort = Sort.by(
                            Sort.Direction.DESC,
                            "averageRating"
                    );

                    break;


                case "title":

                    sort = Sort.by(
                            Sort.Direction.ASC,
                            "title"
                    );

                    break;


                case "priceasc":

                    sort = Sort.by(
                            Sort.Direction.ASC,
                            "price"
                    );

                    break;


                case "pricedesc":

                    sort = Sort.by(
                            Sort.Direction.DESC,
                            "price"
                    );

                    break;


                case "recent":

                default:

                    sort = Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    );

                    break;
            }
        }


        // ==========================================
        // EXECUTE SEARCH
        // ==========================================

        return bookRepository.findAll(
                spec,
                sort
        );
    }
}
