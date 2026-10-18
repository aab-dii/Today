package com.today.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GenreCatalogTest {

    @Test
    fun `name returns label for known id`() {
        assertEquals("Action", GenreCatalog.name(28))
        assertEquals("Sci-Fi", GenreCatalog.name(878))
        assertEquals("Sci-Fi & Fantasy", GenreCatalog.name(10765))
    }

    @Test
    fun `name returns null for unknown id`() {
        assertNull(GenreCatalog.name(999999))
    }

    @Test
    fun `namesFromCsv maps known ids and skips unknown or blank`() {
        assertEquals(
            listOf("Action", "Sci-Fi"),
            GenreCatalog.namesFromCsv("28, 878")
        )
    }

    @Test
    fun `namesFromCsv ignores non numeric and unknown entries`() {
        assertEquals(
            listOf("Drama"),
            GenreCatalog.namesFromCsv("abc, 18, , 999999")
        )
    }

    @Test
    fun `namesFromCsv on empty string returns empty list`() {
        assertEquals(emptyList<String>(), GenreCatalog.namesFromCsv(""))
    }

    @Test
    fun `idForName is case insensitive and null for unknown`() {
        assertEquals(28, GenreCatalog.idForName("action"))
        assertEquals(878, GenreCatalog.idForName("Sci-Fi"))
        assertNull(GenreCatalog.idForName("Nope"))
    }

    @Test
    fun `idsCsvFromNames maps names back to ids and skips unknown`() {
        assertEquals("28, 878", GenreCatalog.idsCsvFromNames(listOf("Action", "Sci-Fi", "Nope")))
    }

    @Test
    fun `names and ids round trip`() {
        val csv = "28, 878, 18"
        assertEquals(csv, GenreCatalog.idsCsvFromNames(GenreCatalog.namesFromCsv(csv)))
    }
}
