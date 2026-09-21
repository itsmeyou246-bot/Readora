/* =========================================================
   READORA — READER DASHBOARD
   COMPLETE FUNCTIONAL VERSION

   Keeps existing READORA dashboard UI/design.
   Uses real backend data.
   Mood recommendations are real.
   Free books -> Reader
   Premium books -> Book Details / Purchase
   ========================================================= */

(function () {

    "use strict";


    /* =========================================================
       API
       ========================================================= */

    const API = {
        dashboard: "/api/dashboard",
        history: "/api/history",
        bookmarks: "/api/bookmarks",
        notes: "/api/notes",
        notifications: "/api/notifications",
        subscription: "/api/subscription",
        purchases: "/api/purchases",
        weeklyActivity: "/api/activity/weekly",
        recommendations: "/api/recommendations",
        mood: "/api/mood"
    };


    /* =========================================================
       STATE
       ========================================================= */

    let authUser = {};

    let dashboardData = {};

    let historyData = [];

    let bookmarksData = [];

    let notesData = [];

    let notificationsData = [];

    let purchasesData = [];

    let subscriptionData = null;

    let weeklyData = [];


    /* =========================================================
       BASIC HELPERS
       ========================================================= */

    function byId(id) {
        return document.getElementById(id);
    }


    function firstValue() {

        for (let i = 0; i < arguments.length; i++) {

            const value = arguments[i];

            if (
                value !== undefined &&
                value !== null &&
                String(value).trim() !== ""
            ) {
                return value;
            }
        }

        return "";
    }


    function safeNumber(value, fallback) {

        const number = Number(value);

        if (Number.isFinite(number)) {
            return number;
        }

        return fallback === undefined ? 0 : fallback;
    }


    function escapeHtml(value) {

        if (value === null || value === undefined) {
            return "";
        }

        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }


    function toArray(value) {

        if (Array.isArray(value)) {
            return value;
        }

        if (!value || typeof value !== "object") {
            return [];
        }

        const possibleArrays = [
            value.data,
            value.items,
            value.content,
            value.results,
            value.history,
            value.bookmarks,
            value.notes,
            value.notifications,
            value.purchases,
            value.recommendations
        ];

        for (const array of possibleArrays) {

            if (Array.isArray(array)) {
                return array;
            }
        }

        return [];
    }


    /* =========================================================
       BOOK HELPERS
       ========================================================= */

    function getBookId(book) {

        if (!book || typeof book !== "object") {
            return "";
        }

        return firstValue(
            book.id,
            book.bookId,
            book.book_id,
            book.bookID
        );
    }


    function getBookTitle(book) {

        if (!book || typeof book !== "object") {
            return "Untitled Book";
        }

        return firstValue(
            book.title,
            book.bookTitle,
            book.name,
            "Untitled Book"
        );
    }


    function getBookAuthor(book) {

        if (!book || typeof book !== "object") {
            return "Unknown Author";
        }

        if (
            book.author &&
            typeof book.author === "object"
        ) {

            return firstValue(
                book.author.name,
                book.author.fullName,
                book.author.username,
                "Unknown Author"
            );
        }

        return firstValue(
            book.authorName,
            book.author,
            book.bookAuthor,
            "Unknown Author"
        );
    }


    function getBookCategory(book) {

        return firstValue(
            book && book.category,
            book && book.tag,
            book && book.genre,
            "GENERAL"
        );
    }


    function getBookImage(book) {

        if (!book || typeof book !== "object") {
            return "";
        }

        let image = firstValue(
            book.image,
            book.coverImage,
            book.cover,
            book.bookImage,
            book.thumbnail
        );

        if (!image) {
            return "";
        }

        image = String(image).replace(/\\/g, "/");


        if (image.startsWith("/static/")) {
            image = image.substring(7);
        }


        if (image.startsWith("/images/")) {
            image = "/img/" + image.substring(8);
        }


        if (
            !image.startsWith("http://") &&
            !image.startsWith("https://") &&
            !image.startsWith("/")
        ) {
            image = "/" + image;
        }

        return image;
    }


    function isPremium(book) {

        if (!book) {
            return false;
        }

        return (
            book.premium === true ||
            String(book.premium).toLowerCase() === "true"
        );
    }


    function getProgress(item) {

        if (!item) {
            return 0;
        }

        let progress = firstValue(
            item.progress,
            item.progressPercent,
            item.percentage,
            item.percent,
            item.readingProgress
        );

        progress = safeNumber(progress, 0);


        /*
         * API may return 0.65 instead of 65.
         */

        if (
            progress > 0 &&
            progress <= 1
        ) {
            progress *= 100;
        }


        return Math.max(
            0,
            Math.min(100, progress)
        );
    }


    function getCurrentPage(item) {

        if (!item) {
            return 0;
        }

        return safeNumber(
            firstValue(
                item.currentPage,
                item.page,
                item.currentPageNumber,
                item.lastPage,
                item.readingPage
            ),
            0
        );
    }


    function getTotalPages(item) {

        if (!item) {
            return 0;
        }

        return safeNumber(
            firstValue(
                item.pageCount,
                item.totalPages,
                item.totalPage,
                item.pages
            ),
            0
        );
    }


    function getChapter(item) {

        if (!item) {
            return "Chapter 1";
        }

        return firstValue(
            item.chapterTitle,
            item.chapter,
            item.currentChapter,
            item.chapterName,
            "Chapter 1"
        );
    }


    /* =========================================================
       FORMATTING
       ========================================================= */

    function formatMinutes(value) {

        const minutes =
            Math.max(
                0,
                Math.round(
                    safeNumber(value, 0)
                )
            );

        const hours =
            Math.floor(minutes / 60);

        const mins =
            minutes % 60;

        if (hours > 0) {
            return hours + "h " + mins + "m";
        }

        return mins + "m";
    }


    function formatPrice(value) {

        const price =
            safeNumber(value, 0);

        return (
            "Rs. " +
            price.toLocaleString("en-IN")
        );
    }


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
            return String(value);
        }

        return date.toLocaleDateString(
            "en-IN",
            {
                year: "numeric",
                month: "short",
                day: "numeric"
            }
        );
    }


    function getFirstName(name) {

        if (!name) {
            return "Reader";
        }

        return (
            String(name)
                .trim()
                .split(/\s+/)[0]
            || "Reader"
        );
    }


    function getGreeting() {

        const hour =
            new Date().getHours();

        if (hour < 12) {
            return "Good morning";
        }

        if (hour < 17) {
            return "Good afternoon";
        }

        return "Good evening";
    }


    /* =========================================================
       AUTH
       ========================================================= */

    function readStoredObject(
        storage,
        key
    ) {

        try {

            const value =
                storage.getItem(key);

            if (!value) {
                return null;
            }

            return JSON.parse(value);

        } catch (error) {

            return null;
        }
    }


    function getAuthUser() {

        let user =
            readStoredObject(
                localStorage,
                "readoraUser"
            );

        if (!user) {

            user =
                readStoredObject(
                    sessionStorage,
                    "readoraUser"
                );
        }

        if (!user) {

            user =
                readStoredObject(
                    localStorage,
                    "user"
                );
        }

        return user || {};
    }


    function getToken() {

        const keys = [
            "readoraToken",
            "accessToken",
            "access_token",
            "jwt",
            "token"
        ];


        for (const key of keys) {

            try {

                const value =
                    localStorage.getItem(key);

                if (value) {
                    return value;
                }

            } catch (error) {
                // ignore
            }


            try {

                const value =
                    sessionStorage.getItem(key);

                if (value) {
                    return value;
                }

            } catch (error) {
                // ignore
            }
        }


        const user =
            getAuthUser();


        return firstValue(
            user.token,
            user.accessToken,
            user.access_token,
            user.jwt,
            ""
        );
    }


    async function authFetch(
        url,
        options
    ) {

        const config =
            Object.assign(
                {
                    method: "GET",
                    credentials: "include",
                    headers: {}
                },
                options || {}
            );


        config.headers =
            Object.assign(
                {},
                config.headers
            );


        const token =
            getToken();


        if (token) {

            config.headers.Authorization =
                "Bearer " + token;
        }


        const response =
            await fetch(
                url,
                config
            );


        if (!response.ok) {

            throw new Error(
                "HTTP " +
                response.status +
                " - " +
                url
            );
        }


        const contentType =
            response.headers.get(
                "content-type"
            ) || "";


        if (
            contentType.includes(
                "application/json"
            )
        ) {

            return await response.json();
        }


        const text =
            await response.text();


        if (!text) {
            return {};
        }


        try {

            return JSON.parse(text);

        } catch (error) {

            return {
                text: text
            };
        }
    }


    /* =========================================================
       LOAD DATA
       ========================================================= */

    async function loadDashboard() {

        try {

            dashboardData =
                await authFetch(
                    API.dashboard
                );

        } catch (error) {

            console.error(
                "READORA dashboard error:",
                error
            );

            dashboardData = {};
        }
    }


    async function loadHistory() {

        try {

            historyData =
                toArray(
                    await authFetch(
                        API.history
                    )
                );

        } catch (error) {

            console.error(
                "READORA history error:",
                error
            );

            historyData = [];
        }
    }


    async function loadBookmarks() {

        try {

            bookmarksData =
                toArray(
                    await authFetch(
                        API.bookmarks
                    )
                );

        } catch (error) {

            console.error(
                "READORA bookmarks error:",
                error
            );

            bookmarksData = [];
        }
    }


    async function loadNotes() {

        try {

            notesData =
                toArray(
                    await authFetch(
                        API.notes
                    )
                );

        } catch (error) {

            console.error(
                "READORA notes error:",
                error
            );

            notesData = [];
        }
    }


    async function loadNotifications() {

        try {

            notificationsData =
                toArray(
                    await authFetch(
                        API.notifications
                    )
                );

        } catch (error) {

            console.error(
                "READORA notifications error:",
                error
            );

            notificationsData = [];
        }
    }


    async function loadSubscription() {

        try {

            subscriptionData =
                await authFetch(
                    API.subscription
                );

        } catch (error) {

            console.error(
                "READORA subscription error:",
                error
            );

            subscriptionData = null;
        }
    }


    async function loadPurchases() {

        try {

            purchasesData =
                toArray(
                    await authFetch(
                        API.purchases
                    )
                );

        } catch (error) {

            console.error(
                "READORA purchases error:",
                error
            );

            purchasesData = [];
        }
    }


    async function loadWeeklyActivity() {

        try {

            const result =
                await authFetch(
                    API.weeklyActivity
                );


            if (Array.isArray(result)) {

                weeklyData =
                    result;

            } else if (
                result &&
                Array.isArray(result.data)
            ) {

                weeklyData =
                    result.data;

            } else if (
                result &&
                Array.isArray(result.activity)
            ) {

                weeklyData =
                    result.activity;

            } else {

                weeklyData = [];
            }

        } catch (error) {

            console.error(
                "READORA weekly activity error:",
                error
            );

            weeklyData = [];
        }
    }


    /* =========================================================
       USER
       ========================================================= */

    function getDashboardUser() {

        if (
            dashboardData &&
            dashboardData.user &&
            typeof dashboardData.user === "object"
        ) {

            return Object.assign(
                {},
                authUser,
                dashboardData.user
            );
        }

        return authUser;
    }


    function renderUser() {

        const user =
            getDashboardUser();


        const name =
            firstValue(
                user.fullName,
                user.name,
                user.username,
                user.displayName,
                "Reader"
            );


        const roleRaw =
            firstValue(
                user.roleName,
                user.role,
                "Reader"
            );


        /*
         * USER should display as Reader.
         */

        let role =
            String(roleRaw);


        if (
            role.toUpperCase() === "USER"
        ) {

            role = "Reader";
        }


        const location =
            firstValue(
                user.location,
                user.city,
                user.addressCity,
                "Kathmandu"
            );


        const displayName =
            byId(
                "userDisplayName"
            );


        if (displayName) {

            displayName.textContent =
                name;
        }


        const roleBadge =
            byId(
                "userRoleBadge"
            );


        if (roleBadge) {

            roleBadge.textContent =
                role;
        }


        const userLocation =
            byId(
                "userLocation"
            );


        if (userLocation) {

            userLocation.textContent =
                role +
                " · " +
                location +
                " (NPT)";
        }


        const firstName =
            byId(
                "userFirstName"
            );


        if (firstName) {

            firstName.textContent =
                getFirstName(name);
        }


        const greeting =
            byId(
                "timeGreeting"
            );


        if (greeting) {

            greeting.textContent =
                getGreeting();
        }


        const sessionDate =
            byId(
                "sessionDate"
            );


        if (sessionDate) {

            sessionDate.textContent =
                new Date().toLocaleDateString(
                    "en-IN",
                    {
                        weekday: "long",
                        day: "numeric",
                        month: "long"
                    }
                );
        }


        renderWelcomeSummary();
    }


    /* =========================================================
       ANALYTICS / STATS
       ========================================================= */

    function getAnalytics() {

        return (
            dashboardData.analytics ||
            dashboardData.statistics ||
            dashboardData.stats ||
            {}
        );
    }


    function getBooksCompleted() {

        const analytics =
            getAnalytics();


        const goal =
            dashboardData.goal ||
            {};


        return safeNumber(
            firstValue(
                analytics.booksCompleted,
                analytics.completedBooks,
                analytics.booksRead,
                dashboardData.booksCompleted,
                goal.completed,
                goal.booksCompleted,
                0
            ),
            0
        );
    }


    function getReadingMinutes() {

        const analytics =
            getAnalytics();


        return safeNumber(
            firstValue(
                analytics.readingMinutes,
                analytics.minutesRead,
                analytics.totalMinutes,
                dashboardData.readingMinutes,
                0
            ),
            0
        );
    }


    function getReadingStreak() {

        const analytics =
            getAnalytics();


        return safeNumber(
            firstValue(
                analytics.currentStreak,
                analytics.readingStreak,
                dashboardData.currentStreak,
                0
            ),
            0
        );
    }


    function getBestStreak() {

        const analytics =
            getAnalytics();


        return safeNumber(
            firstValue(
                analytics.longestStreak,
                analytics.bestStreak,
                dashboardData.longestStreak,
                0
            ),
            0
        );
    }


    function getInProgressCount() {

        const dashboardStats =
            dashboardData.stats ||
            {};


        const value =
            firstValue(
                dashboardStats.inProgress,
                dashboardData.inProgress,
                null
            );


        if (value !== null) {

            return safeNumber(
                value,
                0
            );
        }


        return historyData.filter(
            function (item) {

                return String(
                        item.status || ""
                    ).toUpperCase() ===
                    "IN_PROGRESS";
            }
        ).length;
    }


    function renderWelcomeSummary() {

        const element =
            byId(
                "welcomeSummary"
            );


        if (!element) {
            return;
        }


        element.textContent =
            "You've completed " +
            getBooksCompleted() +
            " books and read " +
            formatMinutes(
                getReadingMinutes()
            ) +
            ", with a " +
            getReadingStreak() +
            "-day reading streak.";
    }


    function renderStats() {

        const booksRead =
            byId(
                "statBooksRead"
            );

        if (booksRead) {

            booksRead.textContent =
                getBooksCompleted();
        }


        const target =
            byId(
                "statBooksTarget"
            );


        const goal =
            dashboardData.goal ||
            {};


        const goalTarget =
            safeNumber(
                firstValue(
                    goal.target,
                    goal.goal,
                    dashboardData.goalTarget,
                    50
                ),
                50
            );


        if (target) {

            target.textContent =
                goalTarget;
        }


        const quota =
            byId(
                "statBooksQuota"
            );


        if (quota) {

            quota.textContent =
                Math.round(
                    (
                        getBooksCompleted() /
                        Math.max(
                            1,
                            goalTarget
                        )
                    ) * 100
                ) +
                "% of yearly quota";
        }


        const inProgress =
            byId(
                "statInProgress"
            );


        if (inProgress) {

            inProgress.textContent =
                getInProgressCount();
        }


        const readingTime =
            byId(
                "statReadingTime"
            );


        if (readingTime) {

            readingTime.textContent =
                formatMinutes(
                    getReadingMinutes()
                );
        }


        const streak =
            byId(
                "statReadingStreak"
            );


        if (streak) {

            streak.textContent =
                getReadingStreak();
        }


        const best =
            byId(
                "statStreakBest"
            );


        if (best) {

            best.textContent =
                "Personal best " +
                getBestStreak() +
                " days";
        }


        const inProgressSub =
            byId(
                "statInProgressSub"
            );


        if (inProgressSub) {

            inProgressSub.textContent =
                getInProgressCount() === 1
                    ? "active volume"
                    : "active volumes";
        }


        const readingTimeSub =
            byId(
                "statReadingTime"
            );


        if (readingTimeSub) {

            readingTimeSub.textContent =
                formatMinutes(
                    getReadingMinutes()
                );
        }
    }


    /* =========================================================
       CURRENT BOOK
       ========================================================= */

    function getCurrentBook() {

        if (
            dashboardData.currentBook
        ) {

            return dashboardData.currentBook;
        }


        const active =
            historyData.find(
                function (item) {

                    return String(
                            item.status || ""
                        ).toUpperCase() ===
                        "IN_PROGRESS";
                }
            );


        return active || null;
    }


    function renderCurrentBook() {

        const book =
            getCurrentBook();


        const mainContainer =
            byId(
                "mainBookContainer"
            );


        if (!mainContainer) {
            return;
        }


        if (!book) {

            mainContainer.innerHTML =
                `
                <div class="no-reading-book">

                    <i class="fa-solid fa-book-open"></i>

                    <h3>No active book</h3>

                    <p>
                        Start reading a book to continue here.
                    </p>

                </div>
                `;


            const subtitle =
                byId(
                    "continueSubTitle"
                );


            if (subtitle) {

                subtitle.textContent =
                    "No active reading session";
            }


            const welcomeButton =
                byId(
                    "welcomeContinueBtn"
                );


            if (welcomeButton) {

                welcomeButton.onclick =
                    function () {

                        window.location.href =
                            "/Book";
                    };
            }


            return;
        }


        const id =
            getBookId(book);


        const title =
            getBookTitle(book);


        const author =
            getBookAuthor(book);


        const category =
            getBookCategory(book);


        const progress =
            getProgress(book);


        const currentPage =
            getCurrentPage(book);


        const totalPages =
            getTotalPages(book);


        const chapter =
            getChapter(book);


        const image =
            getBookImage(book);


        const cover =
            byId(
                "mainBookCover"
            );


        if (cover) {

            if (image) {

                cover.innerHTML =
                    `
                    <img
                        src="${escapeHtml(image)}"
                        alt="${escapeHtml(title)}"
                    >
                    `;

            } else {

                cover.innerHTML =
                    `
                    <small>
                        READORA EDITION
                    </small>

                    <div class="book-middle">

                        <i class="fa-solid fa-book-open"></i>

                        <h3>
                            ${escapeHtml(title)}
                        </h3>

                        <em>
                            ${escapeHtml(author)}
                        </em>

                    </div>
                    `;
            }
        }


        const edition =
            byId(
                "mainBookEdition"
            );


        if (edition) {

            edition.textContent =
                "READORA EDITION";
        }


        const mainIcon =
            byId(
                "mainBookIcon"
            );


        if (mainIcon) {

            mainIcon.className =
                "fa-solid fa-book-open";
        }


        const titleElement =
            byId(
                "mainBookTitle"
            );


        if (titleElement) {

            titleElement.textContent =
                title;
        }


        const authorElement =
            byId(
                "mainBookAuthor"
            );


        if (authorElement) {

            authorElement.textContent =
                "By " + author;
        }


        const chapterElement =
            byId(
                "mainBookChapter"
            );


        if (chapterElement) {

            chapterElement.textContent =
                chapter;
        }


        const categoryElement =
            byId(
                "mainBookCategory"
            );


        if (categoryElement) {

            categoryElement.textContent =
                String(category)
                    .toUpperCase();
        }


        const detailsTitle =
            byId(
                "mainBookDetailsTitle"
            );


        if (detailsTitle) {

            detailsTitle.textContent =
                title;
        }


        const detailsMeta =
            byId(
                "mainBookDetailsMeta"
            );


        if (detailsMeta) {

            detailsMeta.textContent =
                "By " +
                author +
                " · " +
                category;
        }


        const pageText =
            byId(
                "mainBookPageText"
            );


        if (pageText) {

            if (totalPages > 0) {

                pageText.textContent =
                    "Page " +
                    currentPage +
                    " of " +
                    totalPages;

            } else {

                pageText.textContent =
                    "Page " +
                    currentPage;
            }
        }


        const percentText =
            byId(
                "mainBookPercentText"
            );


        if (percentText) {

            percentText.textContent =
                Math.round(progress) +
                "% completed";
        }


        const progressBar =
            byId(
                "mainBookProgressBar"
            );


        if (progressBar) {

            progressBar.style.width =
                progress + "%";
        }


        const notesCount =
            byId(
                "mainBookNotesCount"
            );


        if (notesCount) {

            const count =
                notesData.filter(
                    function (note) {

                        return String(
                                firstValue(
                                    note.bookId,
                                    note.book && note.book.id,
                                    ""
                                )
                            ) ===
                            String(id);
                    }
                ).length;


            notesCount.textContent =
                count + " Notes";
        }


        const continueButton =
            byId(
                "mainBookContinueBtn"
            );


        if (continueButton && id) {

            continueButton.onclick =
                function () {

                    window.location.href =
                        "/book-reader?bookId=" +
                        encodeURIComponent(id);
                };
        }


        const welcomeButton =
            byId(
                "welcomeContinueBtn"
            );


        if (welcomeButton && id) {

            welcomeButton.onclick =
                function () {

                    window.location.href =
                        "/book-reader?bookId=" +
                        encodeURIComponent(id);
                };
        }


        const subtitle =
            byId(
                "continueSubTitle"
            );


        if (subtitle) {

            subtitle.textContent =
                "Continue your active reading";
        }
    }


    /* =========================================================
       OTHER BOOKS
       ========================================================= */

    function renderOtherBooks() {

        const container =
            byId(
                "otherBooksList"
            );


        if (!container) {
            return;
        }


        const current =
            getCurrentBook();


        const currentId =
            current
                ? String(
                    getBookId(current)
                )
                : "";


        const books =
            historyData
                .filter(
                    function (item) {

                        return (
                            String(
                                item.status || ""
                            ).toUpperCase() ===
                            "IN_PROGRESS"
                        );
                    }
                )
                .filter(
                    function (item) {

                        return String(
                            getBookId(item)
                        ) !== currentId;
                    }
                )
                .slice(0, 3);


        if (!books.length) {

            container.innerHTML =
                `
                <p style="
                    color:#888;
                    padding:10px 0;
                ">
                    No other books in progress yet.
                </p>
                `;

            return;
        }


        container.innerHTML =
            books.map(
                function (item) {

                    const book =
                        item.book ||
                        item;


                    const id =
                        getBookId(book) ||
                        item.bookId;


                    const title =
                        getBookTitle(book);


                    const author =
                        getBookAuthor(book);


                    const progress =
                        getProgress(item);


                    const initials =
                        title
                            .split(/\s+/)
                            .map(
                                function (word) {
                                    return word.charAt(0);
                                }
                            )
                            .join("")
                            .slice(0, 2)
                            .toUpperCase();


                    return `
                    <div
                        class="small-book"
                        data-reader-book="${escapeHtml(id)}"
                        style="cursor:pointer;"
                    >

                        <div class="mini-cover">
                            ${escapeHtml(initials)}
                        </div>

                        <div class="small-info">

                            <strong>
                                ${escapeHtml(title)}
                            </strong>

                            <small>
                                ${escapeHtml(author)}
                            </small>

                            <div class="mini-progress">

                                <div
                                    style="
                                        width:${progress}%
                                    "
                                ></div>

                            </div>

                        </div>

                        <div class="percentage">

                            ${Math.round(progress)}%

                            <small>
                                Resume
                            </small>

                        </div>

                    </div>
                    `;
                }
            )
                .join("");


        container
            .querySelectorAll(
                "[data-reader-book]"
            )
            .forEach(
                function (element) {

                    element.addEventListener(
                        "click",
                        function () {

                            const id =
                                element.getAttribute(
                                    "data-reader-book"
                                );


                            if (!id) {
                                return;
                            }


                            window.location.href =
                                "/book-reader?bookId=" +
                                encodeURIComponent(id);
                        }
                    );
                }
            );
    }


    /* =========================================================
       GOALS
       ========================================================= */

    function renderGoals() {

        const goal =
            dashboardData.goal ||
            {};


        const completed =
            safeNumber(
                firstValue(
                    goal.completed,
                    goal.booksCompleted,
                    getBooksCompleted()
                ),
                0
            );


        const target =
            safeNumber(
                firstValue(
                    goal.target,
                    goal.goal,
                    50
                ),
                50
            );


        const percent =
            Math.min(
                100,
                Math.round(
                    (
                        completed /
                        Math.max(1, target)
                    ) * 100
                )
            );


        const completedElement =
            byId(
                "goalBooksCompleted"
            );


        if (completedElement) {

            completedElement.textContent =
                completed;
        }


        const targetElement =
            byId(
                "goalTarget"
            );


        if (targetElement) {

            targetElement.textContent =
                target;
        }


        const percentElement =
            byId(
                "goalPercent"
            );


        if (percentElement) {

            percentElement.textContent =
                percent + "%";
        }


        const progress =
            byId(
                "goalProgressBar"
            );


        if (progress) {

            progress.style.width =
                percent + "%";
        }


        const remaining =
            byId(
                "goalRemainingText"
            );


        if (remaining) {

            const left =
                Math.max(
                    0,
                    target - completed
                );


            remaining.textContent =
                left +
                " books remaining to fulfill your " +
                new Date().getFullYear() +
                " reading goal.";
        }


        const thisMonth =
            byId(
                "goalThisMonth"
            );


        if (thisMonth) {

            thisMonth.textContent =
                firstValue(
                    goal.thisMonth,
                    0
                );
        }


        const lastMonth =
            byId(
                "goalLastMonth"
            );


        if (lastMonth) {

            lastMonth.textContent =
                firstValue(
                    goal.lastMonth,
                    0
                );
        }
    }


    /* =========================================================
       ACTIVITY
       ========================================================= */

    function renderActivity() {

        const average =
            byId(
                "activityAvg"
            );


        const total =
            weeklyData.reduce(
                function (sum, item) {

                    return sum +
                        safeNumber(
                            firstValue(
                                item.minutes,
                                item.readingMinutes,
                                item.duration,
                                item.value,
                                0
                            ),
                            0
                        );
                },
                0
            );


        const avg =
            weeklyData.length
                ? Math.round(
                    total /
                    weeklyData.length
                )
                : 0;


        if (average) {

            average.textContent =
                avg + "m";
        }


        const peak =
            byId(
                "activityPeak"
            );


        if (peak) {

            if (weeklyData.length) {

                let best =
                    weeklyData[0];


                weeklyData.forEach(
                    function (item) {

                        if (
                            safeNumber(
                                item.minutes,
                                0
                            ) >
                            safeNumber(
                                best.minutes,
                                0
                            )
                        ) {

                            best = item;
                        }
                    }
                );


                const label =
                    firstValue(
                        best.day,
                        best.date,
                        best.label,
                        "This week"
                    );


                peak.textContent =
                    label +
                    " (" +
                    safeNumber(
                        firstValue(
                            best.minutes,
                            best.readingMinutes,
                            0
                        ),
                        0
                    ) +
                    " min)";

            } else {

                peak.textContent =
                    "No reading activity yet";
            }
        }


        const status =
            byId(
                "activityStatus"
            );


        if (status) {

            status.textContent =
                total > 0
                    ? "Weekly reading activity recorded"
                    : "No reading activity yet";
        }


        renderActivityChart();
    }


    function renderActivityChart() {

        const chart =
            byId(
                "activityChart"
            );


        if (!chart) {
            return;
        }


        if (!weeklyData.length) {

            chart.innerHTML =
                `
                <div class="activity-empty">
                    No reading activity recorded yet.
                </div>
                `;

            return;
        }


        chart.innerHTML =
            weeklyData
                .slice(0, 7)
                .map(
                    function (item) {

                        const minutes =
                            safeNumber(
                                firstValue(
                                    item.minutes,
                                    item.readingMinutes,
                                    item.duration,
                                    item.value,
                                    0
                                ),
                                0
                            );


                        const max =
                            Math.max(
                                1,
                                ...weeklyData.map(
                                    function (x) {

                                        return safeNumber(
                                            firstValue(
                                                x.minutes,
                                                x.readingMinutes,
                                                x.duration,
                                                x.value,
                                                0
                                            ),
                                            0
                                        );
                                    }
                                )
                            );


                        const height =
                            Math.max(
                                5,
                                Math.round(
                                    (
                                        minutes /
                                        max
                                    ) * 100
                                )
                            );


                        const label =
                            firstValue(
                                item.day,
                                item.label,
                                item.date,
                                ""
                            );


                        return `
                        <div>
                            <i
                                style="
                                    height:${height}%
                                "
                            ></i>

                            <span>
                                ${escapeHtml(
                            String(label).slice(0, 3)
                        )}
                            </span>
                        </div>
                        `;
                    }
                )
                .join("");
    }


    /* =========================================================
       MOOD
       ========================================================= */

    function getCurrentMood() {

        return firstValue(
            dashboardData.mood,
            dashboardData.currentMood,
            dashboardData.user &&
            dashboardData.user.currentMood,
            authUser.currentMood,
            "Calm"
        );
    }


    function updateMoodUI(mood) {

        document
            .querySelectorAll(
                ".moods .mood"
            )
            .forEach(
                function (button) {

                    const value =
                        button.getAttribute(
                            "data-mood"
                        );


                    if (
                        String(value).toLowerCase() ===
                        String(mood).toLowerCase()
                    ) {

                        button.classList.add(
                            "active"
                        );

                    } else {

                        button.classList.remove(
                            "active"
                        );
                    }
                }
            );


        const status =
            byId(
                "moodStatus"
            );


        if (status) {

            status.textContent =
                "Showing: " +
                mood;
        }
    }


    async function saveMood(mood) {

        try {

            await authFetch(
                API.mood,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify({
                            mood: mood
                        })
                }
            );

        } catch (error) {

            console.warn(
                "READORA: Could not save mood:",
                error
            );
        }
    }


    async function loadMoodRecommendations(
        mood
    ) {

        const container =
            byId(
                "recommendationsContainer"
            );


        if (container) {

            container.innerHTML =
                `
                <div
                    style="
                        padding:20px;
                        text-align:center;
                        color:#777;
                    "
                >
                    Finding books for your
                    ${escapeHtml(
                    String(mood).toLowerCase()
                )}
                    mood...
                </div>
                `;
        }


        try {

            const result =
                await authFetch(
                    API.recommendations +
                    "?mood=" +
                    encodeURIComponent(mood)
                );


            const recommendations =
                toArray(result);


            dashboardData.recommendations =
                recommendations;


            renderRecommendations(
                recommendations
            );

        } catch (error) {

            console.error(
                "READORA recommendation error:",
                error
            );


            if (container) {

                container.innerHTML =
                    `
                    <div class="empty-state">
                        No recommendations available
                        for this mood right now.
                    </div>
                    `;
            }
        }
    }


    function setupMoodButtons() {

        const buttons =
            document.querySelectorAll(
                ".moods .mood"
            );


        if (!buttons.length) {
            return;
        }


        buttons.forEach(
            function (button) {

                /*
                 * Prevent duplicate listeners
                 * if dashboard initializes again.
                 */
                if (
                    button.dataset.readoraMoodReady ===
                    "true"
                ) {
                    return;
                }


                button.dataset.readoraMoodReady =
                    "true";


                button.addEventListener(
                    "click",
                    async function () {

                        const mood =
                            button.getAttribute(
                                "data-mood"
                            );


                        if (!mood) {
                            return;
                        }


                        updateMoodUI(
                            mood
                        );


                        await saveMood(
                            mood
                        );


                        await loadMoodRecommendations(
                            mood
                        );
                    }
                );
            }
        );
    }


    async function refreshRecommendations() {

        const mood =
            getCurrentMood();


        updateMoodUI(
            mood
        );


        await loadMoodRecommendations(
            mood
        );
    }


    /* =========================================================
       REAL RECOMMENDATION CARDS
       ========================================================= */

    function renderRecommendations(
        recommendations
    ) {

        const container =
            byId(
                "recommendationsContainer"
            );


        if (!container) {
            return;
        }


        if (
            !Array.isArray(
                recommendations
            ) ||
            recommendations.length === 0
        ) {

            container.innerHTML =
                `
                <div class="empty-state">
                    No recommendations available
                    for this mood yet.
                </div>
                `;

            return;
        }


        container.innerHTML =
            recommendations
                .slice(0, 6)
                .map(
                    function (book) {

                        const id =
                            getBookId(book);


                        const title =
                            getBookTitle(book);


                        const author =
                            getBookAuthor(book);


                        const category =
                            getBookCategory(book);


                        const image =
                            getBookImage(book);


                        const premium =
                            isPremium(book);


                        const price =
                            premium
                                ? formatPrice(
                                    book.price
                                )
                                : "FREE";


                        const coverClass =
                            premium
                                ? "recommend-cover black"
                                : "recommend-cover light";


                        let cover;


                        /*
                         * IMPORTANT:
                         * Real cover is contained inside
                         * the ORIGINAL recommendation cover.
                         *
                         * This prevents the huge-image problem.
                         */

                        if (image) {

                            cover =
                                `
                                <img
                                    src="${escapeHtml(image)}"
                                    alt="${escapeHtml(title)}"
                                    loading="lazy"
                                >
                                `;

                        } else {

                            cover =
                                `
                                <i class="fa-solid fa-book-open"></i>

                                <small>
                                    ${escapeHtml(
                                    String(category)
                                        .toUpperCase()
                                )}
                                </small>

                                <h3>
                                    ${escapeHtml(title)}
                                </h3>

                                <em>
                                    ${escapeHtml(author)}
                                </em>
                                `;
                        }


                        const action =
                            premium
                                ? "Unlock"
                                : "Read Now";


                        return `
                        <article
                            class="recommend card"
                            data-book-id="${escapeHtml(
                            String(id)
                        )}"
                        >

                            <div
                                class="${coverClass}"
                            >

                                ${cover}

                            </div>


                            <div class="match">

                                Personalized Match

                                <span>
                                    ${escapeHtml(
                            String(category)
                        )}
                                </span>

                            </div>


                            <h3>
                                ${escapeHtml(title)}
                            </h3>


                            <p>
                                By
                                ${escapeHtml(author)}
                            </p>


                            <div class="book-footer">

                                <strong>
                                    ${escapeHtml(price)}
                                </strong>


                                <button
                                    type="button"
                                    class="recommend-action"
                                >

                                    ${action}

                                    <i
                                        class="fa-solid fa-arrow-right"
                                    ></i>

                                </button>

                            </div>

                        </article>
                        `;
                    }
                )
                .join("");


        /*
         * Click cards.
         */

        container
            .querySelectorAll(
                "[data-book-id]"
            )
            .forEach(
                function (card) {

                    card.addEventListener(
                        "click",
                        function (event) {

                            const id =
                                card.getAttribute(
                                    "data-book-id"
                                );


                            if (!id) {
                                return;
                            }


                            const book =
                                recommendations.find(
                                    function (item) {

                                        return String(
                                                getBookId(item)
                                            ) ===
                                            String(id);
                                    }
                                );


                            if (!book) {
                                return;
                            }


                            /*
                             * FREE BOOK
                             * Directly open reader.
                             */

                            if (
                                !isPremium(book)
                            ) {

                                window.location.href =
                                    "/book-reader?bookId=" +
                                    encodeURIComponent(
                                        id
                                    );

                                return;
                            }


                            /*
                             * PREMIUM BOOK
                             * Go to book details.
                             * Existing eSewa purchase flow
                             * handles unlocking.
                             */

                            window.location.href =
                                "/book-details?id=" +
                                encodeURIComponent(
                                    id
                                );
                        }
                    );
                }
            );
    }


    /* =========================================================
       BOOKMARKS
       ========================================================= */

    function renderBookmarks() {

        const container =
            byId(
                "bookmarksList"
            );


        if (!container) {
            return;
        }


        if (!bookmarksData.length) {

            container.innerHTML =
                `
                <div class="empty-state">
                    No bookmarks yet.
                </div>
                `;

            return;
        }


        container.innerHTML =
            bookmarksData
                .slice(0, 5)
                .map(
                    function (bookmark) {

                        const title =
                            firstValue(
                                bookmark.bookTitle,
                                bookmark.title,
                                bookmark.book &&
                                bookmark.book.title,
                                "Book"
                            );


                        const chapter =
                            firstValue(
                                bookmark.chapter,
                                bookmark.page,
                                "Saved page"
                            );


                        const text =
                            firstValue(
                                bookmark.text,
                                bookmark.quote,
                                bookmark.content,
                                "Bookmark saved."
                            );


                        return `
                        <div class="note">

                            <small>
                                ${escapeHtml(title)}
                                ·
                                ${escapeHtml(chapter)}
                            </small>

                            <p>
                                ${escapeHtml(text)}
                            </p>

                        </div>
                        `;
                    }
                )
                .join("");
    }


    /* =========================================================
       NOTES
       ========================================================= */

    function renderNotes() {

        const container =
            byId(
                "notesList"
            );


        if (!container) {
            return;
        }


        if (!notesData.length) {

            container.innerHTML =
                `
                <div class="empty-state">
                    No notes yet.
                </div>
                `;

            return;
        }


        container.innerHTML =
            notesData
                .slice(0, 5)
                .map(
                    function (note) {

                        const title =
                            firstValue(
                                note.bookTitle,
                                note.title,
                                note.book &&
                                note.book.title,
                                "Book"
                            );


                        const content =
                            firstValue(
                                note.content,
                                note.text,
                                note.note,
                                "Note saved."
                            );


                        const date =
                            firstValue(
                                note.createdAt,
                                note.date,
                                ""
                            );


                        return `
                        <div class="note">

                            <small>
                                ${escapeHtml(title)}
                                ${
                            date
                                ? " · " +
                                escapeHtml(
                                    formatDate(date)
                                )
                                : ""
                        }
                            </small>

                            <p>
                                ${escapeHtml(content)}
                            </p>

                        </div>
                        `;
                    }
                )
                .join("");
    }


    /* =========================================================
       SUBSCRIPTION
       ========================================================= */

    function renderSubscription() {

        const container =
            byId(
                "subscriptionContent"
            );


        if (!container) {
            return;
        }


        if (
            !subscriptionData ||
            subscriptionData.active === false
        ) {

            container.innerHTML =
                `
                <div class="empty-state">

                    <i class="fa-solid fa-gem"></i>

                    <p>
                        No active subscription.
                    </p>

                    <a href="/subscription">
                        Explore Subscription
                    </a>

                </div>
                `;

            return;
        }


        const status =
            firstValue(
                subscriptionData.status,
                subscriptionData.state,
                "ACTIVE"
            );


        const expiry =
            firstValue(
                subscriptionData.endDate,
                subscriptionData.expiryDate,
                subscriptionData.expiresAt,
                ""
            );


        container.innerHTML =
            `
            <div class="status">

                <i class="fa-solid fa-circle"></i>

                READORA Premium
                ${escapeHtml(status)}

            </div>

            ${
                expiry
                    ? `
                        <p>
                            Renews / expires on
                            <b>
                                ${escapeHtml(
                        formatDate(expiry)
                    )}
                            </b>
                        </p>
                      `
                    : ""
            }

            <a href="/subscription">

                Manage Subscription

                <i
                    class="fa-solid fa-arrow-right"
                ></i>

            </a>
            `;
    }


    /* =========================================================
       ANALYTICS
       ========================================================= */

    function renderAnalytics() {

        const analytics =
            getAnalytics();


        const pages =
            byId(
                "analyticsPagesRead"
            );


        if (pages) {

            pages.textContent =
                safeNumber(
                    firstValue(
                        analytics.pagesRead,
                        analytics.totalPages,
                        0
                    ),
                    0
                ).toLocaleString(
                    "en-IN"
                );
        }


        const session =
            byId(
                "analyticsAvgSession"
            );


        if (session) {

            session.textContent =
                formatMinutes(
                    firstValue(
                        analytics.averageSessionMinutes,
                        analytics.avgSession,
                        0
                    )
                );
        }


        const category =
            byId(
                "analyticsTopCategory"
            );


        if (category) {

            category.textContent =
                firstValue(
                    analytics.topCategory,
                    "—"
                );
        }
    }


    /* =========================================================
       PURCHASES
       ========================================================= */

    function renderPurchases() {

        const container =
            byId(
                "purchasesList"
            );


        if (!container) {
            return;
        }


        if (!purchasesData.length) {

            container.innerHTML =
                `
                <div class="empty-state">
                    No purchases yet.
                </div>
                `;

            return;
        }


        container.innerHTML =
            purchasesData
                .slice(0, 5)
                .map(
                    function (purchase) {

                        const title =
                            firstValue(
                                purchase.bookTitle,
                                purchase.title,
                                purchase.book &&
                                purchase.book.title,
                                "Book"
                            );


                        const amount =
                            firstValue(
                                purchase.amount,
                                purchase.price,
                                purchase.total,
                                0
                            );


                        const gateway =
                            firstValue(
                                purchase.paymentMethod,
                                purchase.gateway,
                                "eSewa"
                            );


                        const date =
                            firstValue(
                                purchase.purchasedAt,
                                purchase.createdAt,
                                purchase.date,
                                ""
                            );


                        return `
                        <div class="purchase">

                            <b>
                                ${escapeHtml(title)}
                            </b>

                            <strong>
                                ${escapeHtml(
                            formatPrice(amount)
                        )}
                            </strong>

                            <small>

                                ${
                            date
                                ? escapeHtml(
                                    formatDate(date)
                                ) +
                                " · "
                                : ""
                        }

                                ${escapeHtml(gateway)}

                            </small>

                        </div>
                        `;
                    }
                )
                .join("");
    }


    /* =========================================================
       NOTIFICATIONS
       ========================================================= */

    function isUnread(notification) {

        if (!notification) {
            return false;
        }


        if (
            notification.read === false ||
            notification.isRead === false ||
            notification.unread === true
        ) {

            return true;
        }


        return (
            String(
                firstValue(
                    notification.status,
                    notification.state,
                    ""
                )
            ).toLowerCase() ===
            "unread"
        );
    }


    function renderNotifications() {

        const container =
            byId(
                "notificationsList"
            );


        const unreadCount =
            notificationsData.filter(
                isUnread
            ).length;


        const countElement =
            byId(
                "notificationsUnreadCount"
            );


        if (countElement) {

            countElement.textContent =
                unreadCount;
        }


        if (!container) {
            return;
        }


        if (!notificationsData.length) {

            container.innerHTML =
                `
                <div class="empty-state">
                    No notifications yet.
                </div>
                `;

            return;
        }


        container.innerHTML =
            notificationsData
                .slice(0, 10)
                .map(
                    function (notification) {

                        const title =
                            firstValue(
                                notification.title,
                                notification.subject,
                                "Notification"
                            );


                        const message =
                            firstValue(
                                notification.message,
                                notification.content,
                                notification.body,
                                ""
                            );


                        const date =
                            firstValue(
                                notification.createdAt,
                                notification.date,
                                notification.timestamp,
                                ""
                            );


                        return `
                        <div
                            class="notification-item"
                        >

                            <strong>
                                ${escapeHtml(title)}
                            </strong>

                            ${
                            message
                                ? `
                                        <p>
                                            ${escapeHtml(
                                    message
                                )}
                                        </p>
                                      `
                                : ""
                        }

                            ${
                            date
                                ? `
                                        <small>
                                            ${escapeHtml(
                                    formatDate(date)
                                )}
                                        </small>
                                      `
                                : ""
                        }

                        </div>
                        `;
                    }
                )
                .join("");
    }


    /* =========================================================
       SMOOTH SCROLL
       ========================================================= */

    function setupSmoothScroll() {

        document
            .querySelectorAll(
                'a[href^="#"]'
            )
            .forEach(
                function (link) {

                    if (
                        link.dataset.readoraScrollReady ===
                        "true"
                    ) {
                        return;
                    }


                    link.dataset.readoraScrollReady =
                        "true";


                    link.addEventListener(
                        "click",
                        function (event) {

                            const href =
                                link.getAttribute(
                                    "href"
                                );


                            if (
                                !href ||
                                href === "#"
                            ) {
                                return;
                            }


                            const target =
                                document.querySelector(
                                    href
                                );


                            if (!target) {
                                return;
                            }


                            event.preventDefault();


                            target.scrollIntoView({
                                behavior: "smooth",
                                block: "start"
                            });
                        }
                    );
                }
            );
    }


    /* =========================================================
       INITIALIZATION
       ========================================================= */

    async function initializeDashboard() {

        console.log(
            "READORA: Dashboard initialization started."
        );


        authUser =
            getAuthUser();


        /*
         * Main dashboard.
         */

        await loadDashboard();


        /*
         * Independent sections.
         * One failing endpoint does not break
         * the entire dashboard.
         */

        await Promise.allSettled([
            loadHistory(),
            loadBookmarks(),
            loadNotes(),
            loadNotifications(),
            loadSubscription(),
            loadPurchases(),
            loadWeeklyActivity()
        ]);


        /*
         * Render real data.
         */

        renderUser();

        renderStats();

        renderCurrentBook();

        renderOtherBooks();

        renderGoals();

        renderActivity();

        renderBookmarks();

        renderNotes();

        renderSubscription();

        renderAnalytics();

        renderPurchases();

        renderNotifications();


        /*
         * Real mood + recommendations.
         */

        await refreshRecommendations();


        setupMoodButtons();

        setupSmoothScroll();


        console.log(
            "READORA: Dashboard initialization completed."
        );
    }


    function startDashboard() {

        initializeDashboard()
            .catch(
                function (error) {

                    console.error(
                        "READORA: Dashboard initialization failed:",
                        error
                    );
                }
            );
    }


    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            startDashboard
        );

    } else {

        startDashboard();
    }

})();