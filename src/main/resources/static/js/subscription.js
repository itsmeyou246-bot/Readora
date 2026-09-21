document.addEventListener("DOMContentLoaded", function () {
    const planButtons = document.querySelectorAll(".plan-btn[data-plan]");
    const institutionalButton = document.getElementById("institutionalBtn");

    planButtons.forEach(function (button) {
        button.addEventListener("click", async function () {
            const plan = button.getAttribute("data-plan");
            button.disabled = true;

            try {
                const res = await fetch("/api/payments/esewa/initiate", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify({
                        paymentType: "SUBSCRIPTION",
                        plan: plan
                    })
                });

                if (res.status === 401) {
                    window.location.href = "/login?redirect=subscription";
                    return;
                }

                const data = await res.json();

                if (!res.ok) {
                    throw new Error(data.message || "Could not start subscription checkout.");
                }

                if (data.free) {
                    // FREE plan — already activated server-side
                    window.location.href = "/dashboard?payment=processed";
                    return;
                }

                // Build and auto-submit the signed eSewa form
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