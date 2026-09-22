/*
 * READORA - shared header behaviour (reader / author / admin)
 *
 *  - fills the profile dropdown with the logged-in name and role
 *  - opens / closes the dropdown
 *  - LOGOUT: asks the server to delete the HttpOnly login cookie
 *            (JavaScript cannot delete it), then clears the browser copy
 *  - SESSION CHECK: if the server says the login has expired,
 *            stale tokens are removed, and admin / author pages go to /login
 *            instead of showing errors or an endless "Loading..."
 */
(function () {
    'use strict';

    var TOKEN_KEYS = ['readoraToken', 'accessToken', 'access_token', 'jwt', 'token'];
    var USER_KEYS = ['readoraUser', 'readoraActiveRole', 'user'];

    function clearClientSession() {
        [window.localStorage, window.sessionStorage].forEach(function (store) {
            try {
                TOKEN_KEYS.concat(USER_KEYS).forEach(function (key) {
                    store.removeItem(key);
                });
            } catch (e) { /* storage blocked - ignore */ }
        });
    }

    function readUser() {
        try {
            var raw = localStorage.getItem('readoraUser');
            return raw ? JSON.parse(raw) : null;
        } catch (e) {
            return null;
        }
    }

    function setText(selector, value) {
        var el = document.querySelector(selector);
        if (el) el.textContent = value;
    }

    function populateUserInfo(user) {
        if (!user) return;
        var name = user.name || '';
        var role = user.role || '';

        setText('.reader-user-name', name);
        setText('.reader-user-role', role);
        setText('.author-user-name', name);
        setText('.author-user-role', role);
        setText('.admin-user-name', name);
        setText('.admin-user-role', role);
    }

    function allDropdowns() {
        return document.querySelectorAll(
            '.reader-user-dropdown, .author-user-dropdown, .admin-user-dropdown'
        );
    }

    function closeAllDropdowns() {
        allDropdowns().forEach(function (d) { d.classList.add('hidden'); });
    }

    function toggleDropdown(button, dropdown) {
        button.addEventListener('click', function (event) {
            event.stopPropagation();
            allDropdowns().forEach(function (d) {
                if (d !== dropdown) d.classList.add('hidden');
            });
            dropdown.classList.toggle('hidden');
        });
    }

    function logout() {
        function finish() {
            clearClientSession();
            window.location.href = '/login';
        }
        try {
            fetch('/api/auth/logout', { method: 'POST', credentials: 'include' })
                .then(finish, finish);
        } catch (e) {
            finish();
        }
    }

    function bindProfileEvents() {
        [
            ['.reader-user-btn', '.reader-user-dropdown'],
            ['.author-user-btn', '.author-user-dropdown'],
            ['.admin-user-btn', '.admin-user-dropdown']
        ].forEach(function (pair) {
            var button = document.querySelector(pair[0]);
            var dropdown = document.querySelector(pair[1]);
            if (button && dropdown) toggleDropdown(button, dropdown);
        });

        document.addEventListener('click', function (event) {
            if (!event.target.closest('.reader-user-profile') &&
                !event.target.closest('.author-user-profile') &&
                !event.target.closest('.admin-user-profile')) {
                closeAllDropdowns();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') closeAllDropdowns();
        });

        ['.reader-logout-btn', '.author-logout-btn', '.admin-logout-btn'].forEach(function (selector) {
            var button = document.querySelector(selector);
            if (button) {
                button.addEventListener('click', function (event) {
                    event.stopPropagation();
                    logout();
                });
            }
        });
    }

    function verifySession() {
        if (!window.fetch) return;

        var protectedArea =
            !!document.querySelector('.admin-user-profile') ||
            !!document.querySelector('.author-user-profile');

        fetch('/api/auth/me', { credentials: 'include' })
            .then(function (response) {
                if (response.status === 401) {
                    clearClientSession();
                    if (protectedArea) {
                        window.location.href = '/login';
                    }
                    return null;
                }
                return response.ok ? response.json() : null;
            })
            .then(function (me) {
                if (!me) return;

                // keep the stored user (and its token) but refresh name / role
                var stored = readUser() || {};
                stored.name = me.name || stored.name;
                stored.email = me.email || stored.email;
                stored.role = me.role || stored.role;
                try {
                    localStorage.setItem('readoraUser', JSON.stringify(stored));
                } catch (e) { /* ignore */ }

                populateUserInfo(stored);
            })
            .catch(function () { /* server unreachable - leave the page as it is */ });
    }

    function init() {
        populateUserInfo(readUser());
        bindProfileEvents();
        verifySession();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();