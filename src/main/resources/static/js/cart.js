document.addEventListener('DOMContentLoaded', renderCart);

function formatPrice(amount) {
    return `Rs. ${Number(amount).toLocaleString('en-IN')}`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function renderCart() {
    const content = document.getElementById('cartContent');
    const cart = getCart();
    updateCartCount();

    if (!cart.length) {
        content.innerHTML = '<section class="empty-cart"><h2>Your cart is empty.</h2><p>Discover something worth reading.</p><a class="button" href="/Book">Explore Books</a></section>';
        return;
    }

    const subtotal = calculateSubtotal(cart);
    content.innerHTML = `<section class="cart-items">${cart.map(book => `<article class="cart-item"><img src="${escapeHtml(book.image)}" alt="${escapeHtml(book.title)}"><div><h2>${escapeHtml(book.title)}</h2><p>${escapeHtml(book.author)}</p><p class="category">${escapeHtml(book.category)}</p><button class="remove-button" type="button" data-remove-id="${escapeHtml(book.id)}">Remove</button></div><strong class="cart-price">${formatPrice(book.price)}</strong></article>`).join('')}</section><aside class="summary"><h2>Order Summary</h2><div class="summary-row"><span>Subtotal</span><span>${formatPrice(subtotal)}</span></div><div class="summary-row"><span>Discount</span><span>Rs. 0</span></div><div class="summary-row summary-total"><span>Total</span><span>${formatPrice(subtotal)}</span></div><a class="button" href="/checkout">Proceed to Checkout</a><a class="button secondary" href="/Book">Continue Shopping</a></aside>`;

    content.querySelectorAll('[data-remove-id]').forEach(button => {
        button.addEventListener('click', () => {
            removeFromCart(button.dataset.removeId);
            showCartToast('Book removed from your cart.');
            renderCart();
        });
    });
}