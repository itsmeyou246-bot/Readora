(() => {
	"use strict";

	const CART_KEY = "readoraCart";

	function getCart() {
		try {
			const savedCart = localStorage.getItem(CART_KEY);
			const cart = savedCart ? JSON.parse(savedCart) : [];

			return Array.isArray(cart) ? cart : [];
		} catch (error) {
			console.error("READORA: Unable to read cart.", error);
			return [];
		}
	}

	function saveCart(cart) {
		const safeCart = Array.isArray(cart) ? cart : [];

		localStorage.setItem(
			CART_KEY,
			JSON.stringify(safeCart)
		);

		updateCartCount();
	}

	function updateCartCount() {
		const count = getCart().length;

		document
			.querySelectorAll("[data-cart-count]")
			.forEach(element => {
				element.textContent = count;
				element.hidden = count === 0;
			});
	}

	function showCartToast(message) {
		let toast = document.querySelector(
			".readora-cart-toast"
		);

		if (toast) {
			toast.remove();
		}

		toast = document.createElement("div");
		toast.className = "readora-cart-toast";
		toast.textContent = message;

		document.body.appendChild(toast);

		requestAnimationFrame(() => {
			toast.classList.add("show");
		});

		setTimeout(() => {
			toast.classList.remove("show");

			setTimeout(() => {
				if (toast && toast.parentNode) {
					toast.remove();
				}
			}, 250);
		}, 2200);
	}

	function isPremiumBook(book) {
		if (!book) {
			return false;
		}

		const premium =
			book.premium === true ||
			String(book.premium).toLowerCase() === "true";

		const price = Number(book.price || 0);

		return premium && price > 0;
	}

	function addToCart(book) {
		if (!book || !book.id) {
			showCartToast("Book information is missing.");
			return false;
		}

		if (!isPremiumBook(book)) {
			showCartToast(
				"This is a free book. You can read it directly."
			);

			return false;
		}

		const cart = getCart();

		const alreadyInCart = cart.some(item => {
			return String(item.id) === String(book.id);
		});

		if (alreadyInCart) {
			showCartToast(
				"This book is already in your cart."
			);

			return false;
		}

		const cartBook = {
			id: book.id,
			title: book.title || "Untitled Book",
			author: book.author || "Unknown Author",
			category: book.category || "Book",
			language: book.language || "English",
			price: Number(book.price) || 0,
			image: book.image || "",
			premium: true
		};

		cart.push(cartBook);

		saveCart(cart);

		showCartToast(
			"Book added to your cart."
		);

		return true;
	}

	function removeFromCart(bookId) {
		const updatedCart = getCart().filter(item => {
			return String(item.id) !== String(bookId);
		});

		saveCart(updatedCart);

		showCartToast(
			"Book removed from your cart."
		);
	}

	function clearCart() {
		saveCart([]);
	}

	function calculateSubtotal(cart) {
		const currentCart = Array.isArray(cart)
			? cart
			: getCart();

		return currentCart.reduce((total, book) => {
			return total + Number(book.price || 0);
		}, 0);
	}

	function setupCartLink() {
		// Cart link injection disabled — readora-cart-link removed from all pages
		return;
	}

	function setupUtilityStyles() {
		if (
			document.getElementById(
				"readora-cart-utility-styles"
			)
		) {
			return;
		}

		const style = document.createElement("style");

		style.id = "readora-cart-utility-styles";

		style.textContent = `
            .readora-add-cart {
                display: block;
                margin-top: 12px;
                padding: 8px 11px;
                border: 1px solid #ddd4c0;
                border-radius: 3px;
                color: #2f4a3a;
                background: transparent;
                font: 500 11px/1.2 Inter, sans-serif;
                letter-spacing: .5px;
                text-transform: uppercase;
                cursor: pointer;
            }

            .readora-add-cart:hover {
                background: #ece5d8;
                border-color: #2f4a3a;
            }

            .readora-cart-toast {
                position: fixed;
                right: 24px;
                bottom: 24px;
                z-index: 9999;
                max-width: calc(100vw - 48px);
                padding: 12px 16px;
                background: #2f4a3a;
                color: #faf6ee;
                font: 12px/1.3 Inter, sans-serif;
                opacity: 0;
                transform: translateY(8px);
                transition:
                    opacity .2s ease,
                    transform .2s ease;
            }

            .readora-cart-toast.show {
                opacity: 1;
                transform: translateY(0);
            }
        `;

		document.head.appendChild(style);
	}

	window.getCart = getCart;
	window.saveCart = saveCart;
	window.addToCart = addToCart;
	window.removeFromCart = removeFromCart;
	window.clearCart = clearCart;
	window.calculateSubtotal = calculateSubtotal;
	window.updateCartCount = updateCartCount;
	window.showCartToast = showCartToast;
	window.isPremiumBook = isPremiumBook;

	document.addEventListener(
		"DOMContentLoaded",
		() => {
			setupUtilityStyles();
			setupCartLink();
			updateCartCount();
		}
	);
})();