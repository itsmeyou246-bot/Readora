// ========================================
// MAGAZINES DATA
// Later: Get from API: GET /api/magazines
// ========================================

const magazinesData = [
    {
        id: 1,
        title: "Contemporary Design",
        issue: "Issue #47",
        date: "December 2024",
        category: "Culture",
        image: "magazine1.jpg",
        popular: true
    },
    {
        id: 2,
        title: "Tech Monthly",
        issue: "Issue #12",
        date: "December 2024",
        category: "Technology",
        image: "magazine2.jpg",
        popular: true
    },
    {
        id: 3,
        title: "Style Weekly",
        issue: "Issue #52",
        date: "December 2024",
        category: "Fashion",
        image: "magazine3.jpg",
        popular: true
    },
    {
        id: 4,
        title: "Wellness Daily",
        issue: "Issue #8",
        date: "December 2024",
        category: "Lifestyle",
        image: "magazine1.jpg",
        popular: false
    },
    {
        id: 5,
        title: "Global Travel",
        issue: "Issue #36",
        date: "December 2024",
        category: "Travel",
        image: "magazine2.jpg",
        popular: false
    },
    {
        id: 6,
        title: "Fashion Forward",
        issue: "Issue #25",
        date: "December 2024",
        category: "Fashion",
        image: "magazine3.jpg",
        popular: false
    },
    {
        id: 7,
        title: "Digital Innovation",
        issue: "Issue #20",
        date: "December 2024",
        category: "Technology",
        image: "magazine1.jpg",
        popular: false
    },
    {
        id: 8,
        title: "Culture Quarterly",
        issue: "Issue #4",
        date: "December 2024",
        category: "Culture",
        image: "magazine2.jpg",
        popular: false
    },
    {
        id: 9,
        title: "Adventure Guide",
        issue: "Issue #15",
        date: "December 2024",
        category: "Travel",
        image: "magazine3.jpg",
        popular: false
    },
    {
        id: 10,
        title: "Healthy Living",
        issue: "Issue #9",
        date: "December 2024",
        category: "Lifestyle",
        image: "magazine1.jpg",
        popular: false
    }
];

// ========================================
// INITIALIZE PAGE
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    displayMagazines();
    displayPopular();
    setupEventListeners();
    setupCategoryFilters();
});

// ========================================
// DISPLAY ALL MAGAZINES
// ========================================

function displayMagazines() {
    const grid = document.getElementById('magazinesGrid');
    const emptyMessage = document.getElementById('emptyMessage');
    const filtered = filterMagazines(magazinesData);
    
    if (filtered.length === 0) {
        grid.innerHTML = '';
        emptyMessage.style.display = 'block';
        return;
    }
    
    emptyMessage.style.display = 'none';
    
    grid.innerHTML = filtered.map(magazine => `
        <div class="magazine-card" data-id="${magazine.id}">
            <img src="${magazine.image}" alt="${magazine.title}" class="magazine-cover">
            <h3 class="magazine-title">${magazine.title}</h3>
            <p class="magazine-meta">${magazine.issue}</p>
            <p class="magazine-date">${magazine.date}</p>
        </div>
    `).join('');
    
    attachMagazineClickHandlers();
}

// ========================================
// DISPLAY POPULAR MAGAZINES
// ========================================

function displayPopular() {
    const grid = document.getElementById('popularGrid');
    const popular = magazinesData.filter(m => m.popular);
    
    grid.innerHTML = popular.map(magazine => `
        <div class="popular-magazine" data-id="${magazine.id}">
            <img src="${magazine.image}" alt="${magazine.title}" class="popular-image">
            <p class="popular-name">${magazine.title}</p>
        </div>
    `).join('');
    
    attachPopularClickHandlers();
}

// ========================================
// FILTER MAGAZINES
// ========================================

function filterMagazines(magazines) {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const category = document.getElementById('filterCategory').value;
    const sortBy = document.getElementById('sortBy').value;
    
    let filtered = magazines.filter(magazine => {
        // Filter by search term
        const matchesSearch = magazine.title.toLowerCase().includes(searchTerm) ||
                             magazine.category.toLowerCase().includes(searchTerm);
        
        // Filter by category
        const matchesCategory = !category || magazine.category === category;
        
        return matchesSearch && matchesCategory;
    });
    
    // Sort
    if (sortBy === 'popular') {
        filtered.sort((a, b) => b.popular - a.popular);
    }
    
    return filtered;
}

// ========================================
// SETUP EVENT LISTENERS
// ========================================

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    const filterCategory = document.getElementById('filterCategory');
    const sortBy = document.getElementById('sortBy');
    
    searchInput.addEventListener('input', displayMagazines);
    filterCategory.addEventListener('change', displayMagazines);
    sortBy.addEventListener('change', displayMagazines);
}

// ========================================
// SETUP CATEGORY FILTERS
// ========================================

function setupCategoryFilters() {
    const categoryCards = document.querySelectorAll('.category-card');
    
    categoryCards.forEach(card => {
        card.addEventListener('click', function() {
            const category = this.getAttribute('data-category');
            document.getElementById('filterCategory').value = category;
            displayMagazines();
            
            // Scroll to magazines grid
            document.getElementById('magazinesGrid').scrollIntoView({ behavior: 'smooth' });
        });
    });
}

// ========================================
// MAGAZINE CARD CLICK HANDLERS
// ========================================

function attachMagazineClickHandlers() {
    const cards = document.querySelectorAll('.magazine-card');
    
    cards.forEach(card => {
        card.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const magazine = magazinesData.find(m => m.id == id);
            
            if (magazine) {
                sessionStorage.setItem('selectedMagazine', JSON.stringify(magazine));
                window.location.href = 'magazine-details.html';
            }
        });
    });
}

// ========================================
// POPULAR MAGAZINE CLICK HANDLERS
// ========================================

function attachPopularClickHandlers() {
    const cards = document.querySelectorAll('.popular-magazine');
    
    cards.forEach(card => {
        card.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const magazine = magazinesData.find(m => m.id == id);
            
            if (magazine) {
                sessionStorage.setItem('selectedMagazine', JSON.stringify(magazine));
                window.location.href = 'magazine-details.html';
            }
        });
    });
}

// ========================================
// FEATURED MAGAZINE CLICK
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    const featuredCover = document.querySelector('.featured-cover');
    const btnRead = document.querySelector('.btn-read');
    
    const handleFeaturedClick = function() {
        const magazine = magazinesData[0];
        sessionStorage.setItem('selectedMagazine', JSON.stringify(magazine));
        window.location.href = 'magazine-details.html';
    };
    
    if (featuredCover) {
        featuredCover.addEventListener('click', handleFeaturedClick);
    }
    
    if (btnRead) {
        btnRead.addEventListener('click', handleFeaturedClick);
    }
});
