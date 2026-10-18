package com.today.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CountryCatalogTest {

    @Test
    fun `maps known codes to names`() {
        assertEquals("Amerika Serikat", CountryCatalog.name("US"))
        assertEquals("Korea Selatan", CountryCatalog.name("KR"))
        assertEquals("Indonesia", CountryCatalog.name("ID"))
    }

    @Test
    fun `is case and whitespace insensitive`() {
        assertEquals("Jepang", CountryCatalog.name(" jp "))
    }

    @Test
    fun `falls back to uppercased code when unknown`() {
        assertEquals("ZZ", CountryCatalog.name("zz"))
    }

    @Test
    fun `empty code returns empty string`() {
        assertEquals("", CountryCatalog.name("   "))
    }

    @Test
    fun `namesFromCsv maps dedupes and skips blanks`() {
        assertEquals(
            listOf("Amerika Serikat", "Korea Selatan"),
            CountryCatalog.namesFromCsv("US, KR, US, , ")
        )
    }
}
