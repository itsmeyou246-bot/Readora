(function () {

    document.addEventListener("DOMContentLoaded", function () {

        // =====================================================
        // BOOK ID
        // =====================================================

        const bookId =
            document.body.dataset.bookId;

        // =====================================================
        // ELEMENTS
        // =====================================================

        const readerStatus =
            document.getElementById("readerStatus");

        const pageContent =
            document.getElementById("pageContent");

        const pageText =
            document.getElementById("pageText");

        const editorialImage =
            document.getElementById("editorialImage");

        const pageNumber =
            document.getElementById("pageNumber");

        const pageCount =
            document.getElementById("pageCount");

        const previousButton =
            document.getElementById("previousButton");

        const nextButton =
            document.getElementById("nextButton");

        const bookTitle =
            document.getElementById("bookTitle");

        const chapterLabel =
            document.getElementById("chapterLabel");

        const pdfContent =
            document.getElementById("pdfContent");

        const bookPdfFrame =
            document.getElementById("bookPdfFrame");

        // =====================================================
        // READER STATE
        // =====================================================

        let currentPage = 1;

        let totalPages = 0;

        let loadingPage = false;

        let bookIsUnlocked = false;

        // =====================================================
        // BOOK ID CHECK
        // =====================================================

        if (!bookId) {

            showError("Book ID is missing.");

            return;
        }

        // =====================================================
        // AUTHENTICATION
        // =====================================================

        let user = null;

        try {

            user =
                JSON.parse(
                    localStorage.getItem("readoraUser") || "null"
                );

        } catch (error) {

            console.error(
                "Unable to read readoraUser:",
                error
            );

            user = null;
        }

        const token =
            user && user.token
                ? user.token
                : localStorage.getItem("token");

        const headers = {
            "Accept": "application/json"
        };

        if (token) {

            headers["Authorization"] =
                "Bearer " + token;
        }

        // =====================================================
        // START
        // =====================================================

        checkAccess();

        // =====================================================
        // CHECK ACCESS
        // =====================================================

        function checkAccess() {

            setStatus(
                "Opening your book..."
            );

            fetch(
                "/api/reader/" +
                encodeURIComponent(bookId) +
                "/access",
                {
                    method: "GET",
                    headers: headers,
                    credentials: "include"
                }
            )
                .then(function (response) {

                    return response
                        .json()
                        .catch(function () {
                            return {};
                        })
                        .then(function (data) {

                            console.log(
                                "READORA reader access:",
                                response.status,
                                data
                            );

                            // -------------------------------
                            // NOT FOUND
                            // -------------------------------

                            if (response.status === 404) {

                                showError(
                                    data.message ||
                                    "This book has not been published yet."
                                );

                                return null;
                            }

                            // -------------------------------
                            // LOGIN REQUIRED
                            // -------------------------------

                            if (response.status === 401) {

                                showError(
                                    data.message ||
                                    "Please login to read this book."
                                );

                                return null;
                            }

                            // -------------------------------
                            // PREMIUM LOCK
                            // -------------------------------

                            if (response.status === 403) {

                                showLocked(
                                    data.message ||
                                    "This book is locked."
                                );

                                return null;
                            }

                            // -------------------------------
                            // OTHER ERROR
                            // -------------------------------

                            if (!response.ok) {

                                throw new Error(
                                    data.message ||
                                    "Unable to verify reader access."
                                );
                            }

                            // -------------------------------
                            // ACCESS CHECK
                            // -------------------------------

                            if (data.unlocked !== true) {

                                showLocked(
                                    data.message ||
                                    "This book is locked."
                                );

                                return null;
                            }

                            return data;
                        });
                })

                .then(function (access) {

                    if (!access) {
                        return;
                    }

                    bookIsUnlocked = true;

                    // =================================================
                    // TITLE
                    // =================================================

                    if (
                        bookTitle &&
                        access.title
                    ) {

                        bookTitle.textContent =
                            access.title;
                    }

                    // =================================================
                    // PAGE COUNT
                    // =================================================

                    totalPages =
                        Number(access.pageCount) || 0;

                    if (pageCount) {

                        pageCount.textContent =
                            totalPages;
                    }

                    // =================================================
                    // IMPORTANT:
                    // AUTHOR-UPLOADED PDF
                    // =================================================

                    if (
                        totalPages <= 0 &&
                        access.fileUrl
                    ) {

                        openPdfReader(
                            access.fileUrl,
                            access.title
                        );

                        return;
                    }

                    // =================================================
                    // NO CONTENT AT ALL
                    // =================================================

                    if (totalPages <= 0) {

                        showError(
                            "This book does not have any readable content yet."
                        );

                        return;
                    }

                    // =================================================
                    // NORMAL PAGE READER
                    // =================================================

                    currentPage =
                        Number(access.lastReadPage) || 1;

                    if (currentPage < 1) {

                        currentPage = 1;
                    }

                    if (currentPage > totalPages) {

                        currentPage = 1;
                    }

                    loadPage(currentPage);

                })

                .catch(function (error) {

                    console.error(
                        "Reader access error:",
                        error
                    );

                    showError(
                        error.message ||
                        "The reader could not be opened."
                    );

                });
        }

        // =====================================================
        // PDF READER
        // =====================================================

        function openPdfReader(
            fileUrl,
            title
        ) {

            if (!fileUrl) {

                showError(
                    "The manuscript file is not available."
                );

                return;
            }

            console.log(
                "READORA opening uploaded manuscript:",
                fileUrl
            );

            // Hide normal page reader

            if (pageContent) {

                pageContent.hidden =
                    true;
            }

            // Hide status

            if (readerStatus) {

                readerStatus.hidden =
                    true;
            }

            // Show PDF

            if (pdfContent) {

                pdfContent.hidden =
                    false;
            }

            if (bookPdfFrame) {

                bookPdfFrame.src =
                    fileUrl;

            }

            if (bookTitle && title) {

                bookTitle.textContent =
                    title;
            }

            if (chapterLabel) {

                chapterLabel.textContent =
                    "Manuscript";
            }

            // PDF is handled by browser viewer

            if (pageNumber) {

                pageNumber.textContent =
                    "PDF";
            }

            if (pageCount) {

                pageCount.textContent =
                    "—";
            }

            if (previousButton) {

                previousButton.disabled =
                    true;
            }

            if (nextButton) {

                nextButton.disabled =
                    true;
            }

            initReviewSection();
        }

        // =====================================================
        // LOAD PAGE
        // =====================================================

        function loadPage(page) {

            if (loadingPage) {
                return;
            }

            if (totalPages <= 0) {

                showError(
                    "This book does not have any readable pages yet."
                );

                return;
            }

            if (page < 1) {

                page = 1;
            }

            if (page > totalPages) {

                page = totalPages;
            }

            loadingPage = true;

            setStatus(
                "Loading page " +
                page +
                "..."
            );

            fetch(
                "/api/reader/" +
                encodeURIComponent(bookId) +
                "/pages/" +
                encodeURIComponent(page),
                {
                    method: "GET",
                    headers: headers,
                    credentials: "include"
                }
            )
                .then(function (response) {

                    return response
                        .json()
                        .catch(function () {
                            return {};
                        })
                        .then(function (data) {

                            if (response.status === 401) {

                                showError(
                                    data.message ||
                                    "Please login to continue reading."
                                );

                                return null;
                            }

                            if (response.status === 403) {

                                if (bookIsUnlocked) {

                                    showLocked(
                                        data.message ||
                                        "This book is locked. Please unlock it to continue reading."
                                    );

                                } else {

                                    showLocked(
                                        data.message ||
                                        "This book is locked."
                                    );
                                }

                                return null;
                            }

                            if (response.status === 404) {

                                showError(
                                    data.message ||
                                    "This book page does not exist."
                                );

                                return null;
                            }

                            if (!response.ok) {

                                throw new Error(
                                    data.message ||
                                    "Unable to load this page."
                                );
                            }

                            return data;
                        });
                })

                .then(function (data) {

                    if (!data) {
                        return;
                    }

                    renderPage(data);

                })

                .catch(function (error) {

                    console.error(
                        "Page loading error:",
                        error
                    );

                    showError(
                        error.message ||
                        "This page could not be loaded."
                    );

                })

                .finally(function () {

                    loadingPage = false;

                });
        }

        // =====================================================
        // RENDER NORMAL PAGE
        // =====================================================

        function renderPage(data) {

            // Make sure PDF is hidden

            if (pdfContent) {

                pdfContent.hidden =
                    true;
            }

            currentPage =
                Number(data.pageNumber) || 1;

            totalPages =
                Number(data.pageCount) || totalPages;

            // -------------------------------
            // PAGE NUMBER
            // -------------------------------

            if (pageNumber) {

                pageNumber.textContent =
                    currentPage;
            }

            // -------------------------------
            // PAGE COUNT
            // -------------------------------

            if (pageCount) {

                pageCount.textContent =
                    totalPages;
            }

            // -------------------------------
            // TITLE
            // -------------------------------

            if (
                bookTitle &&
                data.title
            ) {

                bookTitle.textContent =
                    data.title;
            }

            // -------------------------------
            // CHAPTER
            // -------------------------------

            if (chapterLabel) {

                chapterLabel.textContent =
                    data.chapterLabel ||
                    "Reading";
            }

            // -------------------------------
            // TEXT
            // -------------------------------

            if (pageText) {

                pageText.textContent =
                    data.textContent || "";
            }

            // -------------------------------
            // IMAGE
            // -------------------------------

            if (editorialImage) {

                const image =
                    editorialImage.querySelector("img");

                if (
                    image &&
                    data.imageUrl
                ) {

                    image.src =
                        data.imageUrl;

                    image.alt =
                        data.imageTitle ||
                        "Reading image";

                    editorialImage.hidden =
                        false;

                } else {

                    editorialImage.hidden =
                        true;
                }
            }

            // -------------------------------
            // SHOW PAGE
            // -------------------------------

            if (pageContent) {

                pageContent.hidden =
                    false;
            }

            // -------------------------------
            // HIDE STATUS
            // -------------------------------

            if (readerStatus) {

                readerStatus.hidden =
                    true;
            }

            // -------------------------------
            // PREVIOUS
            // -------------------------------

            if (previousButton) {

                previousButton.disabled =
                    currentPage <= 1;
            }

            // -------------------------------
            // NEXT
            // -------------------------------

            if (nextButton) {

                nextButton.disabled =
                    currentPage >= totalPages;
            }

            initReviewSection();
        }

        // =====================================================
        // PREVIOUS
        // =====================================================

        if (previousButton) {

            previousButton.addEventListener(
                "click",
                function () {

                    if (
                        currentPage > 1 &&
                        !loadingPage
                    ) {

                        loadPage(
                            currentPage - 1
                        );
                    }
                }
            );
        }

        // =====================================================
        // NEXT
        // =====================================================

        if (nextButton) {

            nextButton.addEventListener(
                "click",
                function () {

                    if (
                        currentPage < totalPages &&
                        !loadingPage
                    ) {

                        loadPage(
                            currentPage + 1
                        );
                    }
                }
            );
        }

        // =====================================================
        // STATUS
        // =====================================================

        function setStatus(message) {

            if (!readerStatus) {
                return;
            }

            readerStatus.hidden =
                false;

            readerStatus.textContent =
                message;
        }

        // =====================================================
        // LOCKED
        // =====================================================

        function showLocked(message) {

            const reviewSection =
                document.getElementById("reviewSection");
            if (reviewSection) {
                reviewSection.style.display = "none";
            }

            if (pageContent) {

                pageContent.hidden =
                    true;
            }

            if (pdfContent) {

                pdfContent.hidden =
                    true;
            }

            if (readerStatus) {

                readerStatus.hidden =
                    false;

                readerStatus.innerHTML =
                    "";

                const messageEl =
                    document.createElement("p");

                messageEl.textContent =
                    message;

                readerStatus.appendChild(
                    messageEl
                );

                const unlockButton =
                    document.createElement("button");

                unlockButton.type =
                    "button";

                unlockButton.id =
                    "unlockBookButton";

                unlockButton.textContent =
                    "Unlock this Book";

                unlockButton.addEventListener(
                    "click",
                    function () {

                        window.location.href =
                            "/book-details?id=" +
                            encodeURIComponent(bookId);
                    }
                );

                readerStatus.appendChild(
                    unlockButton
                );
            }

            disableNavigation();
        }

        // =====================================================
        // ERROR
        // =====================================================

        function showError(message) {

            const reviewSection =
                document.getElementById("reviewSection");
            if (reviewSection) {
                reviewSection.style.display = "none";
            }

            if (pageContent) {

                pageContent.hidden =
                    true;
            }

            if (pdfContent) {

                pdfContent.hidden =
                    true;
            }

            if (readerStatus) {

                readerStatus.hidden =
                    false;

                readerStatus.textContent =
                    message;
            }

            disableNavigation();
        }

        // =====================================================
        // DISABLE NAVIGATION
        // =====================================================

        function disableNavigation() {

            if (previousButton) {

                previousButton.disabled =
                    true;
            }

            if (nextButton) {

                nextButton.disabled =
                    true;
            }
        }

        // =====================================================
        // REVIEWS
        // =====================================================

        function initReviewSection() {
            const reviewSection = document.getElementById("reviewSection");
            if (!reviewSection) return;

            reviewSection.style.display = "block";

            const starRow = document.getElementById("starRow");
            const reviewComment = document.getElementById("reviewComment");
            const reviewFeedback = document.getElementById("reviewFeedback");
            const submitReviewBtn = document.getElementById("submitReviewBtn");
            const reviewHeading = document.getElementById("reviewHeading");
            let selectedRating = 0;

            const starBtns = starRow ? starRow.querySelectorAll(".star-btn") : [];

            function updateStarDisplay(rating) {
                starBtns.forEach(function (btn) {
                    const val = Number(btn.getAttribute("data-star"));
                    const icon = btn.querySelector("i");
                    if (val <= rating) {
                        btn.classList.add("filled");
                        if (icon) {
                            icon.className = "fa-solid fa-star";
                        }
                    } else {
                        btn.classList.remove("filled");
                        if (icon) {
                            icon.className = "fa-regular fa-star";
                        }
                    }
                });
            }

            starBtns.forEach(function (btn) {
                btn.addEventListener("click", function () {
                    selectedRating = Number(this.getAttribute("data-star"));
                    updateStarDisplay(selectedRating);
                });
                btn.addEventListener("mouseenter", function () {
                    updateStarDisplay(Number(this.getAttribute("data-star")));
                });
            });

            if (starRow) {
                starRow.addEventListener("mouseleave", function () {
                    updateStarDisplay(selectedRating);
                });
            }

            // Fetch existing reviews for this book to check if current user already reviewed it
            fetch("/api/reviews/book/" + encodeURIComponent(bookId))
                .then(function (res) {
                    if (res.ok) return res.json();
                    return [];
                })
                .then(function (reviews) {
                    if (!Array.isArray(reviews) || !user) return;
                    const existing = reviews.find(function (r) {
                        return (user.name && r.reviewerName && r.reviewerName.toLowerCase() === user.name.toLowerCase());
                    });
                    if (existing) {
                        selectedRating = existing.rating || 0;
                        updateStarDisplay(selectedRating);
                        if (reviewComment && existing.comment) {
                            reviewComment.value = existing.comment;
                        }
                        if (reviewHeading) {
                            reviewHeading.textContent = "Your Review";
                        }
                        if (submitReviewBtn) {
                            submitReviewBtn.textContent = "Update Review";
                        }
                    }
                })
                .catch(function () {});

            if (submitReviewBtn) {
                submitReviewBtn.addEventListener("click", function () {
                    if (!token) {
                        if (reviewFeedback) {
                            reviewFeedback.className = "error";
                            reviewFeedback.innerHTML = 'Please <a href="/login" style="color:inherit;text-decoration:underline;">login</a> to submit your review.';
                        }
                        return;
                    }

                    if (selectedRating < 1 || selectedRating > 5) {
                        if (reviewFeedback) {
                            reviewFeedback.className = "error";
                            reviewFeedback.textContent = "Please select a star rating (1 to 5).";
                        }
                        return;
                    }

                    submitReviewBtn.disabled = true;
                    if (reviewFeedback) {
                        reviewFeedback.className = "";
                        reviewFeedback.textContent = "Submitting review...";
                    }

                    fetch("/api/reviews", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Accept": "application/json",
                            "Authorization": "Bearer " + token
                        },
                        body: JSON.stringify({
                            bookId: Number(bookId),
                            rating: selectedRating,
                            comment: reviewComment ? reviewComment.value.trim() : ""
                        })
                    })
                    .then(async function (res) {
                        const data = await res.json().catch(function () { return {}; });
                        if (!res.ok) {
                            throw new Error(data.message || "Failed to submit review.");
                        }
                        return data;
                    })
                    .then(function () {
                        if (reviewFeedback) {
                            reviewFeedback.className = "success";
                            reviewFeedback.textContent = "Thank you! Your review has been saved.";
                        }
                        if (submitReviewBtn) {
                            submitReviewBtn.textContent = "Update Review";
                            submitReviewBtn.disabled = false;
                        }
                    })
                    .catch(function (err) {
                        if (reviewFeedback) {
                            reviewFeedback.className = "error";
                            reviewFeedback.textContent = err.message || "Could not save review.";
                        }
                        if (submitReviewBtn) {
                            submitReviewBtn.disabled = false;
                        }
                    });
                });
            }
        }

    });

})();