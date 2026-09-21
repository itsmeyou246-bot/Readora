document.addEventListener(
    "DOMContentLoaded",
    async function () {
        "use strict";

        console.log(
            "READORA: Book Details page loaded."
        );

        const params = new URLSearchParams(
            window.location.search
        );

        const bookId = params.get("id");

        if (!bookId) {
            console.error(
                "READORA: Book ID was not found in the URL."
            );

            showError(
                "Book ID was not found."
            );

            return;
        }

        const titleElement =
            document.getElementById("bookTitle");

        const authorElement =
            document.getElementById("bookAuthor");

        const descriptionElement =
            document.getElementById("bookDescription");

        const categoryElement =
            document.getElementById("bookCategory");

        const languageElement =
            document.getElementById("bookLanguage");

        const priceElement =
            document.getElementById("bookPrice");

        const bookCover =
            document.getElementById("bookCover");

        /*
         * Support both possible button IDs.
         */
        const getReadingButton =
            document.getElementById("getReadingBtn") ||
            document.getElementById("getReadingButton") ||
            document.querySelector(".get-reading-btn");

        const addToCartButton =
            document.getElementById("addToCartBtn") ||
            document.getElementById("addToCartButton") ||
            document.querySelector(".add-to-cart-btn");

        const bookStatusElement =
            document.getElementById("bookStatus");

        try {
            console.log(
                "READORA: Loading book:",
                bookId
            );

            const response = await fetch(
                "/api/books/" +
                encodeURIComponent(bookId),
                {
                    method: "GET",
                    headers: {
                        Accept: "application/json"
                    },
                    cache: "no-cache"
                }
            );

            if (!response.ok) {
                throw new Error(
                    "Book API returned status " +
                    response.status
                );
            }

            const book = await response.json();

            console.log(
                "READORA: Book loaded:",
                book
            );

            const premium =
                book.premium === true ||
                String(book.premium).toLowerCase() === "true";

            /*
             * Display book information.
             */
            if (titleElement) {
                titleElement.textContent =
                    book.title || "Untitled Book";
            }

            if (authorElement) {
                authorElement.textContent =
                    book.author || "Unknown Author";
            }

            if (descriptionElement) {
                descriptionElement.textContent =
                    book.description ||
                    "No description available.";
            }

            if (categoryElement) {
                categoryElement.textContent =
                    book.category || "Book";
            }

            if (languageElement) {
                languageElement.textContent =
                    book.language || "English";
            }

            if (priceElement) {
                if (premium) {
                    priceElement.textContent =
                        "Rs. " +
                        Number(book.price || 0).toFixed(2);
                } else {
                    priceElement.textContent = "FREE";
                }
            }

            if (bookStatusElement) {
                bookStatusElement.textContent = premium
                    ? "Premium book"
                    : "Free book";
            }

            const ratingContainer = document.querySelector(".rating");
            if (ratingContainer) {
                const avgRating = Math.round(Number(book.averageRating || book.rating || 5));
                const stars = ratingContainer.querySelectorAll("i");
                stars.forEach(function (star, idx) {
                    if (idx < avgRating) {
                        star.className = "fa-solid fa-star";
                    } else {
                        star.className = "fa-regular fa-star";
                    }
                });
            }

            /*
             * Display book cover.
             */
            if (bookCover && book.image) {
                let image = String(book.image)
                    .replace(/\\/g, "/");

                image = image.replace(
                    /^\/?static\//i,
                    ""
                );

                if (image.startsWith("/images/")) {
                    image =
                        "/img/" +
                        image.substring(
                            "/images/".length
                        );
                }

                if (!image.startsWith("/")) {
                    image = "/" + image;
                }

                bookCover.src = encodeURI(image);
                bookCover.alt =
                    book.title || "Book cover";
            }

            /*
             * Configure Add to Cart button.
             */
            if (addToCartButton) {
                if (premium) {
                    addToCartButton.hidden = false;
                    addToCartButton.disabled = false;
                    addToCartButton.textContent =
                        "Add to Cart";

                    addToCartButton.onclick =
                        function (event) {
                            event.preventDefault();
                            event.stopPropagation();

                            if (
                                typeof window.addToCart !==
                                "function"
                            ) {
                                console.error(
                                    "READORA: addToCart is not loaded."
                                );

                                alert(
                                    "Cart system is not loaded. " +
                                    "Please refresh the page."
                                );

                                return;
                            }

                            const added =
                                window.addToCart(book);

                            if (added) {
                                addToCartButton.textContent =
                                    "Added to Cart";
                            }
                        };
                } else {
                    addToCartButton.hidden = true;
                }
            }

            /*
             * Configure reading button.
             */
            if (getReadingButton) {
                getReadingButton.disabled = false;

                if (premium) {
                    getReadingButton.textContent =
                        "Read After Purchase";
                } else {
                    getReadingButton.textContent =
                        "Get Reading";
                }

                getReadingButton.onclick =
                    async function (event) {
                        event.preventDefault();
                        event.stopPropagation();

                        /*
                         * FREE BOOK:
                         * Open the reader directly.
                         */
                        if (!premium) {
                            window.location.href =
                                "/book-reader?bookId=" +
                                encodeURIComponent(book.id);

                            return;
                        }

                        /*
                         * PREMIUM BOOK:
                         * Check whether the user owns it.
                         */
                        const storedUser =
                            localStorage.getItem(
                                "readoraUser"
                            );

                        let user = null;

                        try {
                            user = storedUser
                                ? JSON.parse(storedUser)
                                : null;
                        } catch (error) {
                            user = null;
                        }

                        if (!user || !user.token) {
                            alert(
                                "Please log in and purchase this " +
                                "book before reading it."
                            );

                            window.location.href =
                                "/login";

                            return;
                        }

                        getReadingButton.disabled = true;
                        getReadingButton.textContent =
                            "Checking Access...";

                        try {
                            const accessResponse =
                                await fetch(
                                    "/api/reader/" +
                                    encodeURIComponent(book.id) +
                                    "/access",
                                    {
                                        method: "GET",
                                        headers: {
                                            Accept:
                                                "application/json",
                                            Authorization:
                                                "Bearer " +
                                                user.token
                                        },
                                        cache: "no-cache"
                                    }
                                );

                            if (accessResponse.ok) {
                                window.location.href =
                                    "/book-reader?bookId=" +
                                    encodeURIComponent(book.id);

                                return;
                            }

                            if (
                                accessResponse.status ===
                                401
                            ) {
                                alert(
                                    "Please log in before reading " +
                                    "this premium book."
                                );

                                window.location.href =
                                    "/login";

                                return;
                            }

                            if (
                                accessResponse.status ===
                                403
                            ) {
                                const shouldOpenCart =
                                    confirm(
                                        "You must purchase this book " +
                                        "before reading it. " +
                                        "Open your cart now?"
                                    );

                                if (shouldOpenCart) {
                                    window.location.href =
                                        "/cart";
                                }

                                return;
                            }

                            throw new Error(
                                "Unable to verify reading access."
                            );
                        } catch (error) {
                            console.error(
                                "READORA: Reading access error:",
                                error
                            );

                            alert(
                                "Unable to check reading access. " +
                                "Please try again."
                            );
                        } finally {
                            getReadingButton.disabled = false;

                            getReadingButton.textContent =
                                "Read After Purchase";
                        }
                    };
            }
        } catch (error) {
            console.error(
                "READORA: Book details error:",
                error
            );

            showError(
                "Unable to load this book."
            );
        }
    }
);

function showError(message) {
    const titleElement =
        document.getElementById("bookTitle");

    if (titleElement) {
        titleElement.textContent = message;
    }

    const descriptionElement =
        document.getElementById("bookDescription");

    if (descriptionElement) {
        descriptionElement.textContent =
            "Please return to the books page and try again.";
    }
}