// ==========================================================================
// READORA — BOOKS CATALOG JAVASCRIPT
// ==========================================================================

document.addEventListener('DOMContentLoaded', () => {
    initBookPage();
});

let currentCategory = 'all';
let currentLanguage = 'all';
let currentSearch = '';
let currentSort = 'newest';

function initBookPage() {
    const searchInput = document.getElementById('searchInput');
    const categoryTabs = document.querySelectorAll('#categoryTabs .filter-tab');
    const languageFilter = document.getElementById('languageFilter');
    const sortDropdown = document.getElementById('sortDropdown');
    const clearBtn = document.getElementById('clearFiltersBtn');

    // Category Tabs
    categoryTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            categoryTabs.forEach(t => t.classList.remove('filter-active'));
            this.classList.add('filter-active');
            currentCategory = this.getAttribute('data-filter');
            renderBooks();
        });
    });

    // Search Input
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            currentSearch = e.target.value.trim().toLowerCase();
            renderBooks();
        });
    }

    // Language Filter
    if (languageFilter) {
        languageFilter.addEventListener('change', (e) => {
            currentLanguage = e.target.value;
            renderBooks();
        });
    }

    // Sort Dropdown
    if (sortDropdown) {
        sortDropdown.addEventListener('change', (e) => {
            currentSort = e.target.value;
            renderBooks();
        });
    }

    // Reset Filters
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            currentCategory = 'all';
            currentLanguage = 'all';
            currentSearch = '';
            currentSort = 'newest';
            if (searchInput) searchInput.value = '';
            if (languageFilter) languageFilter.value = 'all';
            if (sortDropdown) sortDropdown.value = 'newest';
            categoryTabs.forEach(t => t.classList.remove('filter-active'));
            if (categoryTabs[0]) categoryTabs[0].classList.add('filter-active');
            renderBooks();
        });
    }

    renderBooks();
}

function renderBooks() {
    const grid = document.getElementById('bookGrid');
    const emptyState = document.getElementById('emptyMessage');
    const countEl = document.getElementById('resultsCount');
    if (!grid) return;

    let books = [...READORA_DATA.books];

    // Filter by Category or Access
    if (currentCategory !== 'all') {
        if (currentCategory === 'free') {
            books = books.filter(b => !b.premium);
        } else if (currentCategory === 'premium') {
            books = books.filter(b => b.premium);
        } else {
            books = books.filter(b => b.category.toLowerCase() === currentCategory.toLowerCase() || (b.tag && b.tag.toLowerCase() === currentCategory.toLowerCase()));
        }
    }

    // Filter by Language
    if (currentLanguage !== 'all') {
        books = books.filter(b => b.language.toLowerCase() === currentLanguage.toLowerCase());
    }

    // Filter by Search
    if (currentSearch) {
        books = books.filter(b => 
            b.title.toLowerCase().includes(currentSearch) ||
            b.author.toLowerCase().includes(currentSearch) ||
            b.category.toLowerCase().includes(currentSearch)
        );
    }

    // Sorting
    if (currentSort === 'a-z') {
        books.sort((a, b) => a.title.localeCompare(b.title));
    } else if (currentSort === 'popular') {
        books.sort((a, b) => (b.reads || 0) - (a.reads || 0));
    } else if (currentSort === 'price-low') {
        books.sort((a, b) => (a.price || 0) - (b.price || 0));
    } else if (currentSort === 'price-high') {
        books.sort((a, b) => (b.price || 0) - (a.price || 0));
    }

    if (countEl) {
        countEl.textContent = `Showing ${books.length} book${books.length === 1 ? '' : 's'}`;
    }

    if (books.length === 0) {
        grid.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    // Get current bookmarks from localStorage to set bookmark active status
    const savedBookmarks = JSON.parse(localStorage.getItem(STORAGE_KEYS.BOOKMARKS) || '[]');

    grid.innerHTML = books.map(book => {
        const isBookmarked = savedBookmarks.some(bm => bm.bookTitle === book.title);

        let priceBadge = `<span class="free-badge">FREE</span>`;
        if (book.premium) {
            priceBadge = `<span class="premium-badge"><i class="fa-solid fa-crown"></i> Rs. ${book.price}</span>`;
        }

        return `
            <div class="book-card" data-id="${book.id}">
                <div class="book-card-image-wrap" style="position: relative;">
                    <img src="${book.image}" alt="${book.title}" loading="lazy">
                    <button class="bookmark-badge-btn ${isBookmarked ? 'bookmarked' : ''}" data-id="${book.id}" title="Save Bookmark">
                        <i class="fa-${isBookmarked ? 'solid' : 'regular'} fa-bookmark"></i>
                    </button>
                </div>
                <div style="display: flex; justify-content: space-between; align-items: baseline; margin-top: 10px;">
                    ${priceBadge}
                    <span style="font-size: 11px; color: var(--color-text-muted);"><i class="fa-solid fa-star" style="color: var(--color-gold);"></i> ${book.rating || '4.8'}</span>
                </div>
                <h3>${book.title}</h3>
                <p>${book.author}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                    <span class="book-tag">${book.category}</span>
                    <span style="font-size: 11px; color: var(--color-text-secondary); text-transform: uppercase; letter-spacing: 0.5px;">${book.language}</span>
                </div>
            </div>
        `;
    }).join('');

    // Attach card click handlers
    grid.querySelectorAll('.book-card').forEach(card => {
        card.addEventListener('click', function(e) {
            if (e.target.closest('.bookmark-badge-btn')) return;
            const id = parseInt(this.getAttribute('data-id'), 10);
            const book = READORA_DATA.books.find(b => b.id === id);
            if (book) {
                sessionStorage.setItem('selectedBook', JSON.stringify(book));
                window.location.href = 'book-details.html';
            }
        });
    });

    // Attach bookmark handlers
    grid.querySelectorAll('.bookmark-badge-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const id = parseInt(this.getAttribute('data-id'), 10);
            const book = READORA_DATA.books.find(b => b.id === id);
            if (!book) return;

            let bookmarks = JSON.parse(localStorage.getItem(STORAGE_KEYS.BOOKMARKS) || '[]');
            const existsIndex = bookmarks.findIndex(bm => bm.bookTitle === book.title);

            const icon = this.querySelector('i');
            if (existsIndex > -1) {
                bookmarks.splice(existsIndex, 1);
                this.classList.remove('bookmarked');
                icon.className = 'fa-regular fa-bookmark';
                showToast(`Removed '${book.title}' from bookmarks`, 'info');
            } else {
                bookmarks.push({
                    id: Date.now(),
                    bookTitle: book.title,
                    bookAuthor: book.author,
                    page: "Chapter 1",
                    savedDate: new Date().toISOString().split('T')[0],
                    image: book.image
                });
                this.classList.add('bookmarked');
                icon.className = 'fa-solid fa-bookmark';
                showToast(`Bookmarked '${book.title}'`, 'success');
            }
            localStorage.setItem(STORAGE_KEYS.BOOKMARKS, JSON.stringify(bookmarks));
        });
    });
}
