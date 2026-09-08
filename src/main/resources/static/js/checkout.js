document.addEventListener('DOMContentLoaded', setupCheckout);

function formatPrice(amount) {
    return `Rs. ${Number(amount).toLocaleString('en-IN')}`;
}

function setupCheckout() {
    const cart = getCart();
    updateCartCount();
    const summary = document.getElementById('orderSummary');
    const submitButton = document.querySelector('#checkoutForm button[type="submit"]');
    const payLabel = submitButton.querySelector('[data-pay-label]');

    if (!cart.length) {
        summary.innerHTML = '<div class="empty-summary"><h2>Your cart is empty.</h2><a class="button" href="Book.html">Explore Books</a></div>';
        submitButton.disabled = true;
        return;
    }

    const subtotal = calculateSubtotal(cart);
    const [heroBook, ...restBooks] = cart;

    const heroHtml = `
        <div class="product-hero">
            <img src="${heroBook.image}" alt="${heroBook.title}">
            <h3>${heroBook.title}</h3>
            <p>By ${heroBook.author}</p>
            <strong>${formatPrice(heroBook.price)}</strong>
        </div>
    `;

    const restItemsHtml = restBooks.map(book => `<div class="summary-item"><span>${book.title}<small>${book.author}</small></span><strong>${formatPrice(book.price)}</strong></div>`).join('');

    const summaryCardHtml = `
        <div class="summary-card">
            <h4>Order Summary</h4>
            ${restItemsHtml}
            <div class="summary-row"><span>Subtotal</span><span>${formatPrice(subtotal)}</span></div>
            <div class="summary-row"><span>Discount</span><span>Rs. 0</span></div>
            <div class="summary-row summary-total"><span>Total</span><span>${formatPrice(subtotal)}</span></div>
        </div>
    `;

    summary.innerHTML = heroHtml + summaryCardHtml;

    if (payLabel) {
        payLabel.textContent = `Pay ${formatPrice(subtotal)}`;
    }

    document.getElementById('checkoutForm').addEventListener('submit', event => {
        event.preventDefault();
        completePurchase(cart);
    });
}

async function completePurchase(cart) {
    const name = document.getElementById('fullName').value.trim();
    const email = document.getElementById('email').value.trim();
    const payment = document.querySelector('input[name="paymentMethod"]:checked');
    const error = document.getElementById('checkoutError');

    if (!name || !email || !email.includes('@') || !payment) {
        error.textContent = 'Please enter your name, a valid email, and select a payment method.';
        return;
    }

    const loggedInUser = JSON.parse(localStorage.getItem('readoraUser') || 'null');
    if (!loggedInUser || !loggedInUser.token) {
        error.textContent = 'Please log in before completing your purchase.';
        return;
    }

    const ownershipResponse = await fetch('/api/purchases', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${loggedInUser.token}`
        },
        body: JSON.stringify({ books: cart })
    });
    if (!ownershipResponse.ok) {
        const result = await ownershipResponse.json().catch(() => ({}));
        error.textContent = result.message || 'The purchase could not be recorded.';
        return;
    }

    const purchases = JSON.parse(localStorage.getItem('readoraPurchases') || '[]');
    const order = {
        orderId: `RD-2026-${String(purchases.length + 1).padStart(4, '0')}`,
        books: cart,
        amount: calculateSubtotal(cart),
        customerName: name,
        customerEmail: email,
        paymentMethod: payment.value,
        purchasedAt: new Date().toISOString(),
        status: 'Purchased'
    };
    purchases.push(order);
    localStorage.setItem('readoraPurchases', JSON.stringify(purchases));

    const library = JSON.parse(localStorage.getItem('readoraLibrary') || '[]');
    cart.forEach(book => {
        if (!library.some(item => String(item.id) === String(book.id))) {
            library.push({ ...book, progress: 0, status: 'purchased', purchasedAt: order.purchasedAt });
        }
    });
    localStorage.setItem('readoraLibrary', JSON.stringify(library));
    localStorage.setItem('readoraLastPurchase', JSON.stringify(order));
    saveCart([]);
    window.location.href = 'purchase-success.html';
}