document.addEventListener('DOMContentLoaded', function () {

    /*
     * Get Book ID from:
     * /reader/1
     */
    let bookId = document.body.getAttribute('data-book-id');

    if (!bookId) {

        const parts =
            window.location.pathname.split('/');

        bookId =
            parts[parts.length - 1];
    }

    console.log('Reader Book ID:', bookId);


    /*
     * Reader elements
     */
    const loadingState =
        document.getElementById('loadingState');

    const lockedState =
        document.getElementById('lockedState');

    const bookContent =
        document.getElementById('bookContent');

    const pageNumber =
        document.getElementById('pageNumber');

    const pageCount =
        document.getElementById('pageCount');

    const progressText =
        document.getElementById('progressText');

    const previousPage =
        document.getElementById('previousPage');

    const nextPage =
        document.getElementById('nextPage');

    const pages =
        document.querySelectorAll('.content-page');


    /*
     * Your reader.html currently contains
     * 4 content pages.
     */
    const totalPages = pages.length;


    let currentPage = 1;


    /*
     * If there are no pages, stop.
     */
    if (totalPages === 0) {

        if (loadingState) {
            loadingState.hidden = true;
        }

        if (lockedState) {
            lockedState.hidden = false;
        }

        return;
    }


    /*
     * Set total page count.
     */
    if (pageCount) {
        pageCount.textContent =
            totalPages;
    }


    /*
     * IMPORTANT:
     *
     * First hide loading and show
     * the existing book content.
     *
     * This allows FREE books to
     * open directly.
     */
    if (loadingState) {
        loadingState.hidden = true;
    }

    if (lockedState) {
        lockedState.hidden = true;
    }

    if (bookContent) {
        bookContent.hidden = false;
    }


    /*
     * Display first page.
     */
    showPage(1);


    /*
     * NEXT PAGE
     */
    if (nextPage) {

        nextPage.addEventListener(
            'click',
            function () {

                if (currentPage < totalPages) {

                    showPage(
                        currentPage + 1
                    );
                }
            }
        );
    }


    /*
     * PREVIOUS PAGE
     */
    if (previousPage) {

        previousPage.addEventListener(
            'click',
            function () {

                if (currentPage > 1) {

                    showPage(
                        currentPage - 1
                    );
                }
            }
        );
    }


    /*
     * PAGE NUMBER
     */
    if (pageNumber) {

        pageNumber.addEventListener(
            'change',
            function () {

                let page =
                    Number(this.value);

                if (
                    page >= 1 &&
                    page <= totalPages
                ) {

                    showPage(page);

                } else {

                    this.value =
                        currentPage;
                }
            }
        );
    }


    /*
     * SHOW PAGE
     */
    function showPage(page) {

        currentPage = page;


        pages.forEach(
            function (contentPage) {

                const pageValue =
                    Number(
                        contentPage.getAttribute(
                            'data-page'
                        )
                    );

                if (pageValue === currentPage) {

                    contentPage.style.display =
                        'block';

                } else {

                    contentPage.style.display =
                        'none';
                }
            }
        );


        /*
         * Update page number.
         */
        if (pageNumber) {

            pageNumber.value =
                currentPage;
        }


        /*
         * Update progress text.
         */
        if (progressText) {

            progressText.textContent =
                'Page ' +
                currentPage +
                ' of ' +
                totalPages;
        }


        /*
         * Disable previous button
         * on first page.
         */
        if (previousPage) {

            previousPage.disabled =
                currentPage === 1;
        }


        /*
         * Disable next button
         * on last page.
         */
        if (nextPage) {

            nextPage.disabled =
                currentPage === totalPages;
        }
    }

});