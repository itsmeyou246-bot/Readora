(() => {
	const CART_KEY = 'readoraCart';
	const books = [
		{ id: 1, title: 'What You Leave Behind', author: 'Demo Author', category: 'Fiction', price: 250, image: 'img/what you leave behind.jpg', premium: true },
		{ id: 2, title: 'The Story of a New Name', author: 'Elena Ferrante', category: 'Fiction', price: 450, image: 'img/TheStoryofnewname.png', premium: true },
		{ id: 3, title: 'The Silent Spring', author: 'Rachel Carson', category: 'Science & Nature', price: 450, image: 'img/TheSilentSpring.png', premium: true },
		{ id: 4, title: 'The Silent Patient', author: 'Alex Michaelides', category: 'Thriller', price: 350, image: 'img/The-Silent-Patient-.webp', premium: true },
		{ id: 5, title: 'The Hunger Games', author: 'Suzanne Collins', category: 'Fiction', price: 300, image: 'img/the-hunger-games.jpg', premium: true },
		{ id: 6, title: 'The Secret Life of Bees', author: 'Sue Monk Kidd', category: 'Fiction', price: 300, image: 'img/The Secret Life of Bees.jpg', premium: true },
		{ id: 7, title: 'Structures of Light', author: 'Hiroshi Sugimoto', category: 'Architecture', price: 600, image: 'img/StructuresOfLight.png', premium: true },
		{ id: 8, title: 'Outliers', author: 'Malcolm Gladwell', category: 'Non-fiction', price: 350, image: 'img/Outerliers.jpg', premium: true },
		{ id: 9, title: 'The Night Tiger', author: 'Yangsze Choo', category: 'Fiction', price: 350, image: 'img/Night tiger.png', premium: true },
		{ id: 10, title: 'सेतो धरती', author: 'Amar Neupane', category: 'Read Nepal', price: 300, image: 'img/seto dharti.png', premium: true, description: 'A poignant exploration of child widowhood in Nepal, Seto Dharti is a masterpiece that delves deep into the human spirit.' },
		{ id: 11, title: 'निलो प्रेम', author: 'Demo Author', category: 'Read Nepal', price: 250, image: 'img/nilo-prem.jpg', premium: true }
	];

	function getCart() {
		try {
			return JSON.parse(localStorage.getItem(CART_KEY)) || [];
		} catch (error) {
			return [];
		}
	}

	function saveCart(cart) {
		localStorage.setItem(CART_KEY, JSON.stringify(cart));
		updateCartCount();
	}

	function updateCartCount() {
		const count = getCart().length;
		document.querySelectorAll('[data-cart-count]').forEach(element => {
			element.textContent = count;
			element.hidden = count === 0;
		});
	}

	function showCartToast(message) {
		const toast = document.createElement('div');
		toast.className = 'readora-cart-toast';
		toast.textContent = message;
		document.body.appendChild(toast);
		setTimeout(() => toast.classList.add('show'), 10);
		setTimeout(() => {
			toast.classList.remove('show');
			setTimeout(() => toast.remove(), 250);
		}, 2200);
	}

	function addToCart(book) {
		if (!book || !book.id) return;
		const cart = getCart();
		if (cart.some(item => String(item.id) === String(book.id))) {
			showCartToast('This book is already in your cart.');
			return;
		}

		cart.push({
			id: book.id,
			title: book.title,
			author: book.author,
			category: book.category,
			price: Number(book.price) || 0,
			image: book.image,
			premium: !!book.premium
		});
		saveCart(cart);
		showCartToast('Book added to your cart.');
	}

	function removeFromCart(bookId) {
		saveCart(getCart().filter(item => String(item.id) !== String(bookId)));
	}

	function calculateSubtotal(cart) {
		return (cart || getCart()).reduce((total, book) => total + Number(book.price || 0), 0);
	}

	function setupCartLink() {
		const header = document.querySelector('.header-inner');
		if (!header) return;
		const existingLink = header.querySelector('a[href="cart.html"]');
		if (existingLink) {
			existingLink.classList.add('readora-cart-link');
			return;
		}
		if (header.querySelector('.readora-cart-link')) return;
		const link = document.createElement('a');
		link.href = 'cart.html';
		link.className = 'readora-cart-link';
		link.innerHTML = '<i class="fa-solid fa-cart-shopping" aria-hidden="true"></i> Cart <span data-cart-count hidden>0</span>';
		header.appendChild(link);
	}

	function setupUtilityStyles() {
		if (document.getElementById('readora-cart-utility-styles')) return;
		const style = document.createElement('style');
		style.id = 'readora-cart-utility-styles';
		style.textContent = `
			.readora-cart-link { display: inline-flex; align-items: center; gap: 6px; color: #55503f; font-size: 12px; letter-spacing: .7px; text-transform: uppercase; white-space: nowrap; }
			.readora-cart-link:hover { color: #201d18; }
			.readora-add-cart { display: block; margin-top: 12px; padding: 8px 11px; border: 1px solid #ddd4c0; border-radius: 3px; color: #2f4a3a; background: transparent; font: 500 11px/1.2 Inter, sans-serif; letter-spacing: .5px; text-transform: uppercase; cursor: pointer; }
			.readora-add-cart:hover { background: #ece5d8; border-color: #2f4a3a; }
			.book-card { position: relative; }
			.readora-premium-badge { position: absolute; top: 8px; left: 8px; display: inline-block; padding: 4px 8px; background: #2f4a3a; color: #faf6ee; font: 600 10px/1 Inter, sans-serif; letter-spacing: .8px; text-transform: uppercase; }
			.readora-premium-card { cursor: pointer; transition: transform .2s ease, box-shadow .2s ease; }
			.readora-premium-card:hover { transform: translateY(-2px); box-shadow: 0 8px 18px rgba(32, 29, 24, .08); }
			.readora-cart-toast { position: fixed; right: 24px; bottom: 24px; z-index: 9999; padding: 12px 16px; background: #2f4a3a; color: #faf6ee; font: 12px/1.3 Inter, sans-serif; opacity: 0; transform: translateY(8px); transition: opacity .2s ease, transform .2s ease; }
			.readora-cart-toast.show { opacity: 1; transform: translateY(0); }
		`;
		document.head.appendChild(style);
	}

	function setupBookCards() {
		document.querySelectorAll('.book-card').forEach(card => {
			if (card.querySelector('.readora-add-cart') || card.classList.contains('readora-premium-card')) return;
			const titleElement = card.querySelector('h3');
			const book = titleElement && books.find(item => item.title === titleElement.textContent.trim());
			if (!book) return;
			if (book.premium) {
				const badge = document.createElement('span');
				badge.className = 'readora-premium-badge';
				badge.textContent = 'Premium';
				card.classList.add('readora-premium-card');
				card.setAttribute('role', 'link');
				card.setAttribute('tabindex', '0');
				const openDetails = () => {
					window.location.href = `book-details.html?id=${encodeURIComponent(book.id)}`;
				};
				card.addEventListener('click', openDetails);
				card.addEventListener('keydown', event => {
					if (event.key === 'Enter' || event.key === ' ') {
						event.preventDefault();
						openDetails();
					}
				});
				card.appendChild(badge);
				return;
			}
			const button = document.createElement('button');
			button.type = 'button';
			button.className = 'readora-add-cart';
			button.innerHTML = '<i class="fa-solid fa-cart-plus" aria-hidden="true"></i> Add to Cart';
			button.addEventListener('click', event => {
				event.stopPropagation();
				addToCart(book);
			});
			card.appendChild(button);
		});
	}

	window.getCart = getCart;
	window.saveCart = saveCart;
	window.addToCart = addToCart;
	window.removeFromCart = removeFromCart;
	window.calculateSubtotal = calculateSubtotal;
	window.updateCartCount = updateCartCount;
	window.showCartToast = showCartToast;
	window.READORA_CART_BOOKS = books;

	document.addEventListener('DOMContentLoaded', () => {
		setupUtilityStyles();
		setupCartLink();
		setupBookCards();
		updateCartCount();
	});
})();
