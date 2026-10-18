package com.today.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvTest {

    @Test
    fun `parses simple rows`() {
        val rows = Csv.parse("a,b,c\n1,2,3")

        assertEquals(listOf(listOf("a", "b", "c"), listOf("1", "2", "3")), rows)
    }

    @Test
    fun `parses quoted field containing delimiter`() {
        val rows = Csv.parse("name,note\nParasite,\"drama, tegang\"")

        assertEquals("drama, tegang", rows[1][1])
    }

    @Test
    fun `parses escaped quotes`() {
        val rows = Csv.parse("note\n\"dia bilang \"\"oke\"\"\"")

        assertEquals("dia bilang \"oke\"", rows[1][0])
    }

    @Test
    fun `parses newline inside quoted field`() {
        val rows = Csv.parse("note\n\"baris satu\nbaris dua\"")

        assertEquals(2, rows.size)
        assertEquals("baris satu\nbaris dua", rows[1][0])
    }

    @Test
    fun `handles crlf line endings`() {
        val rows = Csv.parse("a,b\r\n1,2\r\n")

        assertEquals(listOf("a", "b"), rows[0])
        assertEquals(listOf("1", "2"), rows[1])
    }

    @Test
    fun `skips blank lines`() {
        val rows = Csv.parse("a,b\n\n1,2\n")

        assertEquals(2, rows.size)
    }

    @Test
    fun `escape quotes only when needed`() {
        assertEquals("plain", Csv.escape("plain"))
        assertEquals("\"a,b\"", Csv.escape("a,b"))
        assertEquals("\"say \"\"hi\"\"\"", Csv.escape("say \"hi\""))
    }

    @Test
    fun `row escapes each field`() {
        assertEquals("a,\"b,c\"", Csv.row(listOf("a", "b,c")))
    }
}
