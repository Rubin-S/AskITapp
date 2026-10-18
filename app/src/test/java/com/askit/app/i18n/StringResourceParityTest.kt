package com.askit.app.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Every translatable English string has a Tamil one and vice versa, in main and in demo. */
class StringResourceParityTest {
    private fun moduleFile(path: String): File =
        listOf(File(path), File("app", path)).firstOrNull(File::exists) ?: error("Missing $path")

    private fun translatableKeys(path: String): Set<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(moduleFile(path))
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).map { nodes.item(it) }
            .filter { it.attributes.getNamedItem("translatable")?.nodeValue != "false" }
            .mapTo(sortedSetOf()) { it.attributes.getNamedItem("name").nodeValue }
    }

    private fun assertParity(sourceSet: String) {
        val english = translatableKeys("src/$sourceSet/res/values/strings.xml")
        val tamil = translatableKeys("src/$sourceSet/res/values-ta/strings.xml")
        assertTrue("no strings found in $sourceSet", english.isNotEmpty())
        assertEquals("English keys missing in Tamil ($sourceSet)", emptySet<String>(), english - tamil)
        assertEquals("Tamil keys missing in English ($sourceSet)", emptySet<String>(), tamil - english)
    }

    @Test fun mainStringsHaveTamilParity() = assertParity("main")

    @Test fun demoStringsHaveTamilParity() = assertParity("demo")
}
