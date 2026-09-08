(() => {
	function loadBookDetails() {
		const id = new URLSearchParams(window.location.search).get('id');
		const book = (window.READORA_CART_BOOKS || []).find(item => String(item.id) === String(id));
		if (!book) return;

		document.getElementById('bookTitle').textContent = book.title;
		document.getElementById('bookAuthor').textContent = book.author;
		document.getElementById('bookDescription').textContent = book.description || 'A carefully selected story for readers who value lasting ideas and beautiful writing.';
		document.getElementById('bookPrice').textContent = `NPR ${Number(book.price).toLocaleString()}`;
		document.getElementById('bookCover').src = `/${book.image}`;
		document.getElementById('bookCover').alt = book.title;
		document.getElementById('previewTitle').textContent = book.title;
		document.getElementById('previewAuthor').textContent = book.author;

		document.getElementById('addToCartBtn').addEventListener('click', () => {
			window.addToCart(book);
		});
	}

	document.addEventListener('DOMContentLoaded', loadBookDetails);
})();