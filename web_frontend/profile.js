const worldLanguages = [
    "Telugu (తెలుగు) 🇮🇳",
    "English (US) 🌐",
    "English (UK) 🇬🇧",
    "Hindi (हिंदी) 🇮🇳",
    "Tamil (தமிழ்) 🇮🇳",
    "Kannada (ಕನ್ನಡ) 🇮🇳",
    "Malayalam (മലയാളം) 🇮🇳",
    "Bengali (বাংলা) 🇮🇳",
    "Marathi (मराठी) 🇮🇳",
    "Gujarati (ગુજરાતી) 🇮🇳",
    "Punjabi (ਪੰਜਾਬੀ) 🇮🇳",
    "Urdu (اردو) 🇵🇰",
    "Spanish (Español) 🇪🇸",
    "French (Français) 🇫🇷",
    "German (Deutsch) 🇩🇪",
    "Mandarin Chinese (中文) 🇨🇳",
    "Japanese (日本語) 🇯🇵",
    "Korean (한국어) 🇰🇷",
    "Arabic (العربية) 🇸🇦",
    "Russian (Русский) 🇷🇺",
    "Portuguese (Português) 🇧🇷",
    "Italian (Italiano) 🇮🇹"
];

function openFSModal(id) {
    document.getElementById(id).classList.add('open');
    if (id === 'langModal') {
        renderLanguageList(worldLanguages);
    }
}

function closeFSModal(id) {
    document.getElementById(id).classList.remove('open');
}

function renderLanguageList(langs) {
    const container = document.getElementById('langListContainer');
    const selectedLang = localStorage.getItem('app_language') || 'Telugu (తెలుగు) 🇮🇳';
    
    container.innerHTML = langs.map(l => `
        <div class="lang-item-btn" onclick="selectLanguage('${l}')">
            <span>${l}</span>
            ${l === selectedLang ? '<span style="color:#4ADE80; font-weight:800;">✓</span>' : ''}
        </div>
    `).join('');
}

function filterLanguages() {
    const q = document.getElementById('langSearch').value.toLowerCase();
    const filtered = worldLanguages.filter(l => l.toLowerCase().includes(q));
    renderLanguageList(filtered);
}

function selectLanguage(lang) {
    localStorage.setItem('app_language', lang);
    document.getElementById('val_app_language').innerText = lang;
    
    if (window.SymptoI18n) {
        window.SymptoI18n.setLanguage(lang);
    }
    
    closeFSModal('langModal');
}

function toggleThemeMode() {
    if (window.SymptoTheme) {
        window.SymptoTheme.openModal();
    }
}

function saveProfileChanges() {
    const name = document.getElementById('editName').value;
    const email = document.getElementById('editEmail').value;
    
    localStorage.setItem('user_name', name);
    localStorage.setItem('user_email', email);
    
    document.getElementById('profileName').innerText = name;
    document.getElementById('profileEmail').innerText = email;
    
    closeFSModal('personalModal');

    if (window.SymptoI18n) {
        window.SymptoI18n.translate();
    }
}

function toggleSecurityFeature(key, enabled) {
    localStorage.setItem(`symtotrack_sec_${key}`, enabled);
    const names = {
        'app_lock': 'App PIN Security Lock',
        'incognito': 'Incognito Health Mode',
        'telemetry': 'Anonymous Disease Telemetry'
    };
    alert(`${names[key] || 'Security setting'} has been ${enabled ? 'ENABLED 🟢' : 'DISABLED 🔴'}`);
}

function exportUserData() {
    const data = {
        user: {
            name: localStorage.getItem('user_name') || 'Yasaswini Kuchi',
            email: localStorage.getItem('user_email') || 'yasaswini@example.com',
            language: localStorage.getItem('app_language') || 'English'
        },
        symptomHistory: JSON.parse(localStorage.getItem('symtotrack_symptom_history') || '[]'),
        familyMembers: JSON.parse(localStorage.getItem('symtotrack_family_members') || '[]'),
        medications: JSON.parse(localStorage.getItem('symtotrack_pill_reminders') || '[]'),
        exportTimestamp: new Date().toISOString()
    };

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `symtotrack_health_backup_${Date.now()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function clearAllHealthData() {
    if (confirm('⚠️ WARNING: This will permanently wipe all local symptom logs, family profiles, and pill reminders. Proceed?')) {
        localStorage.removeItem('symtotrack_symptom_history');
        localStorage.removeItem('symtotrack_family_members');
        localStorage.removeItem('symtotrack_pill_reminders');
        alert('All local symptom history and health cache have been wiped successfully.');
        location.reload();
    }
}

window.addEventListener('DOMContentLoaded', () => {
    const name = localStorage.getItem('user_name') || 'Yasaswini Kuchi';
    const email = localStorage.getItem('user_email') || 'yasaswini@example.com';
    const lang = localStorage.getItem('app_language') || 'Telugu (తెలుగు) 🇮🇳';

    document.getElementById('profileName').innerText = name;
    document.getElementById('profileEmail').innerText = email;
    document.getElementById('val_app_language').innerText = lang;

    // Load saved security toggles
    const toggleLock = document.getElementById('toggleAppLock');
    if (toggleLock) toggleLock.checked = localStorage.getItem('symtotrack_sec_app_lock') === 'true';

    const toggleIncog = document.getElementById('toggleIncognito');
    if (toggleIncog) toggleIncog.checked = localStorage.getItem('symtotrack_sec_incognito') === 'true';

    const toggleTelem = document.getElementById('toggleTelemetry');
    if (toggleTelem) toggleTelem.checked = localStorage.getItem('symtotrack_sec_telemetry') !== 'false';

    if (window.SymptoI18n) {
        window.SymptoI18n.translate();
    }

    document.getElementById('btnLogout').addEventListener('click', () => {
        if (confirm('Are you sure you want to log out?')) {
            localStorage.clear();
            window.location.href = 'login.html';
        }
    });
});
