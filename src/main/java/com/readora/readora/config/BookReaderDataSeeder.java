package com.readora.readora.config;

import com.readora.readora.model.Book;
import com.readora.readora.model.BookPage;
import com.readora.readora.repository.BookPageRepository;
import com.readora.readora.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BookReaderDataSeeder {
    private static final long SAMPLE_BOOK_ID = 900001L;

    @Bean
    CommandLineRunner seedBookReader(BookRepository books, BookPageRepository pages) {
        return args -> {
            Book book = books.findById(SAMPLE_BOOK_ID).orElseGet(() ->
                    books.save(new Book(
                            SAMPLE_BOOK_ID,
                            "The Quiet Ridge",
                            "Readora Editorial Studio",
                            "the-quiet-ridge.pdf"
                    )));

            if (pages.countByBookId(book.getId()) > 0) {
                return;
            }

            pages.saveAll(List.of(
                    new BookPage(book, 1,
                            "At first light, the ridge was only a darker line against the pale sky. We followed it slowly, carrying warm tea, a paper map, and the kind of patience that belongs to long walks. Below us, the valley gathered its roofs and roads out of the mist.",
                            "The Ridge", null, null, null, null, null),
                    new BookPage(book, 2,
                            "The path narrowed where the grass gave way to stone. Each step made the world quieter. We began to notice small things: a feather caught in a thorn, water shining in a crack, the soft movement of clouds beyond the far hills.",
                            "The Ridge", "/img/library.png", "A room for returning", "Notes from the quiet places", "A library at the end of an afternoon, waiting for the next reader.", "Readora Editorial Studio"),
                    new BookPage(book, 3,
                            "By noon, the wind had cleared the last veil from the peaks. Nothing had changed below, yet everything looked newly arranged. We rested beside a low wall and understood that a journey does not need to be dramatic to become important.",
                            "The View", null, null, null, null, null),
                    new BookPage(book, 4,
                            "On the way down, we spoke about ordinary things. The map folded back into a pocket, the tea cooled, and the first lights appeared in the valley. The ridge remained behind us, not as an answer, but as a place where our questions had become easier to carry.",
                            "The Return", null, null, null, null, null)
            ));
        };
    }
}
