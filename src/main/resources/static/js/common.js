/**
 * ==========================================================================
 * READORA — GLOBAL JAVASCRIPT SYSTEM & DEMO STORE
 * Vanilla JavaScript | Ready for Spring Boot & PostgreSQL API integration
 * ==========================================================================
 */

// Local Storage Keys
const STORAGE_KEYS = {
    LIBRARY: 'readoraLibrary',
    BOOKMARKS: 'readoraBookmarks',
    NOTES: 'readoraNotes',
    PROGRESS: 'readoraProgress',
    HIGHLIGHTS: 'readoraHighlights',
    SETTINGS: 'readoraSettings',
    NOTIFICATIONS: 'readoraNotifications',
    PURCHASES: 'readoraPurchases',
    USER: 'readoraUser',
    ACTIVE_ROLE: 'readoraActiveRole'
};

// ==========================================================================
// DEMO DATABASE (Later replaced with Spring Boot GET /api/...)
// ==========================================================================
const READORA_DATA = {
    books: [
        {
            id: 1,
            title: "What You Leave Behind",
            author: "Demo Author",
            category: "Fiction",
            tag: "Fiction",
            language: "English",
            price: 0,
            premium: false,
            image: "img/what you leave behind.jpg",
            description: "A captivating exploration of memory, legacy, and what we leave for future generations. This novel follows characters as they navigate life's most profound moments and discover the lasting impact of their choices.",
            authorBio: "Demo Author has crafted stories that explore the human condition with sensitivity and depth. Their works have touched readers worldwide.",
            pages: 280,
            reads: 1420,
            rating: 4.8
        },
        {
            id: 2,
            title: "The Story of a New Name",
            author: "Elena Ferrante",
            category: "Fiction",
            tag: "Fiction",
            language: "Italian",
            price: 450,
            premium: true,
            image: "img/TheStoryofnewname.png",
            description: "Part of the acclaimed Neapolitan Novels series, this profound work traces the complex friendship between two women as they navigate growing up, ambition, and self-discovery in 1950s Naples.",
            authorBio: "Elena Ferrante is an internationally acclaimed author known for her deeply psychological exploration of female relationships and identity.",
            pages: 480,
            reads: 3200,
            rating: 4.9
        },
        {
            id: 3,
            title: "The Silent Spring",
            author: "Rachel Carson",
            category: "Science & Nature",
            tag: "Science & Nature",
            language: "English",
            price: 0,
            premium: false,
            image: "img/TheSilentSpring.png",
            description: "A groundbreaking environmental classic that awakened public consciousness about the dangers of pesticides. Carson's eloquent writing and rigorous research make this essential reading.",
            authorBio: "Rachel Carson was a pioneering biologist and writer whose fearless advocacy for environmental protection changed the course of modern conservation.",
            pages: 368,
            reads: 5120,
            rating: 4.9
        },
        {
            id: 4,
            title: "The Silent Patient",
            author: "Alex Michaelides",
            category: "Thriller",
            tag: "Thriller",
            language: "English",
            price: 350,
            premium: true,
            image: "img/The-Silent-Patient-.webp",
            description: "Alicia Berenson’s life is seemingly perfect. One evening she shoots her husband five times in the face, and then never speaks another word. A criminal psychotherapist is determined to unravel her mystery.",
            authorBio: "Alex Michaelides is a British-Cypriot author and screenwriter. His debut novel, The Silent Patient, became an international phenomenon.",
            pages: 336,
            reads: 4210,
            rating: 4.7
        },
        {
            id: 5,
            title: "The Hunger Games",
            author: "Suzanne Collins",
            category: "Fiction",
            tag: "Fiction",
            language: "English",
            price: 300,
            premium: true,
            image: "img/the-hunger-games.jpg",
            description: "In the ruins of a place once known as North America lies the nation of Panem. In a dystopian survival contest, Katniss Everdeen volunteers in place of her younger sister.",
            authorBio: "Suzanne Collins is an American author best known for the dystopian The Hunger Games trilogy.",
            pages: 374,
            reads: 8900,
            rating: 4.9
        },
        {
            id: 6,
            title: "The Secret Life of Bees",
            author: "Sue Monk Kidd",
            category: "Fiction",
            tag: "Fiction",
            language: "English",
            price: 0,
            premium: false,
            image: "img/The Secret Life of Bees.jpg",
            description: "Set in South Carolina during 1964, this moving story tells of Lily Owens, whose life has been shaped around the blurred memory of the afternoon her mother was killed.",
            authorBio: "Sue Monk Kidd is an acclaimed American writer of literary fiction and memoir.",
            pages: 336,
            reads: 2150,
            rating: 4.6
        },
        {
            id: 7,
            title: "Structures of Light",
            author: "Hiroshi Sugimoto",
            category: "Architecture",
            tag: "Architecture",
            language: "English",
            price: 600,
            premium: true,
            image: "img/StructuresOfLight.png",
            description: "A breathtaking monograph tracing Sugimoto's photographic exploration of architectural masters, light, space, and the intangible spirit of modern structures.",
            authorBio: "Hiroshi Sugimoto is a contemporary Japanese photographer and architect known for his ethereal black-and-white long-exposure studies.",
            pages: 220,
            reads: 1100,
            rating: 4.9
        },
        {
            id: 8,
            title: "Outliers",
            author: "Malcolm Gladwell",
            category: "Non-fiction",
            tag: "Non-fiction",
            language: "English",
            price: 0,
            premium: false,
            image: "img/Outerliers.jpg",
            description: "Malcolm Gladwell takes us on an intellectual journey through the world of 'outliers'—the best and the brightest, the most famous and the most successful.",
            authorBio: "Malcolm Gladwell is a staff writer at The New Yorker and bestselling author of five books.",
            pages: 320,
            reads: 6400,
            rating: 4.8
        },
        {
            id: 9,
            title: "The Night Tiger",
            author: "Yangsze Choo",
            category: "Fiction",
            tag: "Fiction",
            language: "English",
            price: 350,
            premium: true,
            image: "img/Night tiger.png",
            description: "A sweeping historical novel set in 1930s colonial Malaya about a quick-witted dressmaker’s apprentice and an orphaned houseboy drawn into a dangerous mystery.",
            authorBio: "Yangsze Choo is a New York Times bestselling Malaysian-American novelist.",
            pages: 384,
            reads: 1950,
            rating: 4.7
        },
        {
            id: 10,
            title: "सेतो धरती",
            author: "Amar Neupane",
            category: "Read Nepal",
            tag: "Read Nepal",
            language: "Nepali",
            price: 0,
            premium: false,
            image: "img/seto dharti.png",
            description: "मदन पुरस्कार प्राप्त अमर न्यौपानेको बहुचर्चित उपन्यास जसले बालविवाह र नेपाली समाजमा एकल महिलाले भोग्नुपरेको कारुणिक यथार्थ चित्रण गर्दछ।",
            authorBio: "अमर न्यौपाने नेपाली साहित्यका चर्चित उपन्यासकार हुन् जसले मदन पुरस्कार र पद्मश्री साहित्य पुरस्कार प्राप्त गरेका छन्।",
            pages: 312,
            reads: 9400,
            rating: 5.0
        },
        {
            id: 11,
            title: "निलो प्रेम",
            author: "Demo Author",
            category: "Read Nepal",
            tag: "Read Nepal",
            language: "Nepali",
            price: 250,
            premium: true,
            image: "img/nilo-prem.jpg",
            description: "काठमाडौंका गल्लीहरू र पोखराका ताल किनारहरूमा कोरिएको एक आधुनिक प्रेम कथा।",
            authorBio: "नेपाली समकालीन आख्यानका युवा लेखक।",
            pages: 240,
            reads: 1650,
            rating: 4.5
        },
        {
            id: 12,
            title: "Atomic Habits",
            author: "James Clear",
            category: "Non-fiction",
            tag: "Non-fiction",
            language: "English",
            price: 0,
            premium: false,
            image: "img/AtomicHabits.png",
            description: "An immensely practical guide on how tiny behavioral changes can lead to remarkable results. Learn how to build good habits and break bad ones.",
            authorBio: "James Clear is a writer and speaker focused on habits, decision making, and continuous improvement.",
            pages: 320,
            reads: 15400,
            rating: 4.9
        },
        {
            id: 13,
            title: "Muna Madan",
            author: "Laxmi Prasad Devkota",
            category: "Read Nepal",
            tag: "Read Nepal",
            language: "Nepali",
            price: 0,
            premium: false,
            image: "img/Munamadan.png",
            description: "नेपाली साहित्यका महाकवि लक्ष्मीप्रसाद देवकोटाद्वारा रचित झ्याउरे लयको अद्वितीय खण्डकाव्य।",
            authorBio: "लक्ष्मीप्रसाद देवकोटा (१९६६-२०१६) नेपाली साहित्यका महाकवि हुन्।",
            pages: 80,
            reads: 22000,
            rating: 5.0
        }
    ],

    newspapers: [
        {
            id: 101,
            title: "The Kathmandu Post",
            edition: "Morning Edition",
            date: "Today",
            language: "English",
            country: "Nepal",
            premium: false,
            price: 0,
            image: "img/Kathmandu.png",
            description: "Daily English newspaper covering national politics, business, economy, lifestyle, and opinions across Nepal.",
            about: "The Kathmandu Post is Nepal's leading English-language daily newspaper, founded in February 1993."
        },
        {
            id: 102,
            title: "कान्तिपुर",
            edition: "दैनिक संस्करण",
            date: "Today",
            language: "Nepali",
            country: "Nepal",
            premium: false,
            price: 0,
            image: "img/kantipur.png",
            description: "नेपालको लोकप्रिय राष्ट्रिय दैनिक पत्रिका जसमा राष्ट्रिय, राजनीति, समाज, विचार र आर्थिक समाचार समावेश छन्।",
            about: "कान्तिपुर पब्लिकेशन्सद्वारा प्रकाशित नेपालको सर्वाधिक पढिने दैनिक पत्रिका।"
        },
        {
            id: 103,
            title: "The Himalayan Times",
            edition: "Daily Issue",
            date: "Today",
            language: "English",
            country: "Nepal",
            premium: false,
            price: 0,
            image: "img/himalayan.png",
            description: "English-language news covering Nepal, world affairs, business, lifestyle, culture, and sports.",
            about: "The Himalayan Times is an English broadsheet newspaper published daily in Nepal."
        },
        {
            id: 104,
            title: "BBC News",
            edition: "Global Update",
            date: "Today",
            language: "English",
            country: "United Kingdom",
            premium: false,
            price: 0,
            image: "img/BBC.png",
            description: "International news covering world affairs, geopolitics, business, science, technology, and culture.",
            about: "BBC News provides trusted global reporting with impartial analysis."
        },
        {
            id: 105,
            title: "Reuters",
            edition: "Financial & World",
            date: "Today",
            language: "English",
            country: "International",
            premium: false,
            price: 0,
            image: "img/Reuters.png",
            description: "Global breaking news covering politics, financial markets, technology, and major international events.",
            about: "Reuters is one of the world's most reputable news agencies."
        },
        {
            id: 106,
            title: "The Guardian",
            edition: "International Edition",
            date: "Today",
            language: "English",
            country: "United Kingdom",
            premium: false,
            price: 0,
            image: "img/guardian.png",
            description: "In-depth investigative reporting on politics, climate change, global culture, and society.",
            about: "The Guardian is renowned for independent and investigative journalism."
        }
    ],

    magazines: [
        {
            id: 201,
            title: "TIME",
            issue: "September 2026 Issue",
            publisher: "TIME USA, LLC",
            category: "culture",
            language: "English",
            premium: true,
            price: 250,
            image: "img/timesMagazines.png",
            description: "Current ideas, influential leaders, and compelling stories shaping society, science, and the world.",
            highlights: ["AI In Global Medicine", "The Future of Clean Cities", "Voices of the Next Decade"]
        },
        {
            id: 202,
            title: "WIRED",
            issue: "August 2026 Issue",
            publisher: "Condé Nast",
            category: "technology",
            language: "English",
            premium: false,
            price: 0,
            image: "img/wired.png",
            description: "Technology, science, artificial intelligence, cybersecurity, and the breakthrough ideas changing modern life.",
            highlights: ["Quantum Computing Horizons", "Neural Interfaces", "Autonomous Skies"]
        },
        {
            id: 203,
            title: "National Geographic",
            issue: "August 2026 Issue",
            publisher: "National Geographic Partners",
            category: "lifestyle",
            language: "English",
            premium: false,
            price: 0,
            image: "img/National.png",
            description: "Discover nature, ancient civilizations, exploration, wildlife, and remarkable stories from around the globe.",
            highlights: ["Himalayan Glaciers Exploration", "Deep Ocean Wonders", "Ancient Silk Roads"]
        },
        {
            id: 204,
            title: "The New Yorker",
            issue: "Summer Cultural Review",
            publisher: "Condé Nast",
            category: "culture",
            language: "English",
            premium: true,
            price: 300,
            image: "img/newyorker.png",
            description: "Thoughtful journalism, commentary, criticism, essays, fiction, satire, and poetry.",
            highlights: ["Literary Masterclasses", "The Urban Architecture Shift", "Contemporary Poetry Folio"]
        },
        {
            id: 205,
            title: "Travel + Leisure",
            issue: "Monsoon & Autumn Special",
            publisher: "Dotdash Meredith",
            category: "travel",
            language: "English",
            premium: false,
            price: 0,
            image: "img/travel.png",
            description: "Inspiring travel destinations, boutique stays, culinary journeys, and hidden scenic trails.",
            highlights: ["Top Treks of Annapurna", "Mediterranean Coastal Walks", "Eco-Resorts of Asia"]
        },
        {
            id: 206,
            title: "साहित्यिक संसार",
            issue: "भाद्र २०८३ अङ्क",
            publisher: "नेपाल साहित्य प्रतिष्ठान",
            category: "culture",
            language: "Nepali",
            premium: false,
            price: 0,
            image: "img/nepal.png",
            description: "नेपाली साहित्य, कला, संस्कृति, विचार तथा आख्यान समेटिएको विशेष मासिक साहित्यिक पत्रिका।",
            highlights: ["नेपाली गद्यको विकास", "समकालीन हिमाली कविता", "लोक कथा अनुसन्धान"]
        }
    ],

    journals: [
        {
            id: 301,
            title: "Journal of Himalayan Studies",
            author: "Himalayan Research Institute",
            category: "Research",
            language: "English",
            premium: false,
            price: 0,
            date: "Vol 14, 2026",
            description: "Peer-reviewed multidisciplinary academic journal focusing on anthropology, ecology, and cultural history of the Himalayan belt."
        },
        {
            id: 302,
            title: "Nature Biotechnology & Ecology",
            author: "International Scientific Society",
            category: "Science",
            language: "English",
            premium: true,
            price: 500,
            date: "Issue 8, 2026",
            description: "High-impact research papers exploring ecological resilience, botanical biodiversity, and genetic developments."
        },
        {
            id: 303,
            title: "IEEE Computing & AI Systems",
            author: "IEEE Computer Society",
            category: "Technology",
            language: "English",
            premium: true,
            price: 450,
            date: "August 2026",
            description: "Academic papers covering machine learning systems, distributed computing, and ethical AI architecture."
        }
    ],

    readNepal: [
        {
            id: 401,
            title: "Muna Madan",
            nepaliTitle: "मुनामदन",
            author: "Laxmi Prasad Devkota",
            category: "Poetry",
            era: "1936",
            region: "Central Nepal",
            language: "Nepali",
            image: "img/Munamadan.png",
            about: "नेपाली साहित्यका महाकवि लक्ष्मीप्रसाद देवकोटाद्वारा रचित झ्याउरे लयको अमर खण्डकाव्य। यो कृतिको मुख्य सन्देश 'मानिस ठूलो दिलले हुन्छ जातले हुँदैन' भन्ने रहेको छ।",
            description: "A classical Nepali epic poem depicting the poignant separation of Madan and Muna, exploring themes of love, human values, and social compassion."
        },
        {
            id: 402,
            title: "Sirishko Phool",
            nepaliTitle: "सिरिषको फूल",
            author: "Parijat",
            category: "Novel",
            era: "1965",
            region: "Kathmandu Valley",
            language: "Nepali",
            image: "img/page.png",
            about: "पारिजातको अस्तित्ववादी नेपाली उपन्यास जसले मदन पुरस्कार प्राप्त गरेको थियो।",
            description: "A landmark existentialist masterpiece in Nepali modern literature capturing emotional disillusionment and philosophical reflection."
        },
        {
            id: 403,
            title: "Dibya Upadesh",
            nepaliTitle: "दिव्योपदेश",
            author: "Prithvi Narayan Shah",
            category: "History",
            era: "18th Century",
            region: "Gorkha & Kathmandu",
            language: "Nepali",
            image: "img/prithivinaraynshah.png",
            about: "नेपाल एकीकरणका नायक बडामहाराजाधिराज पृथ्वीनारायण शाहको दिव्योपदेश र राष्ट्रिय नीति।",
            description: "The historical geopolitical counsel and philosophical directives of King Prithvi Narayan Shah shaping the sovereignty of Nepal."
        },
        {
            id: 404,
            title: "Ghumne Mechmathi Andho Manche",
            nepaliTitle: "घुम्ने मेचमाथि अन्धो मान्छे",
            author: "Bhupi Sherchan",
            category: "Poetry",
            era: "1969",
            region: "Western Nepal",
            language: "Nepali",
            image: "img/nepalipage.png",
            about: "भूपी शेरचनको क्रान्तिकारी कविता संग्रह जसले नेपाली मुक्तछन्द कवितामा नयाँ युगको थालनी गर्‍यो।",
            description: "Bhupi Sherchan’s seminal collection of prose poetry that transformed modern Nepali literature with sharp social commentary and free verse."
        }
    ]
};

// ==========================================================================
// SEED INITIAL LOCAL STORAGE DATA (If not already present)
// ==========================================================================
function initLocalStorage() {
    // Current User Profile
    if (!localStorage.getItem(STORAGE_KEYS.USER)) {
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify({
            name: "Aayush Sharma",
            email: "aayush.readora@demo.np",
            role: "Reader & Scholar",
            avatar: "img/John.png",
            interests: ["Fiction", "Nepali Literature", "Science & Nature", "Philosophy"],
            fontSize: "medium",
            theme: "light",
            streak: 12,
            booksCompleted: 8,
            readingMinutes: 1420
        }));
    }

    // Default Saved Library
    if (!localStorage.getItem(STORAGE_KEYS.LIBRARY)) {
        localStorage.setItem(STORAGE_KEYS.LIBRARY, JSON.stringify([
            {
                id: 3,
                type: "book",
                title: "The Silent Spring",
                author: "Rachel Carson",
                image: "img/TheSilentSpring.png",
                category: "Science & Nature",
                progress: 68,
                lastRead: "2 hours ago",
                status: "reading"
            },
            {
                id: 2,
                type: "book",
                title: "The Story of a New Name",
                author: "Elena Ferrante",
                image: "img/TheStoryofnewname.png",
                category: "Fiction",
                progress: 42,
                lastRead: "Yesterday",
                status: "reading"
            },
            {
                id: 12,
                type: "book",
                title: "Atomic Habits",
                author: "James Clear",
                image: "img/AtomicHabits.png",
                category: "Non-fiction",
                progress: 0,
                lastRead: "3 days ago",
                status: "saved"
            },
            {
                id: 8,
                type: "book",
                title: "Outliers",
                author: "Malcolm Gladwell",
                image: "img/Outerliers.jpg",
                category: "Non-fiction",
                progress: 100,
                lastRead: "Last week",
                status: "completed"
            }
        ]));
    }

    // Default Bookmarks
    if (!localStorage.getItem(STORAGE_KEYS.BOOKMARKS)) {
        localStorage.setItem(STORAGE_KEYS.BOOKMARKS, JSON.stringify([
            {
                id: 1,
                bookTitle: "The Silent Spring",
                bookAuthor: "Rachel Carson",
                page: "Chapter 3: Elixirs of Death, Page 45",
                savedDate: "2026-08-28",
                image: "img/TheSilentSpring.png"
            },
            {
                id: 2,
                bookTitle: "Muna Madan",
                bookAuthor: "Laxmi Prasad Devkota",
                page: "Verse 120 (ल्हासाको यात्रा)",
                savedDate: "2026-08-25",
                image: "img/Munamadan.png"
            },
            {
                id: 3,
                bookTitle: "Structures of Light",
                bookAuthor: "Hiroshi Sugimoto",
                page: "Section 2: Villa Savoye Study",
                savedDate: "2026-08-20",
                image: "img/StructuresOfLight.png"
            }
        ]));
    }

    // Default Notes
    if (!localStorage.getItem(STORAGE_KEYS.NOTES)) {
        localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify([
            {
                id: 1,
                bookTitle: "The Silent Spring",
                bookAuthor: "Rachel Carson",
                page: "Chapter 2, Page 23",
                noteText: "Carson’s point regarding the interconnectedness of biological systems and chemical runoff is exceptionally clear and persuasive.",
                dateCreated: "2026-08-28"
            },
            {
                id: 2,
                bookTitle: "Muna Madan",
                bookAuthor: "Laxmi Prasad Devkota",
                page: "Verse 85",
                noteText: "'मानिस ठूलो दिलले हुन्छ जातले हुँदैन' — Devkota's humanist philosophy transcends historical periods.",
                dateCreated: "2026-08-25"
            }
        ]));
    }

    // Default Highlights
    if (!localStorage.getItem(STORAGE_KEYS.HIGHLIGHTS)) {
        localStorage.setItem(STORAGE_KEYS.HIGHLIGHTS, JSON.stringify([
            {
                id: 1,
                bookTitle: "The Silent Spring",
                text: "In nature, nothing exists alone.",
                color: "#ffe58f",
                date: "2026-08-28"
            },
            {
                id: 2,
                bookTitle: "Muna Madan",
                text: "मानिस ठूलो दिलले हुन्छ जातले हुँदैन",
                color: "#b7eb8f",
                date: "2026-08-25"
            }
        ]));
    }

    // Default Purchases
    if (!localStorage.getItem(STORAGE_KEYS.PURCHASES)) {
        localStorage.setItem(STORAGE_KEYS.PURCHASES, JSON.stringify([
            {
                id: "ORD-98412",
                title: "Structures of Light",
                type: "Book",
                author: "Hiroshi Sugimoto",
                image: "img/StructuresOfLight.png",
                amount: 600,
                paymentMethod: "eSewa",
                transactionId: "ESEWA-884920193",
                date: "2026-08-15",
                status: "Successful"
            }
        ]));
    }

    // Default Notifications
    if (!localStorage.getItem(STORAGE_KEYS.NOTIFICATIONS)) {
        localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify([
            {
                id: 1,
                type: "publication",
                title: "New Digital Archive Added",
                message: "'Chronicles of the Valley' manuscripts are now digitized in Read Nepal.",
                time: "10 mins ago",
                read: false,
                icon: "fa-book-bookmark"
            },
            {
                id: 2,
                type: "reminder",
                title: "Reading Streak Maintained",
                message: "You've read 12 days in a row! 18 minutes left to hit today's goal.",
                time: "2 hours ago",
                read: false,
                icon: "fa-fire"
            },
            {
                id: 3,
                type: "recommendation",
                title: "Recommended for You",
                message: "Based on your interest in Science & Nature, you might enjoy 'Outliers'.",
                time: "1 day ago",
                read: true,
                icon: "fa-compass"
            }
        ]));
    }
}

// ==========================================================================
// TOAST NOTIFICATION HELPER
// ==========================================================================
function showToast(message, type = 'success', icon = null) {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    let defaultIcon = 'fa-circle-check';
    if (type === 'info') defaultIcon = 'fa-circle-info';
    if (type === 'warning') defaultIcon = 'fa-triangle-exclamation';
    if (type === 'error') defaultIcon = 'fa-circle-xmark';
    if (icon) defaultIcon = icon;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="fa-solid ${defaultIcon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => toast.classList.add('show'), 20);

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 350);
    }, 3200);
}

// ==========================================================================
// CONFIRMATION MODAL HELPER
// ==========================================================================
function showConfirmModal(options) {
    const {
        title = "Confirm Action",
        message = "Are you sure you want to proceed?",
        confirmText = "Confirm",
        cancelText = "Cancel",
        isDestructive = false,
        onConfirm = () => {}
    } = options;

    let overlay = document.getElementById('globalConfirmModal');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'globalConfirmModal';
        overlay.className = 'modal-overlay';
        document.body.appendChild(overlay);
    }

    overlay.innerHTML = `
        <div class="modal-card">
            <div class="modal-header">
                <h3 class="modal-title">${title}</h3>
                <button class="modal-close-btn" id="modalCloseX"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="modal-body">
                <p style="color: var(--color-text-secondary); font-size: 13.5px;">${message}</p>
            </div>
            <div class="modal-footer">
                <button class="btn btn-outline" id="modalCancelBtn">${cancelText}</button>
                <button class="btn ${isDestructive ? 'btn-dark' : 'btn-green'}" id="modalConfirmBtn">${confirmText}</button>
            </div>
        </div>
    `;

    overlay.classList.add('show');

    function closeModal() {
        overlay.classList.remove('show');
    }

    document.getElementById('modalCloseX').onclick = closeModal;
    document.getElementById('modalCancelBtn').onclick = closeModal;
    document.getElementById('modalConfirmBtn').onclick = () => {
        closeModal();
        onConfirm();
    };

    overlay.onclick = (e) => {
        if (e.target === overlay) closeModal();
    };
}

// ==========================================================================
// HEADER NAVIGATION & PROFILE DROPDOWN INITIALIZER
// ==========================================================================
function initHeaderUI() {
    const profileBtn = document.getElementById('headerProfileBtn');
    const profileDropdown = document.getElementById('headerProfileDropdown');
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileNavDrawer = document.getElementById('mobileNavDrawer');

    if (profileBtn && profileDropdown) {
        profileBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            profileDropdown.classList.toggle('show');
        });

        document.addEventListener('click', (e) => {
            if (!profileDropdown.contains(e.target) && e.target !== profileBtn) {
                profileDropdown.classList.remove('show');
            }
        });
    }

    if (mobileMenuBtn && mobileNavDrawer) {
        mobileMenuBtn.addEventListener('click', () => {
            mobileNavDrawer.classList.toggle('open');
            const icon = mobileMenuBtn.querySelector('i');
            if (icon) {
                if (mobileNavDrawer.classList.contains('open')) {
                    icon.className = 'fa-solid fa-xmark';
                } else {
                    icon.className = 'fa-solid fa-bars';
                }
            }
        });
    }

    // Update notification count badge if exists
    const notifBadge = document.getElementById('headerNotifBadge');
    if (notifBadge) {
        const notifs = JSON.parse(localStorage.getItem(STORAGE_KEYS.NOTIFICATIONS) || '[]');
        const unread = notifs.filter(n => !n.read).length;
        if (unread > 0) {
            notifBadge.textContent = unread;
            notifBadge.style.display = 'flex';
        } else {
            notifBadge.style.display = 'none';
        }
    }
}

// Run on page load
document.addEventListener('DOMContentLoaded', () => {
    initLocalStorage();
    initHeaderUI();
});
