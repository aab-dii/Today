package com.today.app.social.data.mapper

import com.google.firebase.auth.FirebaseAuthException

internal fun Throwable.toFriendlyAuthMessage(): String =
    authMessageFor((this as? FirebaseAuthException)?.errorCode, message)

internal fun authMessageFor(errorCode: String?, detailMessage: String?): String {
    if (detailMessage?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true) {
        return "Metode masuk ini belum diaktifkan di Firebase Console."
    }
    return when (errorCode) {
        "ERROR_INVALID_EMAIL" -> "Format email tidak valid."
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email atau kata sandi salah."
        "ERROR_USER_NOT_FOUND" -> "Akun belum terdaftar."
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar. Coba masuk."
        "ERROR_WEAK_PASSWORD" -> "Kata sandi terlalu lemah (minimal 6 karakter)."
        "ERROR_OPERATION_NOT_ALLOWED" -> "Metode masuk ini belum diaktifkan di Firebase Console."
        "ERROR_USER_DISABLED" -> "Akun ini dinonaktifkan."
        "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi gagal. Periksa koneksi internetmu."
        else -> detailMessage?.takeIf { it.isNotBlank() } ?: "Terjadi kesalahan. Coba lagi."
    }
}
