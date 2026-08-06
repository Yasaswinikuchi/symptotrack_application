// ml.js - TensorFlow.js and IndexedDB (Dexie) Setup

// 1. Initialize Dexie (IndexedDB)
const db = new Dexie('SymtoTrackDB');
db.version(1).stores({
    symptoms: '++id, symptom_text, severity, timestamp',
    user: 'id, name, email'
});
console.log("IndexedDB (Dexie) initialized for offline caching.");

// 2. TensorFlow.js Integration
async function loadModel() {
    try {
        console.log("Initializing TensorFlow.js for Offline ML inference...");
        // Placeholder for loading a pre-trained model:
        // const model = await tf.loadLayersModel('model/model.json');
        console.log("TensorFlow.js model initialized successfully.");
    } catch (e) {
        console.error("Failed to load TF model", e);
    }
}

// Service worker registration
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js').then(registration => {
            console.log('SW registered: ', registration.scope);
        }).catch(registrationError => {
            console.log('SW registration failed: ', registrationError);
        });
    });
}

loadModel();
