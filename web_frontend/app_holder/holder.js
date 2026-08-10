/**
 * APPHOLDER - Device Mockup & Frame Container Studio Logic
 * Handles device toggles, orientation, color finishes, and live URL previews
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- State ---
    const state = {
        device: 'iphone-15-pro',
        color: 'titanium',
        orientation: 'portrait',
        canvasTheme: 'dark',
        url: '../vitalis_health_dashboard/index.html'
    };

    // --- DOM Elements ---
    const deviceWrapper = document.getElementById('device-wrapper');
    const appIframe = document.getElementById('app-iframe');
    const deviceSelector = document.getElementById('device-selector');
    const colorPicker = document.getElementById('color-picker');
    const btnRotate = document.getElementById('btn-rotate');
    const btnTheme = document.getElementById('btn-theme');
    const btnReload = document.getElementById('btn-reload');
    const appUrlInput = document.getElementById('app-url-input');
    const btnLoadUrl = document.getElementById('btn-load-url');
    const statusTime = document.getElementById('status-time');
    const screenDimText = document.getElementById('screen-dim-text');

    // Dimensions Map
    const dimMap = {
        'iphone-15-pro': '393 x 852 px',
        'galaxy-s24': '400 x 840 px',
        'ipad-pro': '768 x 980 px',
        'full-responsive': '1200 x 750 px'
    };

    // --- Clock Manager ---
    function updateStatusClock() {
        const now = new Date();
        let hours = now.getHours();
        const minutes = now.getMinutes().toString().padStart(2, '0');
        hours = hours % 12 || 12;
        statusTime.textContent = `${hours}:${minutes}`;
    }
    setInterval(updateStatusClock, 1000);
    updateStatusClock();

    // --- Device Switching ---
    deviceSelector.addEventListener('click', (e) => {
        if (e.target.classList.contains('ctrl-btn')) {
            deviceSelector.querySelectorAll('.ctrl-btn').forEach(btn => btn.classList.remove('active'));
            e.target.classList.add('active');

            const device = e.target.dataset.device;
            state.device = device;
            applyDeviceState();
        }
    });

    // --- Color Switching ---
    colorPicker.addEventListener('click', (e) => {
        if (e.target.classList.contains('color-dot')) {
            colorPicker.querySelectorAll('.color-dot').forEach(dot => dot.classList.remove('active'));
            e.target.classList.add('active');

            const color = e.target.dataset.color;
            state.color = color;
            applyDeviceState();
        }
    });

    // --- Orientation Switch ---
    btnRotate.addEventListener('click', () => {
        state.orientation = state.orientation === 'portrait' ? 'landscape' : 'portrait';
        applyDeviceState();
    });

    // --- Canvas Theme Toggle ---
    btnTheme.addEventListener('click', () => {
        if (document.body.classList.contains('light-canvas')) {
            document.body.classList.remove('light-canvas');
            document.body.classList.add('dark-canvas');
            state.canvasTheme = 'dark';
        } else {
            document.body.classList.remove('dark-canvas');
            document.body.classList.add('light-canvas');
            state.canvasTheme = 'light';
        }
    });

    // --- Reload Iframe ---
    btnReload.addEventListener('click', () => {
        appIframe.src = appIframe.src;
    });

    // --- Load URL Logic ---
    btnLoadUrl.addEventListener('click', () => {
        const inputUrl = appUrlInput.value.trim();
        if (inputUrl) {
            appIframe.src = inputUrl;
            state.url = inputUrl;
        }
    });

    // Preset Chips
    document.querySelectorAll('.chip-preset').forEach(chip => {
        chip.addEventListener('click', () => {
            const presetUrl = chip.dataset.url;
            appUrlInput.value = presetUrl;
            appIframe.src = presetUrl;
            state.url = presetUrl;
        });
    });

    // --- Apply Overall Device Class State ---
    function applyDeviceState() {
        deviceWrapper.className = `device-frame ${state.device} ${state.color} ${state.orientation}`;
        screenDimText.textContent = dimMap[state.device] || 'Custom';
    }

    // Initialize
    applyDeviceState();
});
