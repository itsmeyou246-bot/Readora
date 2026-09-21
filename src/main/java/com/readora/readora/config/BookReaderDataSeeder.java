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
    CommandLineRunner seedBookReader(
            BookRepository books,
            BookPageRepository pages
    ) {
        return args -> {
            /*
             * 1. INSERT / UPDATE STANDARD BOOKS
             */
            seedStandardBooks(books);

            /*
             * 2. CREATE SAMPLE READER BOOK
             */
            Book quietRidge = books.findById(SAMPLE_BOOK_ID)
                    .orElseGet(() -> {
                        Book book = new Book(
                                SAMPLE_BOOK_ID,
                                "The Quiet Ridge",
                                "Readora Editorial Studio",
                                "A quiet journey into the high hills and deep valleys of contemplation.",
                                "Fiction",
                                "English",
                                0.0,
                                "img/library.png",
                                "the-quiet-ridge.pdf",
                                false,
                                4
                        );
                        book.setType("book");
                        book.setStatus("published");
                        return books.save(book);
                    });

            quietRidge.setType("book");
            quietRidge.setStatus("published");
            books.save(quietRidge);

            /*
             * 3. SEED BOOK PAGES FOR ALL BOOKS
             */
            seedQuietRidgePages(quietRidge, pages);
            seedAllCatalogBookPages(books, pages);
        };
    }

    private void seedQuietRidgePages(Book quietRidge, BookPageRepository pages) {
        if (pages.countByBookId(quietRidge.getId()) > 0) {
            return;
        }

        pages.saveAll(List.of(
                new BookPage(
                        quietRidge,
                        1,
                        "At first light, the ridge was only a darker line against the pale sky. We followed it slowly, carrying warm tea, a paper map, and the kind of patience that belongs to long walks. Below us, the valley gathered its roofs and roads out of the mist.",
                        "The Ridge",
                        null, null, null, null, null
                ),
                new BookPage(
                        quietRidge,
                        2,
                        "The path narrowed where the grass gave way to stone. Each step made the world quieter. We began to notice small things: a feather caught in a thorn, water shining in a crack, the soft movement of clouds beyond the far hills.",
                        "The Ridge",
                        "/img/library.png",
                        "A room for returning",
                        "Notes from the quiet places",
                        "A library at the end of an afternoon, waiting for the next reader.",
                        "Readora Editorial Studio"
                ),
                new BookPage(
                        quietRidge,
                        3,
                        "By noon, the wind had cleared the last veil from the peaks. Nothing had changed below, yet everything looked newly arranged. We rested beside a low wall and understood that a journey does not need to be dramatic to become important.",
                        "The View",
                        null, null, null, null, null
                ),
                new BookPage(
                        quietRidge,
                        4,
                        "On the way down, we spoke about ordinary things. The map folded back into a pocket, the tea cooled, and the first lights appeared in the valley. The ridge remained behind us, not as an answer, but as a place where our questions had become easier to carry.",
                        "The Return",
                        null, null, null, null, null
                )
        ));
    }

    private void seedAllCatalogBookPages(BookRepository books, BookPageRepository pages) {
        // Book 1: What You Leave Behind (Free)
        books.findById(1L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "At the turn of the century, memory had a weight you could measure in letters, train tickets, and the pressed wildflowers kept inside leather-bound journals. When Arthur returned to the old estate by the lake, the stillness was broken only by the clock in the library ticking steadily as it had for fifty years.",
                                "Chapter 1: The Lake House", null, null, null, null, null),
                        new BookPage(b, 2,
                                "In the quiet of the morning, Arthur found the notebook wrapped in oilcloth beneath the cedar chest. The handwriting belonged to his grandfather, penned during the long winter of 1942: 'A life is not measured by the stones we gather, but by the paths we make easier for those who follow.'",
                                "Chapter 2: The Cedar Chest", "/img/what you leave behind.jpg",
                                "Memories of the Estate", "Echoes Across Time", "The library as preserved over generations.", "Readora Archives"),
                        new BookPage(b, 3,
                                "The autumn afternoon brought a golden haze across the orchard. As neighbors gathered to share harvest stories, Arthur understood that what we leave behind is woven into conversations, remembered laughter, and the quiet acts of courage that outlive the speaker.",
                                "Chapter 3: The Golden Hour", null, null, null, null, null),
                        new BookPage(b, 4,
                                "Night fell gently over the valley. From the porch, the distant lanterns of the village sparkled like constellations reflected on earth. Standing in the cool night breeze, he smiled knowing that every ending carries the quiet promise of a beginning.",
                                "Chapter 4: Tomorrow's Light", null, null, null, null, null)
                ));
            }
        });

        // Book 2: The Story of a New Name (Elena Ferrante, Premium)
        books.findById(2L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "The sea at Ischia smelled of salt and wild mint in the summer mornings. We walked along the shore before the sun grew hot, speaking of books we had read and futures we had invented for ourselves in the narrow streets of Naples.",
                                "Chapter 1: Island Summer", null, null, null, null, null),
                        new BookPage(b, 2,
                                "In the afternoons, Elena wrote in her small quaderno, filling pages with descriptions of the light on the cliffs and the peculiar melancholy that accompanied every joyful hour.",
                                "Chapter 2: The Notebook", null, null, null, null, null),
                        new BookPage(b, 3,
                                "Friendship was our anchor and our trial. Between ambition and devotion, every word spoken across the kitchen table carried the weight of our shared history.",
                                "Chapter 3: Words Between Us", null, null, null, null, null)
                ));
            }
        });

        // Book 3: The Silent Spring (Rachel Carson, Free)
        books.findById(3L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "Across the natural world, a quiet transformation was taking place. Birds, insects, forests, streams, and fields were connected through relationships that had developed over generations. When one part of that system was disturbed, the effects could travel far beyond the original point of change.",
                                "A Fable for Tomorrow", null, null, null, null, null),
                        new BookPage(b, 2,
                                "The living landscape depends upon an intricate balance. Soil, water, plants, insects, birds, and animals form communities in which each element has a role. Changes introduced into one part of this system can alter the conditions experienced by everything around it.",
                                "The Balance of Nature", null, null, null, null, null),
                        new BookPage(b, 3,
                                "Water carries the history of everything that enters it. A substance released upon the land may move through streams and soil before reaching another part of the environment. What appears to disappear from sight may therefore continue its journey through the living world.",
                                "Rivers of Life", null, null, null, null, null),
                        new BookPage(b, 4,
                                "The effects of environmental change are not always immediate. A landscape may appear unchanged while gradual alterations take place beneath the surface. Over time, however, these changes can become visible in the plants that grow, the insects that remain, and the animals that depend upon them.",
                                "The Web of Life", null, null, null, null, null),
                        new BookPage(b, 5,
                                "The story of the natural world is therefore also a story of connection. Forests, fields, rivers, birds, insects, and people share an environment in which actions in one place can influence life somewhere else. Understanding these relationships is essential to protecting the world around us.",
                                "A Living Environment", null, null, null, null, null),
                        new BookPage(b, 6,
                                "The future of the landscape depends not only upon the power to change it, but also upon the wisdom to understand those changes. Careful observation, responsible choices, and respect for natural systems can help preserve the richness of the world for those who come after us.",
                                "The Choice Before Us", null, null, null, null, null)
                ));
            }
        });

        // Book 4: The Silent Patient (Alex Michaelides, Premium)
        books.findById(4L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "Alicia Berenson was thirty-three years old when she killed her husband. Six years later, she has not spoken a single word. Her silence is not an absence of sound; it is a fortress that holds the truth of that tragic night.",
                                "Prologue: The Silence", null, null, null, null, null),
                        new BookPage(b, 2,
                                "As a psychotherapist at The Grove, Theo Faber had waited years for the opportunity to treat Alicia. He believed that even the deepest silence could be translated if one listened with infinite patience.",
                                "Chapter 1: The Grove", null, null, null, null, null),
                        new BookPage(b, 3,
                                "Her final painting, created in the days following the murder, was titled Alcestis. In Greek mythology, Alcestis dies for her husband and returns from the underworld, unable to speak. The canvas held clues no one had yet deciphered.",
                                "Chapter 2: Alcestis", null, null, null, null, null)
                ));
            }
        });

        // Book 5: The Hunger Games (Suzanne Collins, Premium)
        books.findById(5L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "When I wake up, the other side of the bed is cold. My little sister, Prim, curled up on her side, asleep. Beyond our fence lies the wilderness of District 12, shrouded in early morning fog.",
                                "Chapter 1: The Reaping Day", null, null, null, null, null),
                        new BookPage(b, 2,
                                "In the woods, Gale is waiting with a freshly snared loaf of warm bread. For a few brief hours before the reaping, we are free under the canopy of pine and birch.",
                                "Chapter 2: Beyond the Fence", null, null, null, null, null)
                ));
            }
        });

        // Book 6: The Secret Life of Bees (Sue Monk Kidd, Free)
        books.findById(6L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "The bees came the summer of 1964, the summer I turned fourteen. They lived inside the cracks of my bedroom wall, making a sound like a tiny, vibrating engine in the dark.",
                                "Chapter 1: The Bees", null, null, null, null, null),
                        new BookPage(b, 2,
                                "Tiburon, South Carolina, welcomed us with rows of pink houses and jars of Black Madonna Honey. In the honey house, August Boatwright taught me that love is the secret current that keeps the world alive.",
                                "Chapter 2: The Pink House", null, null, null, null, null)
                ));
            }
        });

        // Book 7: Structures of Light (Hiroshi Sugimoto, Premium)
        books.findById(7L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "Architecture is not merely the construction of shelter; it is the sculpting of space and time with light. When sunlight enters a vaulted stone archway, shadow becomes a tangible medium.",
                                "Section 1: Space and Shadow", null, null, null, null, null),
                        new BookPage(b, 2,
                                "Through long exposures across empty theaters and minimalist pavilions, the camera captures not just the object, but the duration of light moving through silence.",
                                "Section 2: Time as Material", null, null, null, null, null)
                ));
            }
        });

        // Book 8: Outliers (Malcolm Gladwell, Free)
        books.findById(8L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "We are often told that success is solely the product of individual talent, grit, and extraordinary intellect. But if we look closely at the lives of remarkable achievers, we discover a hidden history of opportunity and cultural legacy.",
                                "Chapter 1: The Roseto Mystery", null, null, null, null, null),
                        new BookPage(b, 2,
                                "The 10,000-hour rule is not about solitary genius; it is about the confluence of timing, mentorship, and extraordinary dedication that allows mastery to flourish.",
                                "Chapter 2: The 10,000-Hour Rule", null, null, null, null, null)
                ));
            }
        });

        // Book 9: The Night Tiger (Yangsze Choo, Premium)
        books.findById(9L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "In the lush rubber plantations of 1930s Malaya, old legends lingered in the monsoon mist. They spoke of weretigers that roamed the jungle paths at twilight, watching travelers from the shadows.",
                                "Chapter 1: Five Virtues", null, null, null, null, null),
                        new BookPage(b, 2,
                                "Ji Lin slipped the lucky glass bead into her pocket. Little did she know that a small token discovered in a dance hall would lead her across the dark rivers of the interior.",
                                "Chapter 2: The Secret Token", null, null, null, null, null)
                ));
            }
        });

        // Book 10: सेतो धरती (Amar Neupane, Free)
        books.findById(10L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "पहाडको उकालोमा बाल्यकालको हाँसो र पुतलीको पखेटा जस्तै रमाइलो थियो जीवन। तर समयको आँधीले जब जीवनको बाटो बदल्यो, सेतो पहिरनभित्रका भावनाहरू नदीझैँ शान्त तर गहिरा बन्दै गए।",
                                "अध्याय १: बाल्यकालका रङ्गहरू", null, null, null, null, null),
                        new BookPage(b, 2,
                                "गाउँको चौतारीमा पीपलको पात हल्लँदा सुनिने सुसेलीले सम्झनाहरू ब्युँझाउँथ्यो। संघर्ष र धैर्यताको कथा नै यो भूमिको मौलिक पहिचान हो।",
                                "अध्याय २: चौतारीको सम्झना", null, null, null, null, null)
                ));
            }
        });

        // Book 11: निलो प्रेम (Demo Author, Premium)
        books.findById(11L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "फेवातालको शान्त पानीमा माछापुच्छ्रेको छायाँ तैरिरहेको थियो। साँझको चिसो हावासँगै सुरु भएको कुराकानीले दुई फरक संसारलाई एउटै लयमा बाँधेको थियो।",
                                "अध्याय १: फेवातालको किनार", null, null, null, null, null),
                        new BookPage(b, 2,
                                "काठमाडौंका पुराना गल्लीहरू र वर्षाको झरीमा कोरिएका शब्दहरूले जीवनको अर्थ खोजिरहेका थिए। माया केवल शब्द होइन, सँगै हिँड्ने मौन यात्रा हो।",
                                "अध्याय २: असनका गल्लीहरू", null, null, null, null, null)
                ));
            }
        });

        // Book 12: Atomic Habits (James Clear, Free)
        books.findById(12L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "Changes that seem small and unimportant at first will compound into remarkable results if you are willing to stick with them for years. We all deal with setbacks, but in the long run, the quality of our lives often depends on the quality of our habits.",
                                "Chapter 1: The Surprising Power of Atomic Habits", null, null, null, null, null),
                        new BookPage(b, 2,
                                "Habits are the compound interest of self-improvement. Getting 1 percent better every day counts for a lot in the long run. If you can get 1 percent better each day for one year, you will end up thirty-seven times better by the time you are done.",
                                "Chapter 2: How Your Habits Shape Your Identity", null, null, null, null, null),
                        new BookPage(b, 3,
                                "The Four Laws of Behavior Change provide a simple set of rules for creating good habits: 1. Make it obvious. 2. Make it attractive. 3. Make it easy. 4. Make it satisfying.",
                                "Chapter 3: The Four Laws", null, null, null, null, null)
                ));
            }
        });

        // Book 13: Muna Madan (Laxmi Prasad Devkota, Free)
        books.findById(13L).ifPresent(b -> {
            if (pages.countByBookId(b.getId()) == 0) {
                pages.saveAll(List.of(
                        new BookPage(b, 1,
                                "मानिस ठूलो दिलले हुन्छ जातले हुँदैन। परदेशको यात्रामा निस्किएका मदन र घरमा बाटो कुरिरहेकी मुनाको अमर प्रेम र करुणाको अमर काव्य।",
                                "पहिलो सर्ग: विदाईको क्षण", null, null, null, null, null),
                        new BookPage(b, 2,
                                "हिमालका काखहरूमा बग्ने नदीहरू जस्तै निर्मल भावनाहरूले मानवीय संवेदनालाई स्पर्श गर्दछन्। सेवा र त्याग नै जीवनको सर्वोच्च सत्य हो।",
                                "दोस्रो सर्ग: परदेशको यात्रा", null, null, null, null, null)
                ));
            }
        });
    }

    private void seedStandardBooks(BookRepository books) {
        List<Book> initialBooks = List.of(
                createBook(1L, "What You Leave Behind", "Demo Author",
                        "A captivating exploration of memory, legacy, and what we leave for future generations.",
                        "Fiction", "English", 0.0, "img/what you leave behind.jpg", "books/sample.pdf", false, 4),
                createBook(2L, "The Story of a New Name", "Elena Ferrante",
                        "Part of the acclaimed Neapolitan Novels series, this profound work traces the complex friendship between two women.",
                        "Fiction", "Italian", 450.0, "img/TheStoryofnewname.png", "books/sample.pdf", true, 3),
                createBook(3L, "The Silent Spring", "Rachel Carson",
                        "A groundbreaking environmental classic that awakened public consciousness about the dangers of pesticides.",
                        "Science & Nature", "English", 0.0, "img/TheSilentSpring.png", "books/sample.pdf", false, 6),
                createBook(4L, "The Silent Patient", "Alex Michaelides",
                        "Alicia Berenson's life is seemingly perfect until she shoots her husband and never speaks another word.",
                        "Thriller", "English", 350.0, "img/The-Silent-Patient-.webp", "books/sample.pdf", true, 3),
                createBook(5L, "The Hunger Games", "Suzanne Collins",
                        "In the ruins of a place once known as North America lies the nation of Panem in a dystopian survival contest.",
                        "Fiction", "English", 300.0, "img/the-hunger-games.jpg", "books/sample.pdf", true, 2),
                createBook(6L, "The Secret Life of Bees", "Sue Monk Kidd",
                        "Set in South Carolina during 1964, this moving story tells of Lily Owens, shaped by memories of her mother.",
                        "Fiction", "English", 0.0, "img/The Secret Life of Bees.jpg", "books/sample.pdf", false, 2),
                createBook(7L, "Structures of Light", "Hiroshi Sugimoto",
                        "A breathtaking monograph tracing Sugimoto's photographic exploration of architectural masters, light, and space.",
                        "Architecture", "English", 600.0, "img/StructuresOfLight.png", "books/sample.pdf", true, 2),
                createBook(8L, "Outliers", "Malcolm Gladwell",
                        "Malcolm Gladwell takes us on an intellectual journey through the world of outliers—the best and brightest.",
                        "Non-fiction", "English", 0.0, "img/Outerliers.jpg", "books/sample.pdf", false, 2),
                createBook(9L, "The Night Tiger", "Yangsze Choo",
                        "A sweeping historical novel set in 1930s colonial Malaya about a dressmaker's apprentice and a mystery.",
                        "Fiction", "English", 350.0, "img/Night tiger.png", "books/sample.pdf", true, 2),
                createBook(10L, "सेतो धरती", "Amar Neupane",
                        "मदन पुरस्कार प्राप्त अमर न्यौपानेको बहुचर्चित उपन्यास जसले बालविवाह र एकल महिलाको कारुणिक यथार्थ चित्रण गर्दछ।",
                        "Read Nepal", "Nepali", 0.0, "img/seto dharti.png", "books/sample.pdf", false, 2),
                createBook(11L, "निलो प्रेम", "Demo Author",
                        "काठमाडौंका गल्लीहरू र पोखराका ताल किनारहरूमा कोरिएको एक आधुनिक प्रेम कथा।",
                        "Read Nepal", "Nepali", 250.0, "img/nilo-prem.jpg", "books/sample.pdf", true, 2),
                createBook(12L, "Atomic Habits", "James Clear",
                        "An immensely practical guide on how tiny behavioral changes can lead to remarkable results.",
                        "Non-fiction", "English", 0.0, "img/AtomicHabits.png", "books/sample.pdf", false, 3),
                createBook(13L, "Muna Madan", "Laxmi Prasad Devkota",
                        "नेपाली साहित्यका महाकवि लक्ष्मीप्रसाद देवकोटाद्वारा रचित झ्याउरे लयको अद्वितीय खण्डकाव्य।",
                        "Read Nepal", "Nepali", 0.0, "img/Munamadan.png", "books/sample.pdf", false, 2)
        );

        for (Book seed : initialBooks) {
            Book book = books.findById(seed.getId()).orElse(new Book());
            book.setId(seed.getId());
            book.setTitle(seed.getTitle());
            book.setAuthor(seed.getAuthor());
            book.setDescription(seed.getDescription());
            book.setCategory(seed.getCategory());
            book.setLanguage(seed.getLanguage());
            book.setPrice(seed.getPrice());
            book.setImage(seed.getImage());
            book.setFilePath(seed.getFilePath());
            book.setPremium(seed.isPremium());
            book.setPageCount(seed.getPageCount());
            book.setType("book");
            book.setStatus("published");

            if (book.getMood() == null || book.getMood().isBlank()) {
                book.setMood("Calm");
            }
            books.save(book);
        }
    }

    private Book createBook(
            Long id,
            String title,
            String author,
            String description,
            String category,
            String language,
            Double price,
            String image,
            String filePath,
            boolean premium,
            Integer pageCount
    ) {
        Book book = new Book(
                id, title, author, description,
                category, language, price, image,
                filePath, premium, pageCount
        );
        book.setType("book");
        book.setStatus("published");
        return book;
    }
}