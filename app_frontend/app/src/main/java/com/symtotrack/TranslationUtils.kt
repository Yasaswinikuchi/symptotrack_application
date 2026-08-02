package com.symtotrack

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object TranslationUtils {

    fun getLangCode(langStr: String): String {
        val lower = langStr.lowercase()
        return when {
            lower.contains("telugu") || lower.contains("తెలుగు") -> "te"
            lower.contains("hindi") || lower.contains("हिंदी") -> "hi"
            lower.contains("tamil") || lower.contains("தமிழ்") -> "ta"
            lower.contains("kannada") || lower.contains("ಕನ್ನಡ") -> "kn"
            lower.contains("malayalam") || lower.contains("മലയാളം") -> "ml"
            lower.contains("bengali") || lower.contains("বাংলা") -> "bn"
            lower.contains("marathi") || lower.contains("मराठी") -> "mr"
            lower.contains("gujarati") || lower.contains("ગુજરાતી") -> "gu"
            lower.contains("punjabi") || lower.contains("ਪੰਜਾਬੀ") -> "pa"
            lower.contains("urdu") || lower.contains("اردو") -> "ur"
            lower.contains("spanish") || lower.contains("español") -> "es"
            lower.contains("french") || lower.contains("français") -> "fr"
            lower.contains("german") || lower.contains("deutsch") -> "de"
            lower.contains("mandarin") || lower.contains("chinese") || lower.contains("中文") -> "zh"
            lower.contains("japanese") || lower.contains("日本語") -> "ja"
            lower.contains("korean") || lower.contains("한국어") -> "ko"
            lower.contains("arabic") || lower.contains("العربية") -> "ar"
            lower.contains("russian") || lower.contains("русский") -> "ru"
            lower.contains("portuguese") || lower.contains("português") -> "pt"
            lower.contains("italian") || lower.contains("italiano") -> "it"
            else -> "en"
        }
    }

    fun updateLocale(context: Context): Context {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val langStr = prefs.getString("app_language", "English") ?: "English"
        val langCode = getLangCode(langStr)

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    // ── Translation Dictionaries ──────────────────────────────────────────────
    private val teluguMap = mapOf(
        "SymptoTrack" to "సింప్టోట్రాక్ Pro",
        "Good morning" to "శుభోదయం",
        "Good afternoon" to "శుభ మధ్యాహ్నం",
        "Good evening" to "శుభ సాయంత్రం",
        "Hello" to "నమస్కారం",
        "Welcome Back" to "తిరిగి స్వాగతం",
        "Enter your details to access your health dashboard." to "మీ ఆరోగ్య డాష్‌బోర్డ్‌ను యాక్సెస్ చేయడానికి మీ వివరాలను నమోదు చేయండి.",
        "Healthcare Features" to "ఆరోగ్య సంరక్షణ ఫీచర్లు",
        "Enter Symptoms" to "లక్షణాలను నమోదు చేయండి",
        "Check Symptoms" to "లక్షణాలను తనిఖీ చేయండి",
        "AI Analysis" to "AI విశ్లేషణ",
        "Scan Image" to "చిత్రాన్ని స్కాన్ చేయండి",
        "Prescription/Lab" to "ప్రిస్క్రిప్షన్ / ల్యాబ్ స్కాన్",
        "Voice Symptoms" to "వాయిస్ ద్వారా లక్షణాలు",
        "Speak Symptoms" to "నోటితో మాట్లాడి చెప్పండి",
        "Emergency Services" to "అత్యవసర సేవలు (112)",
        "Instant SOS Dial" to "తక్షణ అత్యవసర ఫోన్ 112",
        "Medicine Reminders" to "మందుల జ్ఞాపికలు",
        "Pill Schedules" to "మాత్రల షెడ్యూల్",
        "Family Profiles" to "కుటుంబ ఆరోగ్య ప్రొఫైల్స్",
        "Health Records" to "ఆరోగ్య రికార్డులు",
        "Find Hospitals" to "సమీప ఆసుపత్రులు",
        "Nearby Clinics" to "దగ్గరలోని క్లినిక్‌లు",
        "Body Heatmap" to "శరీర హీట్‌మ్యాప్",
        "Visual Symptoms" to "శరీర భాగాల లక్షణాలు",
        "Symptom History" to "లక్షణాల చరిత్ర",
        "Past Reports" to "గత నివేదికలు",
        "AI Health Assistant" to "AI ఆరోగ్య సహాయకుడు",
        "Chat Support" to "చాట్ మద్దతు",
        "No Active Appointment" to "అపాయింట్‌మెంట్ బుక్ కాలేదు",
        "Book a clinic visit to get a live queue token" to "లైవ్ క్యూ టోకెన్ పొందడానికి క్లినిక్ విజిట్ బుక్ చేయండి",
        "Book Appointment & Get Token" to "+ అపాయింట్‌మెంట్ బుక్ చేయండి",
        "Book Now" to "ఇప్పుడే బుక్ చేయండి",
        "LIVE Clinic Queue" to "🏥 లైవ్ క్లినిక్ క్యూ",
        "NOW SERVING" to "ప్రస్తుతం పిలుస్తున్నది",
        "YOUR TOKEN" to "మీ టోకెన్ సంఖ్య",
        "AHEAD OF YOU" to "మీ కంటే ముందున్నవారు",
        "Queue Progress" to "క్యూ పురోగతి",
        "Heart Rate" to "గుండె వేగం",
        "Blood Pressure" to "రక్తపోటు",
        "Health Score" to "ఆరోగ్య స్కోర్",
        "Profile" to "ప్రొఫైల్",
        "Profile & Settings" to "ప్రొఫైల్ & సెట్టింగ్‌లు",
        "Personal Information" to "వ్యక్తిగత సమాచారం",
        "Medical Records" to "వైద్య నివేదికలు",
        "Privacy & Security" to "గోప్యత & భద్రత",
        "App Theme" to "యాప్ థీమ్",
        "App Language" to "యాప్ భాష",
        "Help & Support" to "సహాయం & మద్దతు",
        "Log Out" to "లాగ్ అవుట్"
    )

    private val hindiMap = mapOf(
        "Welcome Back" to "वापसी पर स्वागत है",
        "Healthcare Features" to "स्वास्थ्य सेवा विशेषताएं",
        "Enter Symptoms" to "लक्षण दर्ज करें",
        "Check Symptoms" to "लक्षण जांचें",
        "Scan Image" to "छवि स्कैन करें",
        "Voice Symptoms" to "आवाज से लक्षण बताएं",
        "Emergency Services" to "आपातकालीन सेवाएं (112)",
        "Medicine Reminders" to "दवा अनुस्मारक",
        "Family Profiles" to "पारिवारिक प्रोफ़ाइल",
        "Find Hospitals" to "अस्पताल खोजें",
        "Body Heatmap" to "शरीर का हीटमैप",
        "Symptom History" to "लक्षण इतिहास",
        "AI Health Assistant" to "एआई स्वास्थ्य सहायक",
        "Heart Rate" to "हृदय गति",
        "Blood Pressure" to "रक्तचाप",
        "Health Score" to "स्वास्थ्य स्कोर",
        "Profile & Settings" to "प्रोफ़ाइल और सेटिंग्स",
        "Personal Information" to "व्यक्तिगत जानकारी",
        "Medical Records" to "चिकित्सा रिकॉर्ड",
        "Privacy & Security" to "गोपनीयता और सुरक्षा",
        "App Theme" to "ऐप थीम",
        "App Language" to "ऐप भाषा",
        "Help & Support" to "सहायता और समर्थन",
        "Log Out" to "लॉग आउट"
    )

    fun translate(context: Context, text: String): String {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val langStr = prefs.getString("app_language", "English") ?: "English"
        val langCode = getLangCode(langStr)

        return when (langCode) {
            "te" -> teluguMap[text] ?: text
            "hi" -> hindiMap[text] ?: text
            else -> text
        }
    }
}
