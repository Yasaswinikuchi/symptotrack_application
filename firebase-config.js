// SymptoTrack Pro — Firebase Configuration
// Replace the values below with your own Firebase project config
// Get these from: Firebase Console → Your Project → Project Settings → Web App

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-app.js";
import { getFirestore, collection, addDoc, getDocs, doc, setDoc, deleteDoc, onSnapshot, query, orderBy } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-firestore.js";
import { getAuth, signInAnonymously, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.12.0/firebase-auth.js";

// ─── YOUR FIREBASE CONFIG ──────────────────────────────────────────────────
// TODO: Replace with your Firebase project values
const firebaseConfig = {
    apiKey:            "YOUR_API_KEY",
    authDomain:        "YOUR_PROJECT.firebaseapp.com",
    projectId:         "YOUR_PROJECT_ID",
    storageBucket:     "YOUR_PROJECT.appspot.com",
    messagingSenderId: "YOUR_SENDER_ID",
    appId:             "YOUR_APP_ID"
};
// ──────────────────────────────────────────────────────────────────────────

const FIREBASE_CONFIGURED = firebaseConfig.apiKey !== "YOUR_API_KEY";

let app = null, db = null, auth = null, currentUserId = null;

if (FIREBASE_CONFIGURED) {
    app  = initializeApp(firebaseConfig);
    db   = getFirestore(app);
    auth = getAuth(app);

    // Sign in anonymously so every user gets a unique ID
    signInAnonymously(auth).catch(console.error);
    onAuthStateChanged(auth, (user) => {
        if (user) { currentUserId = user.uid; }
    });
}

// ─── Universal Storage API ─────────────────────────────────────────────────
// Falls back to localStorage when Firebase is not configured

export async function saveData(collection_name, docId, data) {
    if (FIREBASE_CONFIGURED && db && currentUserId) {
        await setDoc(doc(db, "users", currentUserId, collection_name, docId), data);
    } else {
        const store = JSON.parse(localStorage.getItem(`st_${collection_name}`) || "{}");
        store[docId] = { ...data, _id: docId, _updated: new Date().toISOString() };
        localStorage.setItem(`st_${collection_name}`, JSON.stringify(store));
    }
}

export async function loadAll(collection_name) {
    if (FIREBASE_CONFIGURED && db && currentUserId) {
        const snap = await getDocs(collection(db, "users", currentUserId, collection_name));
        return snap.docs.map(d => ({ _id: d.id, ...d.data() }));
    } else {
        const store = JSON.parse(localStorage.getItem(`st_${collection_name}`) || "{}");
        return Object.values(store);
    }
}

export async function deleteData(collection_name, docId) {
    if (FIREBASE_CONFIGURED && db && currentUserId) {
        await deleteDoc(doc(db, "users", currentUserId, collection_name, docId));
    } else {
        const store = JSON.parse(localStorage.getItem(`st_${collection_name}`) || "{}");
        delete store[docId];
        localStorage.setItem(`st_${collection_name}`, JSON.stringify(store));
    }
}

export function isFirebaseActive() { return FIREBASE_CONFIGURED && !!db; }
export function getCurrentUserId() { return currentUserId; }
