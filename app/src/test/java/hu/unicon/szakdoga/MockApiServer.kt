package hu.unicon.szakdoga

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MockApiServer {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val port = 8080
            val serverSocket = ServerSocket(port)
            println("==================================================")
            println("  Oracle DB REST API Mock Server elindult!")
            println("  Cím: http://localhost:$port/api/")
            println("  Emulátorból: http://10.0.2.2:$port/api/")
            println("==================================================")

            while (true) {
                try {
                    val clientSocket = serverSocket.accept()
                    Thread { handleClient(clientSocket) }.start()
                } catch (e: Exception) {
                    println("Szerver hiba: ${e.message}")
                }
            }
        }

        private fun handleClient(socket: Socket) {
            try {
                socket.getInputStream().use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    val requestLine = reader.readLine() ?: return
                    val parts = requestLine.split(" ")
                    if (parts.size < 2) return
                    val pathWithQuery = parts[1]

                    println("Kérés érkezett: $pathWithQuery")

                    socket.getOutputStream().use { outputStream ->
                        when {
                            pathWithQuery.startsWith("/api/categories") -> {
                                val json = """[
                                    {"id": 1, "name": "Varrócérna", "description": "Magas minőségű varrócérnák"},
                                    {"id": 2, "name": "Hímzőcérna", "description": "Selymes hímzőcérnák"},
                                    {"id": 3, "name": "Kellék & Cipzár", "description": "Fém és műanyag cipzárak"}
                                ]"""
                                sendJsonResponse(outputStream, json)
                            }
                            pathWithQuery.startsWith("/api/materials") -> {
                                val search = getQueryParam(pathWithQuery, "search")?.lowercase()
                                val allMaterials = listOf(
                                    """{"id": 101, "categoryId": 1, "categoryName": "Varrócérna", "name": "Aman Gütermann 120", "skuCode": "GUT-120-RED", "details": "Piros varrócérna, 1000m orsó, 100% poliészter", "lastUpdate": "2025-02-15"}""",
                                    """{"id": 102, "categoryId": 1, "categoryName": "Varrócérna", "name": "Aman Gütermann 100", "skuCode": "GUT-100-BLK", "details": "Fekete varrócérna, 500m orsó, erősített szál", "lastUpdate": "2025-02-15"}""",
                                    """{"id": 103, "categoryId": 2, "categoryName": "Hímzőcérna", "name": "Madeira Rayon 40", "skuCode": "MAD-40-BLU", "details": "Kék hímzőcérna, 1000m, viszkóz hímzőszál", "lastUpdate": "2025-02-15"}""",
                                    """{"id": 104, "categoryId": 3, "categoryName": "Kellék & Cipzár", "name": "YKK Fém Cipzár 50cm", "skuCode": "YKK-50-MET", "details": "Ezüst fém fogazatú bontható cipzár", "lastUpdate": "2025-02-15"}"""
                                )
                                val filtered = if (search.isNullOrEmpty()) allMaterials else allMaterials.filter { it.lowercase().contains(search) }
                                sendJsonResponse(outputStream, "[${filtered.joinToString(",")}]")
                            }
                            pathWithQuery.startsWith("/api/folders") -> {
                                val parentId = getQueryParam(pathWithQuery, "parentId")
                                val json = when (parentId) {
                                    null, "", "null" -> """[
                                        {"id": 1, "name": "Tervek és Szabásminták", "parentId": null},
                                        {"id": 2, "name": "Műszaki Dokumentációk", "parentId": null}
                                    ]"""
                                    "1" -> """[
                                        {"id": 10, "name": "2025 Minta Kollekció", "parentId": 1}
                                    ]"""
                                    else -> "[]"
                                }
                                sendJsonResponse(outputStream, json)
                            }
                            pathWithQuery.contains("/download") -> {
                                val pdfBytes = createSamplePdfBytes()
                                sendPdfResponse(outputStream, pdfBytes)
                            }
                            pathWithQuery.startsWith("/api/documents") -> {
                                val json = """[
                                    {"id": 501, "folderId": 10, "title": "Cérna Specifikáció - Gütermann", "fileName": "cerna_specifikacio.pdf", "storagePath": "/pdf/cerna_specifikacio.pdf", "fileSize": 102400, "uploadDate": "2025-02-10"},
                                    {"id": 502, "folderId": 10, "title": "Kabát Szabásminta Terv", "fileName": "kabat_szabasminta.pdf", "storagePath": "/pdf/kabat_szabasminta.pdf", "fileSize": 204800, "uploadDate": "2025-02-12"}
                                ]"""
                                sendJsonResponse(outputStream, json)
                            }
                            else -> {
                                sendJsonResponse(outputStream, "[]")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Connection closed
            }
        }

        private fun getQueryParam(url: String, param: String): String? {
            val queryIdx = url.indexOf('?')
            if (queryIdx == -1) return null
            val query = url.substring(queryIdx + 1)
            for (pair in query.split("&")) {
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2 && parts[0] == param) {
                    return URLDecoder.decode(parts[1], "UTF-8")
                }
            }
            return null
        }

        private fun sendJsonResponse(outputStream: OutputStream, json: String) {
            val bytes = json.toByteArray(StandardCharsets.UTF_8)
            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json; charset=utf-8\r\n" +
                    "Content-Length: ${bytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
            outputStream.write(header.toByteArray(StandardCharsets.UTF_8))
            outputStream.write(bytes)
            outputStream.flush()
        }

        private fun sendPdfResponse(outputStream: OutputStream, pdfBytes: ByteArray) {
            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/pdf\r\n" +
                    "Content-Length: ${pdfBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
            outputStream.write(header.toByteArray(StandardCharsets.UTF_8))
            outputStream.write(pdfBytes)
            outputStream.flush()
        }

        private fun createSamplePdfBytes(): ByteArray {
            val pdfContent = """
                %PDF-1.4
                1 0 obj <</Type /Catalog /Pages 2 0 R>> endobj
                2 0 obj <</Type /Pages /Count 1 /Kids [3 0 R]>> endobj
                3 0 obj <</Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources <</Font <</F1 5 0 R>>>>>> endobj
                4 0 obj <</Length 65>> stream
                BT /F1 20 Tf 50 700 Td (Szakdolgozat Demo PDF Dokumentum) Tj ET
                endstream endobj
                5 0 obj <</Type /Font /Subtype /Type1 /BaseFont /Helvetica>> endobj
                xref
                0 6
                0000000000 65535 f 
                0000000009 00000 n 
                0000000058 00000 n 
                0000000115 00000 n 
                0000000238 00000 n 
                0000000353 00000 n 
                trailer <</Size 6 /Root 1 0 R>>
                startxref
                424
                %%EOF
            """.trimIndent()
            return pdfContent.toByteArray(StandardCharsets.UTF_8)
        }
    }
}
