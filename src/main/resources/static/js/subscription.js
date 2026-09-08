document.addEventListener("DOMContentLoaded", function () {
    const planButtons = document.querySelectorAll(".plan-btn[data-plan]");
    const institutionalButton = document.getElementById("institutionalBtn");

    function getRegisteredEmail() {
        try {
            const user = JSON.parse(localStorage.getItem("readoraUser") || "null");
            if (user && user.email) {
                return user.email.trim();
            }
        } catch (error) {
            localStorage.removeItem("readoraUser");
        }

        const email = window.prompt("Enter your registered READORA email:");
        return email ? email.trim() : "";
    }

    planButtons.forEach(function (button) {
        button.addEventListener("click", async function () {
            const plan = button.getAttribute("data-plan");
            const email = getRegisteredEmail();

            if (!email) {
                alert("A registered READORA email is required to select a plan.");
                return;
            }

            button.disabled = true;

            try {
                const response = await fetch("/api/subscription", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        email: email,
                        plan: plan
                    })
                });

                const data = await response.json().catch(function () {
                    return null;
                });

                if (!response.ok) {
                    throw new Error(data && data.message
                        ? data.message
                        : "Subscription selection failed.");
                }

                localStorage.setItem("selectedPlan", data.plan);
                localStorage.setItem("planPrice", data.price);
                alert(data.message);
                window.location.href = "/login";
            } catch (error) {
                alert(error.message);
                button.disabled = false;
            }
        });
    });

    if (institutionalButton) {
        institutionalButton.addEventListener("click", function () {
            alert("Please contact READORA support for Institutional plans.");
        });
    }
});
