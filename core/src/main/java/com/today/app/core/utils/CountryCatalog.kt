package com.today.app.core.utils

object CountryCatalog {

    private val names = mapOf(
        "AR" to "Argentina",
        "AT" to "Austria",
        "AU" to "Australia",
        "BE" to "Belgia",
        "BR" to "Brasil",
        "CA" to "Kanada",
        "CH" to "Swiss",
        "CL" to "Cile",
        "CN" to "Tiongkok",
        "CO" to "Kolombia",
        "CZ" to "Ceko",
        "DE" to "Jerman",
        "DK" to "Denmark",
        "EG" to "Mesir",
        "ES" to "Spanyol",
        "FI" to "Finlandia",
        "FR" to "Prancis",
        "GB" to "Inggris",
        "GR" to "Yunani",
        "HK" to "Hong Kong",
        "HU" to "Hungaria",
        "ID" to "Indonesia",
        "IE" to "Irlandia",
        "IL" to "Israel",
        "IN" to "India",
        "IR" to "Iran",
        "IS" to "Islandia",
        "IT" to "Italia",
        "JP" to "Jepang",
        "KR" to "Korea Selatan",
        "KP" to "Korea Utara",
        "MX" to "Meksiko",
        "MY" to "Malaysia",
        "NL" to "Belanda",
        "NO" to "Norwegia",
        "NZ" to "Selandia Baru",
        "PH" to "Filipina",
        "PL" to "Polandia",
        "PT" to "Portugal",
        "RO" to "Rumania",
        "RU" to "Rusia",
        "SE" to "Swedia",
        "SG" to "Singapura",
        "TH" to "Thailand",
        "TR" to "Turki",
        "TW" to "Taiwan",
        "UA" to "Ukraina",
        "US" to "Amerika Serikat",
        "VN" to "Vietnam",
        "ZA" to "Afrika Selatan"
    )

    fun name(code: String): String {
        val key = code.trim().uppercase()
        if (key.isEmpty()) return ""
        return names[key] ?: key
    }

    fun namesFromCsv(csv: String): List<String> =
        csv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .map { name(it) }
}
