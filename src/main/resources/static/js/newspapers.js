

const newspapersData = [
    {
        id: 1,
        title: "The Daily Times",
        edition: "Morning Edition",
        date: "December 27, 2024",
        language: "English",
        image: "img/TheDailyTimes.png",
        popular: true
    },
    {
        id: 2,
        title: "The Global Herald",
        edition: "Evening Edition",
        date: "December 27, 2024",
        language: "English",
        image: "img/The global herald.png",
        popular: true
    },
    {
        id: 3,
        title: "Kantipur Times",
        edition: "आज को संस्करण",
        date: "Dec 27, 2024",
        language: "Nepali",
        image: "img/kantipur.png",
        popular: false
    },
    {
        id: 4,
        title: "The Morning Post",
        edition: "Daily",
        date: "December 27, 2024",
        language: "English",
        image: "img/news.png",
        popular: false
    },
    {
        id: 5,
        title: "नेपाल समाचार",
        edition: "दैनिक",
        date: "डिसेम्बर २७, २०२४",
        language: "Nepali",
        image: "img/nepal.png",
        popular: false
    },
    {
        id: 6,
        title: "The World News",
        edition: "International",
        date: "December 27, 2024",
        language: "English",
        image: "img/Aljazeera.png",
        popular: true
    },
    {
        id: 7,
        title: "Business Daily",
        edition: "Finance",
        date: "December 27, 2024",
        language: "English",
        image: "img/Reuters.png",
        popular: false
    },
    {
        id: 8,
        title: "भोलि खबर",
        edition: "साप्ताहिक",
        date: "डिसेम्बर २७, २०२४",
        language: "Nepali",
        image: "img/himalayan.png",
        popular: false
    },
    {
        id: 9,
        title: "Tech Weekly",
        edition: "Technology",
        date: "December 27, 2024",
        language: "English",
        image: "img/wired.png",
        popular: false
    }
];

// ========================================
// INITIALIZE PAGE
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    displayPublications();
    displayPopular();
    setupEventListeners();
});

// ========================================
// DISPLAY LATEST PUBLICATIONS
// ========================================

function displayPublications() {
    const grid = document.getElementById('publicationsGrid');
    const emptyMessage = document.getElementById('emptyMessage');
    const allNewspapers = filterNewspapers(newspapersData);
    
    if (allNewspapers.length === 0) {
        grid.innerHTML = '';
        emptyMessage.style.display = 'block';
        return;
    }
    
    emptyMessage.style.display = 'none';
    
    grid.innerHTML = allNewspapers.map(newspaper => `
        <div class="publication-item" data-id="${newspaper.id}">
            <img src="${newspaper.image}" alt="${newspaper.title}" class="publication-image">
            <div class="publication-details">
                <h3 class="publication-title">${newspaper.title}</h3>
                <p class="publication-meta">${newspaper.edition}</p>
                <p class="publication-meta">${newspaper.date}</p>
                <span class="publication-lang">${newspaper.language}</span>
                <button class="read-issue-btn">Read Issue</button>
            </div>
        </div>
    `).join('');
    
    attachEventListeners();
}

// ========================================
// DISPLAY POPULAR PUBLICATIONS
// ========================================

function displayPopular() {
    const grid = document.getElementById('popularGrid');
    const popular = newspapersData.filter(n => n.popular);
    
    grid.innerHTML = popular.map(newspaper => `
        <div class="popular-item" data-id="${newspaper.id}">
            <img src="${newspaper.image}" alt="${newspaper.title}" class="popular-image">
            <p class="popular-name">${newspaper.title}</p>
            <p class="popular-date">${newspaper.date}</p>
        </div>
    `).join('');
}

// ========================================
// FILTER NEWSPAPERS
// ========================================

function filterNewspapers(newspapers) {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const language = document.getElementById('filterLanguage').value;
    const sortBy = document.getElementById('sortBy').value;
    
    let filtered = newspapers.filter(newspaper => {
        // Filter by search term
        const matchesSearch = newspaper.title.toLowerCase().includes(searchTerm) ||
                             newspaper.edition.toLowerCase().includes(searchTerm);
        
        // Filter by language
        const matchesLanguage = !language || newspaper.language === language;
        
        return matchesSearch && matchesLanguage;
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
    const filterLanguage = document.getElementById('filterLanguage');
    const sortBy = document.getElementById('sortBy');
    
    searchInput.addEventListener('input', displayPublications);
    filterLanguage.addEventListener('change', displayPublications);
    sortBy.addEventListener('change', displayPublications);
}

// ========================================
// PUBLICATION ITEM CLICK HANDLER
// ========================================

function attachEventListeners() {
    const items = document.querySelectorAll('.publication-item');
    
    items.forEach(item => {
        item.addEventListener('click', function(e) {
            const id = this.getAttribute('data-id');
            const newspaper = newspapersData.find(n => n.id == id);
            
            if (newspaper) {
                sessionStorage.setItem('selectedNewspaper', JSON.stringify(newspaper));
                window.location.href = 'newspaper-details.html';
            }
        });
    });
    
    // Read issue buttons
    const readBtns = document.querySelectorAll('.read-issue-btn');
    readBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            this.closest('.publication-item').click();
        });
    });
}

// ========================================
// POPULAR ITEM CLICK
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    const popularItems = document.querySelectorAll('.popular-item');
    
    popularItems.forEach(item => {
        item.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const newspaper = newspapersData.find(n => n.id == id);
            
            if (newspaper) {
                sessionStorage.setItem('selectedNewspaper', JSON.stringify(newspaper));
                window.location.href = 'newspaper-details.html';
            }
        });
    });
});

// ========================================
// FEATURED PUBLICATION CLICK
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    const featuredPub = document.getElementById('featuredPub');
    
    if (featuredPub) {
        featuredPub.addEventListener('click', function() {
            const newspaper = newspapersData[0]; // First newspaper as featured
            sessionStorage.setItem('selectedNewspaper', JSON.stringify(newspaper));
            window.location.href = 'newspaper-details.html';
        });
    }
    
    const readBtn = document.querySelector('.featured-content .read-btn');
    if (readBtn) {
        readBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            featuredPub.click();
        });
    }
});
