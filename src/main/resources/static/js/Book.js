/* ==========================================================================
   READORA — BOOKS CATALOG JAVASCRIPT
   Free books -> Reading page
   Premium books -> Premium Library page
   ========================================================================== */

document.addEventListener('DOMContentLoaded', initBookPage);

let currentCategory = 'all';
let currentLanguage = 'all';
let currentSearch = '';
let currentSort = 'newest';

function isPremiumBook(book) {
    return book && (
        book.premium === true ||
        String(book.premium).toLowerCase() === 'true'
    ) && Number(book.price) > 0;
}

function getBookById(id) {
    return READORA_DATA.books.find(
        book => String(book.id) === String(id)
    );
}

function openBook(book) {
    if (!book || !book.id) {
        showToast('Book information is missing.', 'error');
        return;
    }

    sessionStorage.setItem(
        'selectedBook',
        JSON.stringify(book)
    );

    if (isPremiumBook(book)) {
        window.location.href =
            '/book-details?id=' +
            encodeURIComponent(book.id);
    } else {
        window.location.href =
            '/book-reader?bookId=' +
            encodeURIComponent(book.id);
    }
}

function initBookPage() {
    const searchInput = document.getElementById('searchInput');
    const categoryTabs = document.querySelectorAll(
        '#categoryTabs .filter-tab'
    );
    const languageFilter = document.getElementById('languageFilter');
    const sortDropdown = document.getElementById('sortDropdown');
    const clearBtn = document.getElementById('clearFiltersBtn');

    categoryTabs.forEach(tab => {
        tab.addEventListener('click', function () {
            categoryTabs.forEach(t =>
                t.classList.remove('filter-active')
            );

            this.classList.add('filter-active');
            currentCategory = this.getAttribute('data-filter');
            renderBooks();
        });
    });

    if (searchInput) {
        searchInput.addEventListener('input', event => {
            currentSearch = event.target.value.trim().toLowerCase();
            renderBooks();
        });
    }

    if (languageFilter) {
        languageFilter.addEventListener('change', event => {
            currentLanguage = event.target.value;
            renderBooks();
        });
    }

    if (sortDropdown) {
        sortDropdown.addEventListener('change', event => {
            currentSort = event.target.value;
            renderBooks();
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            currentCategory = 'all';
            currentLanguage = 'all';
            currentSearch = '';
            currentSort = 'newest';

            if (searchInput) searchInput.value = '';
            if (languageFilter) languageFilter.value = 'all';
            if (sortDropdown) sortDropdown.value = 'newest';

            categoryTabs.forEach(t =>
                t.classList.remove('filter-active')
            );

            if (categoryTabs[0]) {
                categoryTabs[0].classList.add('filter-active');
            }

            renderBooks();
        });
    }

    renderBooks();
}

function renderBooks() {
    const grid = document.getElementById('bookGrid');
    const emptyState = document.getElementById('emptyMessage');
    const countEl = document.getElementById('resultsCount');

    if (!grid || !window.READORA_DATA) return;

    let books = [...READORA_DATA.books];

    if (currentCategory !== 'all') {
        if (currentCategory === 'free') {
            books = books.filter(book => !isPremiumBook(book));
        } else if (currentCategory === 'premium') {
            books = books.filter(book => isPremiumBook(book));
        } else {
            books = books.filter(book =>
                (book.category || '').toLowerCase() ===
                currentCategory.toLowerCase() ||
                (book.tag || '').toLowerCase() ===
                currentCategory.toLowerCase()
            );
        }
    }

    if (currentLanguage !== 'all') {
        books = books.filter(book =>
            (book.language || '').toLowerCase() ===
            currentLanguage.toLowerCase()
        );
    }

    if (currentSearch) {
        books = books.filter(book => {
            const search = currentSearch;

            return (
                (book.title || '').toLowerCase().includes(search) ||
                (book.author || '').toLowerCase().includes(search) ||
                (book.category || '').toLowerCase().includes(search)
            );
        });
    }

    if (currentSort === 'a-z') {
        books.sort((a, b) =>
            (a.title || '').localeCompare(b.title || '')
        );
    } else if (currentSort === 'popular') {
        books.sort((a, b) =>
            (b.reads || 0) - (a.reads || 0)
        );
    } else if (currentSort === 'price-low') {
        books.sort((a, b) =>
            (a.price || 0) - (b.price || 0)
        );
    } else if (currentSort === 'price-high') {
        books.sort((a, b) =>
            (b.price || 0) - (a.price || 0)
        );
    }

    if (countEl) {
        countEl.textContent =
            `Showing ${books.length} book${books.length === 1 ? '' : 's'}`;
    }

    if (!books.length) {
        grid.innerHTML = '';

        if (emptyState) {
            emptyState.style.display = 'block';
        }

        return;
    }

    if (emptyState) {
        emptyState.style.display = 'none';
    }

    let savedBookmarks = [];

    try {
        savedBookmarks = JSON.parse(
            localStorage.getItem(
                window.STORAGE_KEYS?.BOOKMARKS || 'readoraBookmarks'
            ) || '[]'
        );
    } catch (error) {
        savedBookmarks = [];
    }

    grid.innerHTML = books.map(book => {
        const isBookmarked = savedBookmarks.some(
            bookmark => bookmark.bookTitle === book.title
        );

        const premium = isPremiumBook(book);

        const priceBadge = premium
            ? `<span class="premium-badge">
                <i class="fa-solid fa-crown"></i>
                Rs. ${Number(book.price || 0).toFixed(2)}
               </span>`
            : `<span class="free-badge">FREE</span>`;

        return `
            <div class="book-card" data-id="${book.id}">
                <div class="book-card-image-wrap"
                     style="position: relative;">
                    <img src="${escapeHtml(book.image || '')}"
                         alt="${escapeHtml(book.title || 'Book')}"
                         loading="lazy">

                    <button class="bookmark-badge-btn
                        ${isBookmarked ? 'bookmarked' : ''}"
                        data-id="${book.id}"
                        title="Save Bookmark"
                        type="button">
                        <i class="fa-${isBookmarked ? 'solid' : 'regular'}
                            fa-bookmark"></i>
                    </button>
                </div>

                <div style="display:flex;
                            justify-content:space-between;
                            align-items:baseline;
                            margin-top:10px;">
                    ${priceBadge}

                    <span style="font-size:11px;
                                 color:var(--color-text-muted);">
                        <i class="fa-solid fa-star"
                           style="color:var(--color-gold);"></i>
                        ${book.rating || book.averageRating || '4.8'}
                    </span>
                </div>

                <h3>${escapeHtml(book.title || 'Untitled Book')}</h3>
                <p>${escapeHtml(book.author || 'Unknown Author')}</p>

                <div style="display:flex;
                            justify-content:space-between;
                            align-items:center;
                            margin-top:8px;">
                    <span class="book-tag">
                        ${escapeHtml(book.category || 'Book')}
                    </span>

                    <span style="font-size:11px;
                                 color:var(--color-text-secondary);
                                 text-transform:uppercase;
                                 letter-spacing:.5px;">
                        ${escapeHtml(book.language || 'English')}
                    </span>
                </div>
            </div>
        `;
    }).join('');

    grid.querySelectorAll('.book-card').forEach(card => {
        card.addEventListener('click', function (event) {
            if (event.target.closest('.bookmark-badge-btn')) return;

            const book = getBookById(this.dataset.id);

            if (book) {
                openBook(book);
            }
        });
    });

    grid.querySelectorAll('.bookmark-badge-btn').forEach(button => {
        button.addEventListener('click', function (event) {
            event.stopPropagation();

            const book = getBookById(this.dataset.id);
            if (!book) return;

            let bookmarks = [];

            try {
                bookmarks = JSON.parse(
                    localStorage.getItem(
                        window.STORAGE_KEYS?.BOOKMARKS ||
                        'readoraBookmarks'
                    ) || '[]'
                );
            } catch (error) {
                bookmarks = [];
            }

            const index = bookmarks.findIndex(
                bookmark => bookmark.bookTitle === book.title
            );

            const icon = this.querySelector('i');

            if (index > -1) {
                bookmarks.splice(index, 1);
                this.classList.remove('bookmarked');

                if (icon) {
                    icon.className = 'fa-regular fa-bookmark';
                }

                showToast(
                    `Removed '${book.title}' from bookmarks`,
                    'info'
                );
            } else {
                bookmarks.push({
                    id: Date.now(),
                    bookTitle: book.title,
                    bookAuthor: book.author,
                    page: 'Chapter 1',
                    savedDate: new Date().toISOString().split('T')[0],
                    image: book.image
                });

                this.classList.add('bookmarked');

                if (icon) {
                    icon.className = 'fa-solid fa-bookmark';
                }

                showToast(
                    `Bookmarked '${book.title}'`,
                    'success'
                );
            }

            localStorage.setItem(
                window.STORAGE_KEYS?.BOOKMARKS || 'readoraBookmarks',
                JSON.stringify(bookmarks)
            );
        });
    });
}

function escapeHtml(value) {
    return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}