(function () {
    const bookId = document.body.dataset.bookId;
    const user = JSON.parse(localStorage.getItem('readoraUser') || 'null');
    const token = user && user.token;
    const headers = token ? { Authorization: 'Bearer ' + token } : {};
    const status = document.getElementById('readerStatus');
    const content = document.getElementById('pageContent');
    const pageText = document.getElementById('pageText');
    const image = document.getElementById('editorialImage');
    const pageNumber = document.getElementById('pageNumber');
    const pageCount = document.getElementById('pageCount');
    let currentPage = 1;

    if (!token) {
        redirectToLibrary('Please log in before opening a book.');
        return;
    }

    fetch('/api/reader/' + encodeURIComponent(bookId) + '/access', { headers: headers })
        .then(async response => {
            if (response.status === 403) {
                redirectToLibrary('This book must be purchased first.');
                return null;
            }
            if (!response.ok) throw new Error('Unable to verify reader access.');
            return response.json();
        })
        .then(access => {
            if (!access) return;
            document.getElementById('bookTitle').textContent = access.title;
            pageCount.textContent = access.pageCount;
            loadPage(access.lastReadPage || 1);
        })
        .catch(error => {
            console.error(error);
            status.textContent = 'The reader could not be opened.';
        });

    function loadPage(page) {
        fetch('/api/reader/' + encodeURIComponent(bookId) + '/pages/' + page, { headers: headers })
            .then(async response => {
                if (response.status === 403) {
                    redirectToLibrary('This book must be purchased first.');
                    return null;
                }
                if (!response.ok) throw new Error('Unable to load this page.');
                return response.json();
            })
            .then(renderPage)
            .catch(error => {
                console.error(error);
                status.textContent = 'This page could not be loaded.';
            });
    }

    function renderPage(data) {
        if (!data) return;
        currentPage = data.pageNumber;
        pageNumber.textContent = data.pageNumber;
        pageCount.textContent = data.pageCount;
        document.getElementById('chapterLabel').textContent = data.chapterLabel;
        pageText.textContent = data.textContent;

        const hasImage = Boolean(data.imageUrl);
        image.hidden = !hasImage;
        if (hasImage) {
            document.getElementById('imageUrl').src = data.imageUrl;
            document.getElementById('imageUrl').alt = data.imageTitle || 'Editorial image';
            document.getElementById('imageTitle').textContent = data.imageTitle || '';
            document.getElementById('imageSubtitle').textContent = data.imageSubtitle || '';
            document.getElementById('imageCaption').textContent = data.imageCaption || '';
            document.getElementById('imagePhotoCredit').textContent = data.imagePhotoCredit ? 'Photo: ' + data.imagePhotoCredit : '';
        }

        document.getElementById('previousButton').disabled = !data.hasPrevious;
        document.getElementById('nextButton').disabled = !data.hasNext;
        status.hidden = true;
        content.hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    document.getElementById('previousButton').onclick = function () {
        if (currentPage > 1) loadPage(currentPage - 1);
    };
    document.getElementById('nextButton').onclick = function () {
        loadPage(currentPage + 1);
    };
    document.getElementById('bookmarkButton').onclick = function (event) {
        const button = event.currentTarget;
        button.classList.toggle('saved');
        button.innerHTML = button.classList.contains('saved')
            ? '<i class="fa-solid fa-bookmark"></i>'
            : '<i class="fa-regular fa-bookmark"></i>';
    };
    document.getElementById('menuButton').onclick = function () {
        document.body.classList.toggle('reader-menu-open');
    };

    function redirectToLibrary(message) {
        window.alert(message);
        window.location.href = '/library.html';
    }
})();
