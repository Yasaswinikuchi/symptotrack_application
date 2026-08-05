(function() {
    function checkAppLock() {
        const isLockEnabled = localStorage.getItem('symtotrack_sec_app_lock') === 'true';
        const isUnlocked = sessionStorage.getItem('symtotrack_unlocked') === 'true';

        if (isLockEnabled && !isUnlocked) {
            renderLockScreen();
        }
    }

    function renderLockScreen() {
        if (document.getElementById('appLockOverlay')) return;

        const overlay = document.createElement('div');
        overlay.id = 'appLockOverlay';
        overlay.style.cssText = `
            position: fixed;
            inset: 0;
            background: #0B132B;
            background: radial-gradient(circle at 50% 0%, #162447 0%, #0B132B 70%);
            z-index: 999999;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 24px;
            color: #FFFFFF;
            font-family: 'Plus Jakarta Sans', sans-serif;
        `;

        const savedPin = localStorage.getItem('symtotrack_pin') || '1234';

        overlay.innerHTML = `
            <div style="background: rgba(19, 28, 49, 0.9); backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 28px; padding: 32px 24px; max-width: 380px; width: 100%; text-align: center; box-shadow: 0 20px 50px rgba(0,0,0,0.6);">
                <div style="width: 64px; height: 64px; border-radius: 50%; background: rgba(59, 130, 246, 0.2); color: #3B82F6; display: flex; align-items: center; justify-content: center; font-size: 28px; margin: 0 auto 16px;">
                    🔒
                </div>
                <h2 style="font-size: 20px; font-weight: 800; margin-bottom: 6px; color: #FFF;">SymptoTrack Locked</h2>
                <p style="font-size: 13px; color: #94A3B8; margin-bottom: 24px;">Enter your 4-digit Security PIN to access health records</p>
                
                <input type="password" maxlength="4" id="pinInputField" style="width: 100%; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.2); border-radius: 16px; padding: 16px; font-size: 24px; text-align: center; letter-spacing: 12px; color: #FFF; outline: none; margin-bottom: 16px;" placeholder="••••" autofocus>
                
                <div id="pinErrorMsg" style="color: #F87171; font-size: 12px; font-weight: 700; margin-bottom: 14px; min-height: 18px;"></div>

                <button id="btnUnlockApp" style="width: 100%; background: #2563EB; border: none; color: #FFF; padding: 16px; border-radius: 16px; font-size: 14px; font-weight: 800; cursor: pointer;">UNLOCK APP</button>
                <div style="font-size: 11px; color: #64748B; margin-top: 14px;">Default Security PIN: 1234</div>
            </div>
        `;

        document.body.appendChild(overlay);

        const input = document.getElementById('pinInputField');
        const unlockBtn = document.getElementById('btnUnlockApp');
        const errorEl = document.getElementById('pinErrorMsg');

        function verifyPin() {
            const val = input.value.trim();
            if (val === savedPin) {
                sessionStorage.setItem('symtotrack_unlocked', 'true');
                overlay.remove();
            } else {
                errorEl.innerText = "❌ Incorrect Security PIN. Try again.";
                input.value = "";
                input.focus();
            }
        }

        unlockBtn.addEventListener('click', verifyPin);
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') verifyPin();
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', checkAppLock);
    } else {
        checkAppLock();
    }
})();
