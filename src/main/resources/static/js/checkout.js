document.addEventListener(
    "DOMContentLoaded",
    setupCheckout
);

function formatPrice(amount) {
    return `Rs. ${Number(amount || 0).toLocaleString("en-IN")}`;
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function setupCheckout() {
    "use strict";

    console.log("READORA: Checkout page loaded.");

    if (typeof window.getCart !== "function") {
        console.error(
            "READORA: cart-utils.js was not loaded."
        );

        showCheckoutError(
            "Cart system is not loaded. Please refresh the page."
        );

        return;
    }

    const cart = window.getCart();

    if (typeof window.updateCartCount === "function") {
        window.updateCartCount();
    }

    const summary =
        document.getElementById("orderSummary");

    const checkoutForm =
        document.getElementById("checkoutForm");

    const submitButton =
        checkoutForm
            ? checkoutForm.querySelector(
                'button[type="submit"]'
            )
            : null;

    const payLabel =
        submitButton
            ? submitButton.querySelector(
                "[data-pay-label]"
            )
            : null;

    if (!summary || !checkoutForm || !submitButton) {
        console.error(
            "READORA: Checkout HTML elements are missing."
        );

        return;
    }

    if (!cart.length) {
        summary.innerHTML = `
            <div class="empty-summary">
                <h2>Your cart is empty.</h2>
                <a class="button" href="/Book">
                    Explore Books
                </a>
            </div>
        `;

        submitButton.disabled = true;

        return;
    }

    const subtotal =
        typeof window.calculateSubtotal === "function"
            ? window.calculateSubtotal(cart)
            : cart.reduce(
                (total, book) => {
                    return total + Number(book.price || 0);
                },
                0
            );

    const [heroBook, ...restBooks] = cart;

    const heroHtml = `
        <div class="product-hero">
            <img
                src="${escapeHtml(heroBook.image || "")}"
                alt="${escapeHtml(heroBook.title || "Book")}"
            >

            <h3>
                ${escapeHtml(
        heroBook.title || "Untitled Book"
    )}
            </h3>

            <p>
                By ${escapeHtml(
        heroBook.author || "Unknown Author"
    )}
            </p>

            <strong>
                ${formatPrice(heroBook.price)}
            </strong>
        </div>
    `;

    const restItemsHtml = restBooks
        .map(book => {
            return `
                <div class="summary-item">
                    <span>
                        ${escapeHtml(
                book.title || "Untitled Book"
            )}

                        <small>
                            ${escapeHtml(
                book.author || "Unknown Author"
            )}
                        </small>
                    </span>

                    <strong>
                        ${formatPrice(book.price)}
                    </strong>
                </div>
            `;
        })
        .join("");

    const summaryCardHtml = `
        <div class="summary-card">
            <h4>Order Summary</h4>

            ${restItemsHtml}

            <div class="summary-row">
                <span>Subtotal</span>
                <span>${formatPrice(subtotal)}</span>
            </div>

            <div class="summary-row">
                <span>Discount</span>
                <span>Rs. 0</span>
            </div>

            <div class="summary-row summary-total">
                <span>Total</span>
                <span>${formatPrice(subtotal)}</span>
            </div>
        </div>
    `;

    summary.innerHTML =
        heroHtml +
        summaryCardHtml;

    if (payLabel) {
        payLabel.textContent =
            `Pay ${formatPrice(subtotal)}`;
    }

    checkoutForm.addEventListener(
        "submit",
        function (event) {
            event.preventDefault();

            completePurchase(cart);
        }
    );
}

async function completePurchase(cart) {
    "use strict";

    const nameElement =
        document.getElementById("fullName");

    const emailElement =
        document.getElementById("email");

    const submitButton =
        document.querySelector(
            '#checkoutForm button[type="submit"]'
        );

    const name =
        nameElement
            ? nameElement.value.trim()
            : "";

    const email =
        emailElement
            ? emailElement.value.trim()
            : "";

    clearCheckoutError();

    if (!name || !email || !email.includes("@")) {
        showCheckoutError(
            "Please enter your name and a valid email."
        );

        return;
    }

    if (submitButton) {
        submitButton.disabled = true;

        const payLabel =
            submitButton.querySelector(
                "[data-pay-label]"
            );

        if (payLabel) {
            payLabel.textContent =
                "Redirecting to eSewa...";
        } else {
            submitButton.textContent =
                "Redirecting to eSewa...";
        }
    }

    try {
        const bookIds = cart.map(book => Number(book.id));

        const response = await fetch(
            "/api/payments/esewa/initiate",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify({
                    paymentType: "CART_PURCHASE",
                    bookIds: bookIds
                })
            }
        );

        if (response.status === 401) {
            showCheckoutError(
                "Your login session has expired. Please log in again."
            );

            window.setTimeout(function () {
                window.location.href =
                    "/login?redirect=checkout";
            }, 1000);

            return;
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                (data && data.message) ||
                "The purchase could not be started."
            );
        }

        if (data.free) {
            // Total was Rs. 0 — already fulfilled server-side, nothing to redirect to
            if (typeof window.saveCart === "function") {
                window.saveCart([]);
            }

            window.location.href = "/library?payment=processed";
            return;
        }

        // Build and auto-submit the signed eSewa form — the cart is only
        // cleared once eSewa confirms payment and the server fulfills it,
        // not here, so a cancelled payment doesn't lose the cart.
        const form = document.createElement("form");
        form.method = "POST";
        form.action = data.formActionUrl;

        Object.entries(data.formFields).forEach(function ([key, value]) {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = key;
            input.value = value;
            form.appendChild(input);
        });

        document.body.appendChild(form);
        form.submit();
    } catch (error) {
        console.error(
            "READORA: Checkout error:",
            error
        );

        showCheckoutError(
            error.message ||
            "Unable to start checkout. Please try again."
        );

        if (submitButton) {
            submitButton.disabled = false;

            const payLabel =
                submitButton.querySelector(
                    "[data-pay-label]"
                );

            if (payLabel) {
                payLabel.textContent = "Place Order";
            } else {
                submitButton.textContent = "Place Order";
            }
        }
    }
}

function showCheckoutError(message) {
    const errorElement =
        document.getElementById("checkoutError");

    if (errorElement) {
        errorElement.textContent = message;
    } else {
        alert(message);
    }
}

function clearCheckoutError() {
    const errorElement =
        document.getElementById("checkoutError");

    if (errorElement) {
        errorElement.textContent = "";
    }
}