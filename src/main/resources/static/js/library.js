/*
 * ============================================================
 * READORA — MY LIBRARY
 * ============================================================
 *
 * Real backend source:
 *
 * GET /api/library?categorized=true
 *
 * Response:
 *
 * {
 *     reading: [],
 *     saved: [],
 *     completed: [],
 *     purchased: []
 * }
 *
 * Active reader:
 *
 * /book-reader?bookId=BOOK_ID
 *
 * No demo books.
 * No fake localStorage library.
 * No legacy reader.html.
 * ============================================================
 */

(function () {

    "use strict";

    /* =========================================================
       STATE
       ========================================================= */

    let libraryData = {
        reading: [],
        saved: [],
        completed: [],
        purchased: []
    };

    /* =========================================================
       AUTHENTICATION
       ========================================================= */

    function getStoredUser() {

        try {

            const raw =
                localStorage.getItem(
                    "readoraUser"
                );

            if (!raw) {
                return null;
            }

            return JSON.parse(raw);

        } catch (error) {

            console.error(
                "READORA: Invalid stored user.",
                error
            );

            return null;
        }
    }

    function getToken() {

        const user =
            getStoredUser();

        if (!user) {
            return null;
        }

        return (
            user.token ||
            user.accessToken ||
            user.jwt ||
            null
        );
    }

    function getAuthHeaders() {

        const token =
            getToken();

        const headers = {
            "Accept":
                "application/json"
        };

        if (token) {

            headers[
                "Authorization"
                ] =
                "Bearer " + token;
        }

        return headers;
    }

    /* =========================================================
       API
       ========================================================= */

    async function fetchLibrary() {

        const response =
            await fetch(
                "/api/library?categorized=true",
                {
                    method: "GET",
                    headers:
                        getAuthHeaders(),
                    credentials:
                        "include",
                    cache:
                        "no-cache"
                }
            );

        if (
            response.status === 401 ||
            response.status === 403
        ) {

            window.location.href =
                "/login";

            throw new Error(
                "Authentication required."
            );
        }

        if (!response.ok) {

            let message =
                "Unable to load your library.";

            try {

                const data =
                    await response.json();

                if (data && data.message) {
                    message =
                        data.message;
                }

            } catch (ignored) {
            }

            throw new Error(
                message
            );
        }

        return response.json();
    }

    /* =========================================================
       NORMALIZATION
       ========================================================= */

    function normalizeLibrary(data) {

        data =
            data || {};

        return {

            reading:
                Array.isArray(
                    data.reading
                )
                    ? data.reading
                    : [],

            saved:
                Array.isArray(
                    data.saved
                )
                    ? data.saved
                    : [],

            completed:
                Array.isArray(
                    data.completed
                )
                    ? data.completed
                    : [],

            purchased:
                Array.isArray(
                    data.purchased
                )
                    ? data.purchased
                    : []
        };
    }

    function bookId(book) {

        if (!book) {
            return null;
        }

        const value =
            book.id ??
            book.bookId ??
            book.book_id;

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return null;
        }

        return String(value);
    }

    function bookTitle(book) {

        return String(
            book?.title ||
            "Untitled Book"
        );
    }

    function bookAuthor(book) {

        return String(
            book?.author ||
            "Unknown Author"
        );
    }

    function bookCategory(book) {

        return String(
            book?.category ||
            "General"
        );
    }

    function bookImage(book) {

        let image =
            book?.image ||
            "/img/Book.png";

        image =
            String(image)
                .replaceAll("\\", "/");

        if (
            image.startsWith(
                "/static/"
            )
        ) {

            image =
                image.substring(
                    "/static".length
                );
        }

        if (
            !image.startsWith("/")
        ) {

            image =
                "/" + image;
        }

        return image;
    }

    function progress(book) {

        let value =
            Number(
                book?.progress ?? 0
            );

        if (
            !Number.isFinite(value)
        ) {
            value = 0;
        }

        return Math.max(
            0,
            Math.min(
                100,
                Math.round(value)
            )
        );
    }

    function currentPage(book) {

        const page =
            Number(
                book?.currentPage ?? 0
            );

        if (
            !Number.isFinite(page)
        ) {
            return 0;
        }

        return Math.max(
            0,
            Math.round(page)
        );
    }

    function totalPages(book) {

        const pages =
            Number(
                book?.pageCount ??
                book?.totalPages ??
                0
            );

        if (
            !Number.isFinite(pages)
        ) {
            return 0;
        }

        return Math.max(
            0,
            Math.round(pages)
        );
    }

    /* =========================================================
       HTML SAFETY
       ========================================================= */

    function escapeHtml(value) {

        return String(
            value ?? ""
        )
            .replaceAll(
                "&",
                "&amp;"
            )
            .replaceAll(
                "<",
                "&lt;"
            )
            .replaceAll(
                ">",
                "&gt;"
            )
            .replaceAll(
                '"',
                "&quot;"
            )
            .replaceAll(
                "'",
                "&#039;"
            );
    }

    /* =========================================================
       DATE
       ========================================================= */

    function formatDate(value) {

        if (!value) {
            return "";
        }

        const date =
            new Date(value);

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return "";
        }

        return date.toLocaleDateString(
            "en-US",
            {
                month: "short",
                day: "numeric",
                year: "numeric"
            }
        );
    }

    function relativeDate(value) {

        if (!value) {
            return "";
        }

        const date =
            new Date(value);

        if (
            Number.isNaN(
                date.getTime()
            )
        ) {
            return "";
        }

        const diff =
            Date.now() -
            date.getTime();

        const minutes =
            Math.floor(
                diff / 60000
            );

        if (minutes < 1) {
            return "Just now";
        }

        if (minutes < 60) {
            return (
                minutes +
                (
                    minutes === 1
                        ? " minute ago"
                        : " minutes ago"
                )
            );
        }

        const hours =
            Math.floor(
                minutes / 60
            );

        if (hours < 24) {
            return (
                hours +
                (
                    hours === 1
                        ? " hour ago"
                        : " hours ago"
                )
            );
        }

        const days =
            Math.floor(
                hours / 24
            );

        if (days < 7) {
            return (
                days +
                (
                    days === 1
                        ? " day ago"
                        : " days ago"
                )
            );
        }

        return formatDate(
            value
        );
    }

    /* =========================================================
       READER URL
       ========================================================= */

    function readerUrl(book) {

        const id =
            bookId(book);

        if (!id) {
            return "#";
        }

        return (
            "/book-reader?bookId=" +
            encodeURIComponent(id)
        );
    }

    /* =========================================================
       BOOK DETAILS URL
       ========================================================= */

    function detailsUrl(book) {

        const id =
            bookId(book);

        if (!id) {
            return "#";
        }

        return (
            "/book-details?id=" +
            encodeURIComponent(id)
        );
    }

    /* =========================================================
       EMPTY STATE
       ========================================================= */

    function emptyMessage(
        category
    ) {

        const messages = {

            reading:
                "You are not reading any books yet. Start reading a book from Books to see it here.",

            saved:
                "You have no saved books yet. Use the bookmark/favorite option on a book to save it.",

            completed:
                "Completed books will appear here after you finish reading them.",

            purchased:
                "Your purchased premium books will appear here after a successful eSewa payment."
        };

        return (
            messages[category] ||
            "Nothing to show here yet."
        );
    }

    /* =========================================================
       BOOK CARD
       ========================================================= */

    function createBookCard(
        book,
        category
    ) {

        const id =
            bookId(book);

        const title =
            escapeHtml(
                bookTitle(book)
            );

        const author =
            escapeHtml(
                bookAuthor(book)
            );

        const categoryName =
            escapeHtml(
                bookCategory(book)
            );

        const image =
            escapeHtml(
                bookImage(book)
            );

        const pct =
            progress(book);

        const page =
            currentPage(book);

        const pages =
            totalPages(book);

        const lastRead =
            book.lastReadAt
                ? relativeDate(
                    book.lastReadAt
                )
                : "";

        let actionText =
            "Read Book";

        if (
            category === "reading"
        ) {

            actionText =
                "Continue Reading";

        } else if (
            category === "completed"
        ) {

            actionText =
                "Read Again";

        } else if (
            category === "purchased"
        ) {

            actionText =
                pct > 0
                    ? "Continue Reading"
                    : "Read Book";

        } else if (
            category === "saved"
        ) {

            actionText =
                pct > 0
                    ? "Continue Reading"
                    : "Open Book";
        }

        const actionUrl =
            category === "saved" &&
            pct === 0
                ? detailsUrl(book)
                : readerUrl(book);

        let progressHtml = "";

        if (
            category === "reading" ||
            category === "completed" ||
            category === "purchased" ||
            pct > 0
        ) {

            progressHtml =
                `
                <div class="progress-container"
                     aria-label="Reading progress">

                    <div class="progress-bar-background">

                        <div
                            class="progress-bar-fill"
                            style="width:${pct}%">
                        </div>

                    </div>

                    <p class="progress-text">
                        ${pct}% complete
                    </p>

                </div>
                `;
        }

        let metaText =
            "";

        if (
            page > 0 &&
            pages > 0
        ) {

            metaText =
                `Page ${page} of ${pages}`;

        } else if (
            lastRead
        ) {

            metaText =
                `Last opened ${lastRead}`;

        } else if (
            category === "purchased"
        ) {

            metaText =
                "Premium book";

        } else {

            metaText =
                categoryName;
        }

        const removeButton =
            category === "saved"
                ?
                `
                <button
                    type="button"
                    class="remove-btn"
                    data-remove-favorite="${escapeHtml(id)}">
                    Remove
                </button>
                `
                :
                "";

        return `
            <article
                class="reading-card library-book-card"
                data-book-id="${escapeHtml(id)}"
                data-category="${escapeHtml(category)}">

                <img
                    src="${image}"
                    alt="${title}"
                    class="book-cover"
                    loading="lazy"
                    onerror="this.onerror=null;this.src='/img/Book.png';">

                <div class="reading-info">

                    <h3>
                        ${title}
                    </h3>

                    <p class="reading-author">
                        ${author}
                    </p>

                    <div class="reading-meta">

                        <span class="reading-percent">
                            ${categoryName}
                        </span>

                        <span class="reading-time">
                            ${escapeHtml(metaText)}
                        </span>

                    </div>

                    ${progressHtml}

                    <div class="action-buttons">

                        <a
                            href="${actionUrl}"
                            class="continue-btn"
                            data-reader-link="${escapeHtml(id)}">

                            ${actionText}

                        </a>

                        ${removeButton}

                    </div>

                </div>

            </article>
        `;
    }

    /* =========================================================
       RENDER CATEGORY
       ========================================================= */

    function renderCategory(
        category
    ) {

        const grid =
            document.getElementById(
                category + "Grid"
            );

        const emptyState =
            document.getElementById(
                category + "Empty"
            );

        if (!grid) {
            return;
        }

        const books =
            Array.isArray(
                libraryData[category]
            )
                ? libraryData[category]
                : [];

        if (
            books.length === 0
        ) {

            grid.innerHTML = "";

            if (emptyState) {

                emptyState.textContent =
                    emptyMessage(
                        category
                    );

                emptyState.style.display =
                    "block";
            }

            return;
        }

        if (emptyState) {
            emptyState.style.display =
                "none";
        }

        grid.innerHTML =
            books
                .map(
                    book =>
                        createBookCard(
                            book,
                            category
                        )
                )
                .join("");

        attachGridEvents(
            grid
        );
    }

    /* =========================================================
       RENDER ALL
       ========================================================= */

    function renderLibrary() {

        renderCategory(
            "reading"
        );

        renderCategory(
            "saved"
        );

        renderCategory(
            "completed"
        );

        renderCategory(
            "purchased"
        );

        updateTabCounts();
    }

    /* =========================================================
       TAB COUNTS
       ========================================================= */

    function updateTabCounts() {

        const mapping = {
            "reading":
                ".library-tab[data-tab='currently-reading']",

            "saved":
                ".library-tab[data-tab='saved']",

            "completed":
                ".library-tab[data-tab='completed']",

            "purchased":
                ".library-tab[data-tab='purchased']"
        };

        Object.keys(mapping)
            .forEach(
                function (category) {

                    const button =
                        document.querySelector(
                            mapping[category]
                        );

                    if (!button) {
                        return;
                    }

                    const count =
                        Array.isArray(
                            libraryData[
                                category
                                ]
                        )
                            ? libraryData[
                                category
                                ].length
                            : 0;

                    let badge =
                        button.querySelector(
                            ".library-count"
                        );

                    if (!badge) {

                        badge =
                            document.createElement(
                                "span"
                            );

                        badge.className =
                            "library-count";

                        button.appendChild(
                            badge
                        );
                    }

                    badge.textContent =
                        count;
                }
            );
    }

    /* =========================================================
       GRID EVENTS
       ========================================================= */

    function attachGridEvents(
        grid
    ) {

        /*
         * Reader buttons are normal links,
         * so no JS redirect is required.
         */

        const cards =
            grid.querySelectorAll(
                ".library-book-card"
            );

        cards.forEach(
            function (card) {

                card.addEventListener(
                    "click",
                    function (event) {

                        if (
                            event.target.closest(
                                "a, button"
                            )
                        ) {
                            return;
                        }

                        const id =
                            card.dataset.bookId;

                        if (!id) {
                            return;
                        }

                        window.location.href =
                            "/book-reader?bookId=" +
                            encodeURIComponent(
                                id
                            );
                    }
                );
            }
        );

        /*
         * Remove favorite.
         */
        const removeButtons =
            grid.querySelectorAll(
                "[data-remove-favorite]"
            );

        removeButtons.forEach(
            function (button) {

                button.addEventListener(
                    "click",
                    async function (event) {

                        event.preventDefault();
                        event.stopPropagation();

                        const id =
                            this.getAttribute(
                                "data-remove-favorite"
                            );

                        if (!id) {
                            return;
                        }

                        await removeFavorite(
                            id
                        );
                    }
                );
            }
        );
    }

    /* =========================================================
       REMOVE FAVORITE
       ========================================================= */

    async function removeFavorite(
        bookIdValue
    ) {

        try {

            const response =
                await fetch(
                    "/api/favorites/" +
                    encodeURIComponent(
                        bookIdValue
                    ),
                    {
                        method:
                            "DELETE",

                        headers:
                            getAuthHeaders(),

                        credentials:
                            "include"
                    }
                );

            if (
                response.status === 401 ||
                response.status === 403
            ) {

                window.location.href =
                    "/login";

                return;
            }

            if (!response.ok) {

                throw new Error(
                    "Unable to remove saved book."
                );
            }

            showToast(
                "Removed from saved books."
            );

            await loadLibrary();

        } catch (error) {

            console.error(
                "READORA: Remove favorite failed.",
                error
            );

            showToast(
                "Could not remove the saved book."
            );
        }
    }

    /* =========================================================
       LOAD LIBRARY
       ========================================================= */

    async function loadLibrary() {

        const loading =
            document.getElementById(
                "libraryLoading"
            );

        const errorBox =
            document.getElementById(
                "libraryError"
            );

        if (loading) {
            loading.style.display =
                "block";
        }

        if (errorBox) {
            errorBox.style.display =
                "none";
        }

        try {

            const data =
                await fetchLibrary();

            libraryData =
                normalizeLibrary(
                    data
                );

            renderLibrary();

            await loadReadingHabits();

            await loadRecentNotes();

        } catch (error) {

            console.error(
                "READORA: Library loading failed:",
                error
            );

            libraryData = {
                reading: [],
                saved: [],
                completed: [],
                purchased: []
            };

            renderLibrary();

            if (errorBox) {

                errorBox.textContent =
                    error.message ||
                    "Unable to load your library.";

                errorBox.style.display =
                    "block";
            }

        } finally {

            if (loading) {
                loading.style.display =
                    "none";
            }
        }
    }

    /* =========================================================
       READING HABITS
       ========================================================= */

    async function loadReadingHabits() {

        try {

            const response =
                await fetch(
                    "/api/dashboard",
                    {
                        headers:
                            getAuthHeaders(),

                        credentials:
                            "include",

                        cache:
                            "no-cache"
                    }
                );

            if (!response.ok) {
                return;
            }

            const data =
                await response.json();

            const analytics =
                data.analytics ||
                {};

            const session =
                data.session ||
                {};

            const minutes =
                Number(
                    analytics.readingMinutes ??
                    analytics.minutes ??
                    session.minutes ??
                    0
                );

            const streak =
                Number(
                    analytics.currentStreak ??
                    analytics.streakDays ??
                    session.streak ??
                    0
                );

            const streakElement =
                document.getElementById(
                    "libraryStreak"
                );

            if (streakElement) {

                streakElement.textContent =
                    Number.isFinite(streak)
                        ? streak + " Day Streak"
                        : "0 Day Streak";
            }

            const streakLabel =
                document.getElementById(
                    "libraryStreakLabel"
                );

            if (streakLabel) {

                streakLabel.textContent =
                    streak > 0
                        ? "Keep your reading routine going."
                        : "Start reading to build your streak.";
            }

            const minutesElement =
                document.getElementById(
                    "libraryReadingTime"
                );

            if (minutesElement) {

                minutesElement.textContent =
                    formatMinutes(
                        minutes
                    );
            }

        } catch (error) {

            console.warn(
                "READORA: Reading habits unavailable.",
                error
            );
        }
    }

    function formatMinutes(
        value
    ) {

        const minutes =
            Number(value);

        if (
            !Number.isFinite(minutes) ||
            minutes <= 0
        ) {
            return "0m";
        }

        const hours =
            Math.floor(
                minutes / 60
            );

        const remaining =
            minutes % 60;

        if (hours <= 0) {
            return remaining + "m";
        }

        if (remaining <= 0) {
            return hours + "h";
        }

        return (
            hours +
            "h " +
            remaining +
            "m"
        );
    }

    /* =========================================================
       NOTES
       ========================================================= */

    async function loadRecentNotes() {

        const container =
            document.getElementById(
                "recentNotesContainer"
            );

        if (!container) {
            return;
        }

        try {

            const response =
                await fetch(
                    "/api/notes",
                    {
                        headers:
                            getAuthHeaders(),

                        credentials:
                            "include",

                        cache:
                            "no-cache"
                    }
                );

            if (
                !response.ok
            ) {
                return;
            }

            const data =
                await response.json();

            const notes =
                Array.isArray(data)
                    ? data
                    : Array.isArray(
                        data.notes
                    )
                        ? data.notes
                        : [];

            if (
                notes.length === 0
            ) {

                container.innerHTML =
                    `
                    <p class="note-text">
                        No notes or highlights yet.
                    </p>
                    `;

                return;
            }

            const note =
                notes[0];

            const text =
                note.noteText ||
                note.content ||
                note.text ||
                note.note ||
                "";

            const book =
                note.bookTitle ||
                (
                    note.book &&
                    note.book.title
                ) ||
                "Your reading";

            if (!text) {

                container.innerHTML =
                    `
                    <p class="note-text">
                        Your recent notes will appear here.
                    </p>
                    `;

                return;
            }

            container.innerHTML =
                `
                <div class="quote-card">
                    <p>
                        "${escapeHtml(text)}"
                    </p>
                </div>

                <p class="note-meta">
                    From: ${escapeHtml(book)}
                </p>

                <a
                    href="/notes"
                    class="view-notes-link">
                    View All Notes →
                </a>
                `;

        } catch (error) {

            console.warn(
                "READORA: Notes unavailable.",
                error
            );
        }
    }

    /* =========================================================
       TABS
       ========================================================= */

    function setupTabs() {

        const tabs =
            document.querySelectorAll(
                ".library-tab"
            );

        const panels =
            document.querySelectorAll(
                ".tab-panel"
            );

        tabs.forEach(
            function (tab) {

                tab.addEventListener(
                    "click",
                    function () {

                        const selected =
                            this.getAttribute(
                                "data-tab"
                            );

                        tabs.forEach(
                            function (item) {

                                item.classList.remove(
                                    "tab-active"
                                );
                            }
                        );

                        this.classList.add(
                            "tab-active"
                        );

                        panels.forEach(
                            function (panel) {

                                panel.classList.toggle(
                                    "panel-active",
                                    panel.id ===
                                    "panel-" +
                                    selected
                                );
                            }
                        );
                    }
                );
            }
        );
    }

    /* =========================================================
       TOAST
       ========================================================= */

    function showToast(
        message
    ) {

        const existing =
            document.querySelector(
                ".readora-library-toast"
            );

        if (existing) {
            existing.remove();
        }

        const toast =
            document.createElement(
                "div"
            );

        toast.className =
            "readora-library-toast";

        toast.textContent =
            message;

        Object.assign(
            toast.style,
            {
                position: "fixed",
                bottom: "24px",
                right: "24px",
                zIndex: "99999",
                padding: "12px 18px",
                borderRadius: "6px",
                background: "#2f4a3a",
                color: "#faf6ee",
                fontSize: "13px",
                boxShadow:
                    "0 8px 25px rgba(0,0,0,.18)",
                opacity: "0",
                transition:
                    "opacity .25s ease"
            }
        );

        document.body.appendChild(
            toast
        );

        requestAnimationFrame(
            function () {
                toast.style.opacity =
                    "1";
            }
        );

        setTimeout(
            function () {

                toast.style.opacity =
                    "0";

                setTimeout(
                    function () {

                        toast.remove();

                    },
                    300
                );

            },
            2200
        );
    }

    /* =========================================================
       INITIALIZE
       ========================================================= */

    document.addEventListener(
        "DOMContentLoaded",
        function () {

            setupTabs();

            loadLibrary();

        }
    );

})();