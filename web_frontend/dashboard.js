document.addEventListener('DOMContentLoaded', () => {

    // ── Load user name ──────────────────────────────────────────────────────
    const greetingEl = document.getElementById('tv_user_greeting');
    const storedName = localStorage.getItem('user_name') || 'Yasaswini Kuchi';
    if (greetingEl) {
        greetingEl.textContent = `Good morning, ${storedName} 👋`;
    }

    // ── Emergency SOS Modal ─────────────────────────────────────────────────
    const btnSOS     = document.getElementById('btnEmergencySOS');
    const sosModal   = document.getElementById('sosModal');
    const btnCancel  = document.getElementById('btnCancelSOS');
    const countdownEl = document.getElementById('sosCountdown');

    let countdownTimer = null;

    function openSOS() {
        if (!sosModal) return;

        // Vibrate on supported devices (mobile haptic feedback)
        if ('vibrate' in navigator) navigator.vibrate([300, 100, 300]);

        sosModal.classList.add('sos-open');

        // Start countdown from 5
        let count = 5;
        if (countdownEl) countdownEl.textContent = count;

        countdownTimer = setInterval(() => {
            count--;
            if (countdownEl) countdownEl.textContent = count;

            if (count <= 0) {
                clearInterval(countdownTimer);
                // Auto-trigger the call link
                const dialLink = document.getElementById('btnDialNow');
                if (dialLink) dialLink.click();
            }
        }, 1000);
    }

    function closeSOS() {
        if (!sosModal) return;
        clearInterval(countdownTimer);
        sosModal.classList.remove('sos-open');
        if (countdownEl) countdownEl.textContent = '5';
    }

    if (btnSOS)    btnSOS.addEventListener('click', openSOS);
    if (btnCancel) btnCancel.addEventListener('click', closeSOS);

    // Also close if user taps outside the modal box
    if (sosModal) {
        sosModal.addEventListener('click', (e) => {
            if (e.target === sosModal) closeSOS();
        });
    }
});
