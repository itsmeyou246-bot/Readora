document.addEventListener('DOMContentLoaded', renderCart);

function formatPrice(amount) {
    return `Rs. ${Number(amount).toLocaleString('en-IN')}`;
}

function renderCart() {
    const content = document.getElementById('cartContent');
    const cart = getCart();
    updateCartCount();

    if (!cart.length) {
        content.innerHTML = '<section class="empty-cart"><h2>Your cart is empty.</h2><p>Discover something worth reading.</p><a class="button" href="Book.html">Explore Books</a></section>';
        return;
    }

    const subtotal = calculateSubtotal(cart);
    content.innerHTML = `<section class="cart-items">${cart.map(book => `<article class="cart-item"><img src="${book.image}" alt="${book.title}"><div><h2>${book.title}</h2><p>${book.author}</p><p class="category">${book.category}</p><button class="remove-button" type="button" data-remove-id="${book.id}">Remove</button></div><strong class="cart-price">${formatPrice(book.price)}</strong></article>`).join('')}</section><aside class="summary"><h2>Order Summary</h2><div class="summary-row"><span>Subtotal</span><span>${formatPrice(subtotal)}</span></div><div class="summary-row"><span>Discount</span><span>Rs. 0</span></div><div class="summary-row summary-total"><span>Total</span><span>${formatPrice(subtotal)}</span></div><a class="button" href="checkout.html">Proceed to Checkout</a><a class="button secondary" href="Book.html">Continue Shopping</a></aside>`;

    content.querySelectorAll('[data-remove-id]').forEach(button => {
        button.addEventListener('click', () => {
            removeFromCart(button.dataset.removeId);
            showCartToast('Book removed from your cart.');
            renderCart();
        });
    });
}
