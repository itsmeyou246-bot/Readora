(async function () {
    const bookId = document.body.dataset.bookId;
    const user = JSON.parse(localStorage.getItem('readoraUser') || 'null');
    const token = user && user.token;
    const headers = token ? { Authorization: 'Bearer ' + token } : {};
    const loading = document.getElementById('loadingState');
    const locked = document.getElementById('lockedState');
    const stage = document.getElementById('bookStage');
    const content = document.getElementById('bookContent');
    const canvas = document.getElementById('pdfCanvas');
    const pageNumber = document.getElementById('pageNumber');
    const pageCount = document.getElementById('pageCount');
    const progressText = document.getElementById('progressText');
    let pdf;
    let page = 1;
    let scale = 1.25;
    let contentPages = Array.from(content.querySelectorAll('.content-page'));
    let contentPage = 1;

    if (!token) {
        loading.hidden = true;
        locked.hidden = false;
        locked.querySelector('p').textContent = 'Please log in to read purchased books.';
        locked.querySelector('a').href = '/login';
        return;
    }

    try {
        const detailsResponse = await fetch('/api/reader/' + bookId, { headers: headers });
        if (!detailsResponse.ok) {
            loading.hidden = true;
            locked.hidden = false;
            return;
        }
        const details = await detailsResponse.json();
        page = details.currentPage || 1;

        const pdfResponse = await fetch('/api/reader/' + bookId + '/file', { headers: headers });
        if (!pdfResponse.ok) {
            if (pdfResponse.status === 404) {
                showContentReader(page);
            } else {
                loading.hidden = true;
                locked.hidden = false;
                locked.querySelector('p').textContent = 'This book is not unlocked for your account.';
            }
            return;
        }
        const pdfData = await pdfResponse.arrayBuffer();
        const pdfjs = await import('https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.5.136/pdf.min.mjs');
        pdfjs.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/4.5.136/pdf.worker.min.mjs';
        pdf = await pdfjs.getDocument({ data: pdfData }).promise;
        pageCount.textContent = pdf.numPages;
        stage.hidden = false;
        loading.hidden = true;
        await renderPage(page);
    } catch (error) {
        console.error('Reader loading error:', error);
        loading.textContent = 'The reader could not load this book.';
    }

    function showContentReader(startPage) {
        loading.hidden = true;
        content.hidden = false;
        pageCount.textContent = contentPages.length + 1;
        renderContentPage(startPage || 1);
    }

    function renderContentPage(number) {
        contentPage = Math.max(1, Math.min(number, contentPages.length + 1));
        contentPages.forEach(pageElement => {
            pageElement.hidden = Number(pageElement.dataset.page) !== contentPage;
        });
        pageNumber.value = contentPage;
        pageCount.textContent = contentPages.length + 1;
        progressText.textContent = 'Page ' + contentPage + ' of ' + (contentPages.length + 1);
        window.scrollTo({ top: 0, behavior: 'smooth' });
        saveProgress(contentPage);
    }

    function saveProgress(currentPage) {
        if (!token) return;
        fetch('/api/reader/' + bookId + '/progress', {
            method: 'POST', headers: { ...headers, 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentPage: currentPage })
        }).catch(() => {});
    }

    async function renderPage(number) {
        page = Math.max(1, Math.min(number, pdf.numPages));
        pageNumber.value = page;
        const pdfPage = await pdf.getPage(page);
        const viewport = pdfPage.getViewport({ scale: scale });
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        await pdfPage.render({ canvasContext: canvas.getContext('2d'), viewport: viewport }).promise;
        const percent = Math.round((page / pdf.numPages) * 100);
        progressText.textContent = percent + '%';
        saveProgress(page);
    }

    document.getElementById('previousPage').onclick = () => pdf ? renderPage(page - 1) : renderContentPage(contentPage - 1);
    document.getElementById('nextPage').onclick = () => pdf ? renderPage(page + 1) : renderContentPage(contentPage + 1);
    pageNumber.onchange = () => pdf ? renderPage(Number(pageNumber.value)) : renderContentPage(Number(pageNumber.value));
    document.getElementById('bookmark').onclick = event => {
        event.currentTarget.classList.toggle('is-saved');
        event.currentTarget.querySelector('i').className = event.currentTarget.classList.contains('is-saved')
            ? 'fa-solid fa-bookmark' : 'fa-regular fa-bookmark';
    };
    document.getElementById('readerMenu').onclick = () => document.body.classList.toggle('reader-settings-open');
})();
