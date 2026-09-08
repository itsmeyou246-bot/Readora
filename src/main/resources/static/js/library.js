// ========================================
// DEMO LIBRARY DATA
// Later: Get from API: GET /api/library
// ========================================

const demoLibraryBooks = {
    reading: [
        {
            id: 1,
            title: "The Silent Spring",
            author: "Rachel Carson",
            image: "img/TheSilentSpring.png",
            progress: 68,
            category: "Science & Nature"
        },
        {
            id: 2,
            title: "The Story of a New Name",
            author: "Elena Ferrante",
            image: "img/TheStoryofnewname.png",
            progress: 42,
            category: "Fiction"
        }
    ],
    saved: [
        {
            id: 3,
            title: "Atomic Habits",
            author: "James Clear",
            image: "img/AtomicHabits.png",
            progress: 0,
            category: "Non-fiction"
        },
        {
            id: 4,
            title: "Pride and Prejudice",
            author: "Jane Austen",
            image: "img/6.png",
            progress: 0,
            category: "Fiction"
        },
        {
            id: 5,
            title: "The Great Gatsby",
            author: "F. Scott Fitzgerald",
            image: "img/7.png",
            progress: 0,
            category: "Fiction"
        }
    ],
    completed: [
        {
            id: 6,
            title: "Outliers",
            author: "Malcolm Gladwell",
            image: "img/Outerliers.jpg",
            progress: 100,
            category: "Non-fiction"
        }
    ],
    purchased: [
        {
            id: 7,
            title: "Structures of Light",
            author: "Hiroshi Sugimoto",
            image: "img/StructuresOfLight.png",
            progress: 15,
            category: "Architecture"
        }
    ]
};

// ========================================
// TAB SWITCHING
// ========================================

const tabBtns = document.querySelectorAll('.tab-btn');

tabBtns.forEach(btn => {
    btn.addEventListener('click', function() {
        const tabName = this.getAttribute('data-tab');
        
        // Remove active from all buttons
        tabBtns.forEach(b => b.classList.remove('tab-active'));
        // Add active to clicked button
        this.classList.add('tab-active');
        
        // Hide all tab contents
        const allContents = document.querySelectorAll('.tab-content');
        allContents.forEach(content => content.classList.remove('tab-active'));
        
        // Show selected tab content
        document.getElementById(tabName).classList.add('tab-active');
    });
});

// ========================================
// DISPLAY LIBRARY BOOKS
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    loadLibrary();
});

function loadLibrary() {
    // Load from localStorage
    const savedBooks = JSON.parse(localStorage.getItem('myLibrary')) || [];
    
    // Display each category
    displayBooks('reading', demoLibraryBooks.reading);
    displayBooks('saved', savedBooks.length > 0 ? savedBooks : demoLibraryBooks.saved);
    displayBooks('completed', demoLibraryBooks.completed);
    displayBooks('purchased', demoLibraryBooks.purchased);
}

function displayBooks(category, books) {
    const grid = document.getElementById(category + 'Grid');
    const emptyState = document.getElementById(category + 'Empty');
    
    if (books.length === 0) {
        grid.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }
    
    emptyState.style.display = 'none';
    grid.innerHTML = books.map(book => `
        <div class="book-item" data-book-id="${book.id}">
            <img src="${book.image}" alt="${book.title}" class="book-cover">
            <h3 class="book-title">${book.title}</h3>
            <p class="book-author">${book.author}</p>
            
            ${book.progress > 0 ? `
                <div class="progress-container">
                    <div class="progress-bar-background">
                        <div class="progress-bar-fill" style="width: ${book.progress}%"></div>
                    </div>
                    <p class="progress-text">${book.progress}%</p>
                </div>
            ` : ''}
            
            <div class="action-buttons">
                <button class="continue-btn" data-book-id="${book.id}">Continue Reading</button>
                <button class="remove-btn" data-book-id="${book.id}">Remove</button>
            </div>
        </div>
    `).join('');
    
    // Add event listeners
    attachEventListeners(grid);
}

// ========================================
// CONTINUE READING
// ========================================

function attachEventListeners(grid) {
    const continueButtons = grid.querySelectorAll('.continue-btn');
    const removeButtons = grid.querySelectorAll('.remove-btn');
    
    continueButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            
            const bookId = this.getAttribute('data-book-id');
            const bookItem = this.closest('.book-item');
            const bookTitle = bookItem.querySelector('.book-title').textContent;
            const bookAuthor = bookItem.querySelector('.book-author').textContent;
            
            // Store current book
            sessionStorage.setItem('currentBook', JSON.stringify({
                title: bookTitle,
                author: bookAuthor
            }));
            
            // Navigate to reader
            window.location.href = 'reader.html';
        });
    });
    
    removeButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            
            const bookItem = this.closest('.book-item');
            const bookTitle = bookItem.querySelector('.book-title').textContent;
            
            // Get category from active tab
            const activeTab = document.querySelector('.tab-content.tab-active');
            const category = activeTab.id;
            
            // Remove from localStorage
            let books = JSON.parse(localStorage.getItem('myLibrary')) || [];
            books = books.filter(b => b.title !== bookTitle);
            localStorage.setItem('myLibrary', JSON.stringify(books));
            
            // Show toast
            showToast('Removed from library');
            
            // Reload the category
            loadLibrary();
        });
    });
    
    // Book card click - also continue reading
    const bookItems = grid.querySelectorAll('.book-item');
    bookItems.forEach(item => {
        item.addEventListener('click', function(e) {
            // Don't trigger if button was clicked
            if (e.target.classList.contains('continue-btn') || 
                e.target.classList.contains('remove-btn')) {
                return;
            }
            
            const bookTitle = this.querySelector('.book-title').textContent;
            const bookAuthor = this.querySelector('.book-author').textContent;
            
            sessionStorage.setItem('currentBook', JSON.stringify({
                title: bookTitle,
                author: bookAuthor
            }));
            
            window.location.href = 'reader.html';
        });
    });
}

// ========================================
// TOAST NOTIFICATION
// ========================================

function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.right = '20px';
    toast.style.backgroundColor = '#2f4a3a';
    toast.style.color = '#faf6ee';
    toast.style.padding = '12px 16px';
    toast.style.borderRadius = '3px';
    toast.style.fontSize = '12px';
    toast.style.zIndex = '9999';
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    document.body.appendChild(toast);
    
    setTimeout(() => toast.style.opacity = '1', 10);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}
