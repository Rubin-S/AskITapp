package com.askit.app.auth

data class CountryCode(
    val countryName: String,
    val isoCode: String,
    val dialCode: String,
    val flagEmoji: String,
)

val defaultCountryCodes = listOf(
    CountryCode("India", "IN", "+91", "🇮🇳"),
    CountryCode("United States", "US", "+1", "🇺🇸"),
    CountryCode("United Kingdom", "GB", "+44", "🇬🇧"),
    CountryCode("United Arab Emirates", "AE", "+971", "🇦🇪"),
    CountryCode("Canada", "CA", "+1", "🇨🇦"),
    CountryCode("Australia", "AU", "+61", "🇦🇺"),
    CountryCode("Singapore", "SG", "+65", "🇸🇬"),
    CountryCode("Malaysia", "MY", "+60", "🇲🇾"),
    CountryCode("Germany", "DE", "+49", "🇩🇪"),
    CountryCode("France", "FR", "+33", "🇫🇷"),
    CountryCode("Saudi Arabia", "SA", "+966", "🇸🇦"),
    CountryCode("Qatar", "QA", "+974", "🇶🇦"),
    CountryCode("Oman", "OM", "+968", "🇴🇲"),
    CountryCode("Kuwait", "KW", "+965", "🇰🇼"),
    CountryCode("Japan", "JP", "+81", "🇯🇵"),
)
