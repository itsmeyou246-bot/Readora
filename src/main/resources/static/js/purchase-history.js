document.addEventListener('DOMContentLoaded', renderPurchaseHistory);

function renderPurchaseHistory() {
    const target = document.getElementById('historyList');
    const purchases = JSON.parse(localStorage.getItem('readoraPurchases') || '[]').slice().reverse();
    if (!purchases.length) {
        target.innerHTML = '<p class="empty-history">You have not purchased any books yet.</p>';
        return;
    }
    target.innerHTML = purchases.map(order => {
        const date = new Date(order.purchasedAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
        return `<article class="purchase"><div class="purchase-heading"><h2>Order #${order.orderId}</h2><span class="purchase-date">${date}</span></div><div class="purchase-books">${order.books.map(book => `<div class="purchase-book"><img src="${book.image}" alt="${book.title}"><div><h3>${book.title}</h3><p>${book.author}</p></div></div>`).join('')}</div><div class="purchase-footer"><span>Rs. ${Number(order.amount).toLocaleString('en-IN')} · <span class="purchase-status">${order.status}</span></span><a class="read-button" href="library.html">Read Book</a></div></article>`;
    }).join('');
}
