(function () {
    "use strict";

    if (typeof window.currentUserId === "undefined" || !window.currentUserId) {
        return;
    }

    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
        console.warn("READORA: SockJS/Stomp not loaded.");
        return;
    }

    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function () {
        stompClient.subscribe("/topic/notifications/" + window.currentUserId, function (message) {
            const notification = JSON.parse(message.body);
            showLiveNotification(notification);
        });
    });

    function showLiveNotification(notification) {
        const badge = document.querySelector("[data-notification-count]");
        if (badge) {
            const current = parseInt(badge.textContent || "0", 10) || 0;
            badge.textContent = String(current + 1);
            badge.style.display = "inline-block";
        }

        const toast = document.createElement("div");
        toast.textContent = notification.title || "New notification";
        toast.style.cssText =
            "position:fixed;top:16px;right:16px;background:#1b3022;color:#fff;" +
            "padding:12px 18px;border-radius:8px;font-size:13px;z-index:9999;" +
            "box-shadow:0 4px 14px rgba(0,0,0,0.2);max-width:280px;";
        document.body.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 4000);
    }
})();