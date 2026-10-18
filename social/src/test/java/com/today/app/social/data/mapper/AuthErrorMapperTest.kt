package com.today.app.social.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun `configuration not found maps to provider not enabled`() {
        val message = authMessageFor(
            errorCode = "ERROR_INTERNAL_ERROR",
            detailMessage = "An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]"
        )

        assertEquals("Metode masuk ini belum diaktifkan di Firebase Console.", message)
    }

    @Test
    fun `operation not allowed maps to provider not enabled`() {
        val message = authMessageFor("ERROR_OPERATION_NOT_ALLOWED", null)

        assertEquals("Metode masuk ini belum diaktifkan di Firebase Console.", message)
    }

    @Test
    fun `email already in use maps to friendly message`() {
        val message = authMessageFor("ERROR_EMAIL_ALREADY_IN_USE", null)

        assertEquals("Email sudah terdaftar. Coba masuk.", message)
    }

    @Test
    fun `wrong password maps to invalid credentials`() {
        val message = authMessageFor("ERROR_WRONG_PASSWORD", null)

        assertEquals("Email atau kata sandi salah.", message)
    }

    @Test
    fun `unknown error falls back to detail message`() {
        val message = authMessageFor("ERROR_SOMETHING", "detail asli")

        assertEquals("detail asli", message)
    }

    @Test
    fun `unknown error without detail falls back to generic message`() {
        val message = authMessageFor(null, null)

        assertEquals("Terjadi kesalahan. Coba lagi.", message)
    }
}
