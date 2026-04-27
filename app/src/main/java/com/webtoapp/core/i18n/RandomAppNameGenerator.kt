package com.webtoapp.core.i18n

import kotlin.random.Random

/**
 * Note.
 * 
 * Note.
 * Note.
 */
object RandomAppNameGenerator {
    

    
    // ==================== ====================
    private val englishPrefixes = listOf(
        "Quick", "Smart", "Easy", "Super", "Magic", "Ultra", "Pro", "Neo", "Max", "Prime",
        "Flash", "Turbo", "Swift", "Rapid", "Instant", "Fast", "Speed", "Zoom", "Rush", "Blitz",
        "Power", "Mega", "Giga", "Hyper", "Omni", "Multi", "Poly", "Meta", "Cyber", "Tech",
        "Star", "Nova", "Luna", "Solar", "Cosmic", "Galaxy", "Orbit", "Sky", "Cloud", "Air",
        "Dream", "Vision", "Mind", "Soul", "Spirit", "Heart", "Core", "Pure", "True", "Real",
        "Blue", "Red", "Green", "Gold", "Silver", "Crystal", "Diamond", "Pearl", "Ruby", "Jade",
        "Fire", "Ice", "Thunder", "Storm", "Wave", "Spark", "Flame", "Frost", "Wind", "Rain"
    )
    
    private val englishSuffixes = listOf(
        "App", "Tool", "Kit", "Box", "Hub", "Lab", "Pro", "Go", "Now", "One",
        "Helper", "Master", "Genius", "Wizard", "Expert", "Guide", "Buddy", "Pal", "Mate", "Friend",
        "Space", "World", "Zone", "Land", "Realm", "Sphere", "Field", "Arena", "Studio", "Works",
        "Link", "Connect", "Bridge", "Path", "Way", "Gate", "Door", "Portal", "Channel", "Stream",
        "Base", "Center", "Core", "Point", "Spot", "Place", "Site", "Desk", "Board", "Pad",
        "Flow", "Sync", "Track", "Pulse", "Beat", "Wave", "Loop", "Ring", "Spin", "Dash"
    )
    

    
    // ==================== HINDI ====================
    private val hindiPrefixes = listOf(
        "त्वरित", "स्मार्ट", "आसान", "सुपर", "जादुई", "अल्ट्रा", "प्रो", "नियो", "मैक्स", "प्राइम",
        "फ्लैश", "टर्बो", "स्विफ्ट", "रैपिड", "इंस्टेंट", "फास्ट", "स्पीड", "ज़ूम", "रश", "ब्लिट्ज",
        "पावर", "मेगा", "गीगा", "हाइपर", "ओम्नी", "मल्टी", "पॉली", "मेटा", "साइबर", "टेक",
        "स्टार", "नोवा", "लूना", "सोलर", "कोस्मिक", "गैलेक्सी", "ऑर्बिट", "स्काई", "क्लाउड", "एयर",
        "ड्रीम", "विज़न", "माइंड", "सोल", "स्पिरिट", "हार्ट", "कोर", "प्योर", "ट्रू", "रियल"
    )
    
    private val hindiSuffixes = listOf(
        "ऐप", "टूल", "किट", "बॉक्स", "हब", "लैब", "प्रो", "गो", "नाउ", "वन",
        "हेल्पर", "मास्टर", "जीनियस", "विज़ार्ड", "एक्सपर्ट", "गाइड", "बडी", "पाल", "मेट", "फ्रेंड",
        "स्पेस", "वर्ल्ड", "ज़ोन", "लैंड", "रेल्म", "स्फीयर", "फील्ड", "अखाड़ा", "स्टूडियो", "वर्क्स",
        "लिंक", "कनेक्ट", "ब्रिज", "पाथ", "वे", "गेट", "डोर", "पोर्टल", "चैनल", "स्ट्रीम",
        "बेस", "सेंटर", "कोर", "पॉइंट", "स्पॉट", "प्लेस", "साइट", "डेस्क", "बोर्ड", "पैड"
    )
    
    /**
     * Note.
     */
    fun generate(): String {
        return when (AppStringsProvider.currentLanguage) {
            AppLanguage.HINDI -> generateHindi()
            else -> generateEnglish()
        }
    }
    
    /**
     * Note.
     */
    fun generate(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> generateHindi()
            else -> generateEnglish()
        }
    }
    

    
    private fun generateEnglish(): String {
        val prefix = englishPrefixes[Random.nextInt(englishPrefixes.size)]
        val suffix = englishSuffixes[Random.nextInt(englishSuffixes.size)]
        return prefix + suffix
    }
    


    private fun generateHindi(): String {
        val prefix = hindiPrefixes[Random.nextInt(hindiPrefixes.size)]
        val suffix = hindiSuffixes[Random.nextInt(hindiSuffixes.size)]
        return "$prefix $suffix"
    }
}
