(function() {
    function getStoredTheme() {
        return localStorage.getItem('symtotrack_theme') || 'dark';
    }

    function resolveTheme(theme) {
        if (theme === 'system') {
            return (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'light';
        }
        return theme || 'dark';
    }

    function applyTheme(theme) {
        const resolved = resolveTheme(theme);
        document.documentElement.setAttribute('data-theme', resolved);
        localStorage.setItem('symtotrack_theme', theme);

        // Update modal option highlights if modal exists
        const modal = document.getElementById('themeModal');
        if (modal) {
            document.querySelectorAll('.theme-option').forEach(opt => {
                if (opt.getAttribute('data-value') === theme) {
                    opt.classList.add('selected');
                } else {
                    opt.classList.remove('selected');
                }
            });
        }
    }

    // Run immediately on script load
    applyTheme(getStoredTheme());

    // Listen for OS system theme changes
    if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
            if (getStoredTheme() === 'system') {
                applyTheme('system');
            }
        });
    }

    window.SymptoTheme = {
        get: getStoredTheme,
        set: applyTheme,
        openModal: function() {
            const modal = document.getElementById('themeModal');
            if (modal) {
                applyTheme(getStoredTheme());
                modal.classList.add('open');
            }
        },
        closeModal: function() {
            const modal = document.getElementById('themeModal');
            if (modal) modal.classList.remove('open');
        }
    };
})();
