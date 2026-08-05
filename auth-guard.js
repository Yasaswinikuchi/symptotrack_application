// auth-guard.js - Centralized Authentication & Session Security for SymtoTrack Pro
(function () {
    const PUBLIC_PAGES = ['login.html', 'signup.html', 'index.html', ''];

    function getPageName() {
        const path = window.location.pathname;
        const page = path.substring(path.lastIndexOf('/') + 1);
        return page.split('?')[0];
    }

    function isAuthenticated() {
        const email = localStorage.getItem('user_email');
        const token = localStorage.getItem('user_token') || localStorage.getItem('user_id');
        return !!(email || token);
    }

    function checkAuth() {
        const currentPage = getPageName();
        const isPublic = PUBLIC_PAGES.includes(currentPage);

        if (!isPublic && !isAuthenticated()) {
            console.warn('[AuthGuard] Unauthenticated access attempt to ' + currentPage + '. Redirecting to login.html');
            sessionStorage.setItem('redirect_after_login', window.location.href);
            window.location.href = 'login.html';
        }
    }

    window.logoutUser = function () {
        localStorage.removeItem('user_email');
        localStorage.removeItem('user_name');
        localStorage.removeItem('user_id');
        localStorage.removeItem('user_token');
        sessionStorage.clear();
        window.location.href = 'login.html';
    };

    window.getCurrentUser = function () {
        return {
            id: localStorage.getItem('user_id') || '1',
            name: localStorage.getItem('user_name') || 'Yasaswini Kuchi',
            email: localStorage.getItem('user_email') || 'yasaswini@example.com'
        };
    };

    // Run auth check immediately when script loads
    checkAuth();
})();
