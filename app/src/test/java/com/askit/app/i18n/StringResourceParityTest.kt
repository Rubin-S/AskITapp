package com.askit.app.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Every translatable English string and plural has a Tamil counterpart, and Tamil has no orphans. */
class StringResourceParityTest {
    @Test fun mainResourcesHaveTamilParity() = assertParity("main")

    @Test fun demoResourcesHaveTamilParity() = assertParity("demo")

    private fun assertParity(sourceSet: String) {
        val english = translatableKeys("src/$sourceSet/res/values/strings.xml")
        val tamil = translatableKeys("src/$sourceSet/res/values-ta/strings.xml")
        assertTrue("no strings found in $sourceSet", english.isNotEmpty())
        assertEquals("English keys missing in Tamil ($sourceSet)", emptySet<String>(), english - tamil)
        assertEquals("Tamil keys missing in English ($sourceSet)", emptySet<String>(), tamil - english)
    }

    private fun translatableKeys(path: String): Set<String> {
        // Gradle runs unit tests from the module directory; an IDE may run them from the root.
        val file = listOf(File(path), File("app", path)).firstOrNull(File::exists) ?: error("Missing $path")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        return listOf("string", "plurals").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { nodes.item(it) as Element }
                .filter { it.getAttribute("translatable") != "false" }
                .map { "$tag/${it.getAttribute("name")}" }
        }.toSortedSet()
    }
}
