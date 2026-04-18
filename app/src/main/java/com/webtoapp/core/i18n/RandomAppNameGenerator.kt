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
    private val chinesePrefixes = listOf(
        "小", "大", "快", "智", "云", "新", "超", "酷", "妙", "神",
        "精", "万", "美", "乐", "易", "轻", "精", "巧", "火", "飞",
        "光", "星", "月", "雷", "风", "电", "金", "银", "玉", "宝",
        "梦", "天", "地", "海", "山", "林", "泉", "雪", "雨", "露",
        "红", "蓝", "绿", "紫", "青", "白", "黑", "橙", "粉", "灰"
    )
    
    private val chineseSuffixes = listOf(
        "助手", "工具", "宝", "通", "达", "星", "盒", "管家", "精灵", "魔法",
        "帮手", "大师", "侠", "神", "客", "管", "方", "统", "乐园", "向导",
        "精灵", "天使", "小精", "小宝", "宝贝", "小伙伴", "管家", "助理", "笔记", "记录",
        "空间", "世界", "宇宙", "星球", "银河", "统计", "分析", "探索", "发现", "秘密",
        "路径", "桥梁", "通道", "入口", "窗口", "门户", "平台", "中心", "基地", "站"
    )
    
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
    
    // ==================== ====================
    private val arabicPrefixes = listOf(
        "السريع", "الذكي", "السهل", "الخارق", "السحري", "الفائق", "المتقدم", "الجديد", "الأقصى", "الأول",
        "البرق", "الصاروخ", "الخفيف", "المباشر", "الفوري", "السريع", "العاجل", "المنطلق", "المندفع", "الخاطف",
        "القوي", "الضخم", "العملاق", "المفرط", "الشامل", "المتعدد", "المتنوع", "الرقمي", "الإلكتروني", "التقني",
        "النجم", "المضيء", "القمري", "الشمسي", "الكوني", "المجري", "السماوي", "العلوي", "السحابي", "الهوائي",
        "الأزرق", "الأحمر", "الأخضر", "الذهبي", "الفضي", "البلوري", "الماسي", "اللؤلؤي", "الياقوتي", "الزمردي"
    )
    
    private val arabicSuffixes = listOf(
        "التطبيق", "الأداة", "المجموعة", "الصندوق", "المركز", "المختبر", "المحترف", "المنطلق", "الآن", "الواحد",
        "المساعد", "الخبير", "العبقري", "الساحر", "المتخصص", "المرشد", "الرفيق", "الصديق", "الزميل", "الشريك",
        "الفضاء", "العالم", "المنطقة", "الأرض", "المملكة", "الكرة", "الميدان", "الساحة", "الاستوديو", "الورشة",
        "الرابط", "الموصل", "الجسر", "الطريق", "المسار", "البوابة", "الباب", "البورتال", "القناة", "التدفق",
        "القاعدة", "النقطة", "القلب", "الموقع", "المكان", "الموضع", "الموقف", "المكتب", "اللوحة", "الوسادة"
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
            AppLanguage.CHINESE -> generateChinese()
            AppLanguage.ENGLISH -> generateEnglish()
            AppLanguage.ARABIC -> generateArabic()
            AppLanguage.HINDI -> generateHindi()
        }
    }
    
    /**
     * Note.
     */
    fun generate(language: AppLanguage): String {
        return when (language) {
            AppLanguage.CHINESE -> generateChinese()
            AppLanguage.ENGLISH -> generateEnglish()
            AppLanguage.ARABIC -> generateArabic()
            AppLanguage.HINDI -> generateHindi()
        }
    }
    
    private fun generateChinese(): String {
        val prefix = chinesePrefixes[Random.nextInt(chinesePrefixes.size)]
        val suffix = chineseSuffixes[Random.nextInt(chineseSuffixes.size)]
        return prefix + suffix
    }
    
    private fun generateEnglish(): String {
        val prefix = englishPrefixes[Random.nextInt(englishPrefixes.size)]
        val suffix = englishSuffixes[Random.nextInt(englishSuffixes.size)]
        return prefix + suffix
    }
    
    private fun generateArabic(): String {
        val prefix = arabicPrefixes[Random.nextInt(arabicPrefixes.size)]
        val suffix = arabicSuffixes[Random.nextInt(arabicSuffixes.size)]
        return "$prefix $suffix"
    }

    private fun generateHindi(): String {
        val prefix = hindiPrefixes[Random.nextInt(hindiPrefixes.size)]
        val suffix = hindiSuffixes[Random.nextInt(hindiSuffixes.size)]
        return "$prefix $suffix"
    }
}
