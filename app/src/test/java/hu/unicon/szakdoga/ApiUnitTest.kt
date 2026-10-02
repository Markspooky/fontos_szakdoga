package hu.unicon.szakdoga

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.unicon.szakdoga.model.Category
import hu.unicon.szakdoga.model.Document
import hu.unicon.szakdoga.model.Folder
import hu.unicon.szakdoga.model.Material
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ApiUnitTest {

    private val gson = Gson()

    @Test
    fun testMaterialJsonParsing() {
        val json = """
            {
                "id": 101,
                "categoryId": 1,
                "categoryName": "Varrócérna",
                "name": "Aman Gütermann 120",
                "skuCode": "GUT-120-RED",
                "details": "Piros varrócérna, 1000m",
                "lastUpdate": "2025-01-15T10:00:00"
            }
        """.trimIndent()

        val material = gson.fromJson(json, Material::class.java)

        assertNotNull(material)
        assertEquals(101L, material.id)
        assertEquals("Aman Gütermann 120", material.name)
        assertEquals("GUT-120-RED", material.skuCode)
        assertEquals("Varrócérna", material.categoryName)
        assertEquals("Piros varrócérna, 1000m", material.details)
        assertEquals("2025-01-15T10:00:00", material.lastUpdate)
    }

    @Test
    fun testMaterialListJsonParsing() {
        val jsonList = """
            [
                {"id": 1, "name": "Cérna A", "skuCode": "A1"},
                {"id": 2, "name": "Cérna B", "skuCode": "B2"}
            ]
        """.trimIndent()

        val type = object : TypeToken<List<Material>>() {}.type
        val materials: List<Material> = gson.fromJson(jsonList, type)

        assertEquals(2, materials.size)
        assertEquals("Cérna A", materials[0].name)
        assertEquals("Cérna B", materials[1].name)
    }

    @Test
    fun testCategoryJsonParsing() {
        val json = """
            {
                "id": 1,
                "name": "Varrócérna",
                "description": "Különböző vastagságú varrócérnák"
            }
        """.trimIndent()

        val category = gson.fromJson(json, Category::class.java)

        assertNotNull(category)
        assertEquals(1L, category.id)
        assertEquals("Varrócérna", category.name)
        assertEquals("Különböző vastagságú varrócérnák", category.description)
    }

    @Test
    fun testFolderJsonParsing() {
        val json = """
            {
                "id": 5,
                "name": "Tervek 2025",
                "parentId": 1
            }
        """.trimIndent()

        val folder = gson.fromJson(json, Folder::class.java)

        assertNotNull(folder)
        assertEquals(5L, folder.id)
        assertEquals("Tervek 2025", folder.name)
        assertEquals(1L, folder.parentId)
    }

    @Test
    fun testRootFolderJsonParsingWithoutParent() {
        val json = """
            {
                "id": 1,
                "name": "Gyökér Mappa"
            }
        """.trimIndent()

        val folder = gson.fromJson(json, Folder::class.java)

        assertNotNull(folder)
        assertEquals(1L, folder.id)
        assertNull(folder.parentId)
    }

    @Test
    fun testDocumentJsonParsing() {
        val json = """
            {
                "id": 42,
                "folderId": 5,
                "title": "Szabásminta 01",
                "fileName": "szabasminta_01.pdf",
                "storagePath": "/pdfs/2025/szabasminta_01.pdf",
                "fileSize": 2048576,
                "uploadDate": "2025-02-01T12:00:00"
            }
        """.trimIndent()

        val doc = gson.fromJson(json, Document::class.java)

        assertNotNull(doc)
        assertEquals(42L, doc.id)
        assertEquals("Szabásminta 01", doc.title)
        assertEquals("szabasminta_01.pdf", doc.fileName)
        assertEquals("/pdfs/2025/szabasminta_01.pdf", doc.storagePath)
        assertEquals(2048576L, doc.fileSize)
    }
}
