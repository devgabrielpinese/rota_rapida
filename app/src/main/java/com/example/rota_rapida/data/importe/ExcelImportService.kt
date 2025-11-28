package com.example.rota_rapida.data.importe

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import javax.inject.Inject

data class ExcelStop(
    val atId: String?,
    val spxTn: String?,
    val destinationAddress: String?,
    val bairro: String?,
    val city: String?,
    val zipcode: String?,
    val latitude: Double?,
    val longitude: Double?
)

class ExcelImportService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Importa a planilha de forma TOLERANTE aos nomes das colunas.
     * A planilha não precisa mudar – o código tenta achar as colunas
     * por vários nomes possíveis e também por "contém texto".
     */
    suspend fun importarExcel(uri: Uri): List<ExcelStop> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<ExcelStop>()

        context.contentResolver.openInputStream(uri).use { input ->
            if (input != null) {
                WorkbookFactory.create(input).use { wb ->
                    val sheet = wb.getSheetAt(0)
                    val headerRow = sheet.getRow(0) ?: return@use

                    // Mapeamento de colunas: texto do cabeçalho (como string) -> índice
                    val colMap = mutableMapOf<String, Int>()
                    for (cell in headerRow) {
                        val text = cell.toString().trim()
                        if (text.isNotEmpty()) {
                            colMap[text] = cell.columnIndex
                        }
                    }

                    // --------- NOVO: função para achar colunas de forma flexível ---------
                    fun findColumnIndex(possibleNames: List<String>): Int? {
                        if (colMap.isEmpty()) return null

                        // normaliza: tudo em lower case
                        val normalized = colMap.mapKeys { it.key.trim().lowercase() }

                        // 1) tentativa por igualdade exata (ignorando maiúsc/minúsc)
                        for (name in possibleNames) {
                            val key = name.trim().lowercase()
                            normalized[key]?.let { return it }
                        }

                        // 2) tentativa por "contém" (para cabeçalhos mais longos)
                        for ((key, idx) in normalized) {
                            if (possibleNames.any { cand ->
                                    key.contains(cand.trim().lowercase())
                                }
                            ) {
                                return idx
                            }
                        }

                        return null
                    }

                    // Índices das colunas desejadas (lista de apelidos pra cada uma)
                    val idxAddress = findColumnIndex(
                        listOf(
                            "destination address",
                            "address",
                            "endereço",
                            "endereco",
                            "endereco destino",
                            "destination"
                        )
                    )

                    val idxLat = findColumnIndex(
                        listOf(
                            "latitude",
                            "lat"
                        )
                    )

                    val idxLng = findColumnIndex(
                        listOf(
                            "longitude",
                            "long",
                            "lng"
                        )
                    )

                    val idxAtId = findColumnIndex(
                        listOf(
                            "at id",
                            "atid"
                        )
                    )

                    val idxSpxTn = findColumnIndex(
                        listOf(
                            "spx tn",
                            "spxtn",
                            "spx_tn"
                        )
                    )

                    val idxBairro = findColumnIndex(
                        listOf(
                            "bairro",
                            "neighborhood",
                            "neighbourhood"
                        )
                    )

                    val idxCity = findColumnIndex(
                        listOf(
                            "city",
                            "cidade"
                        )
                    )

                    val idxZip = findColumnIndex(
                        listOf(
                            "zipcode/postal code",
                            "zipcode",
                            "postal code",
                            "cep"
                        )
                    )

                    // Itera linhas de dados (a partir da linha 1)
                    for (row in sheet.drop(1)) {
                        if (row == null) continue

                        val address = getCellString(row, idxAddress)
                        // Se não tem endereço, ignora a linha
                        if (address.isNullOrBlank()) continue

                        val lat = getCellDouble(row, idxLat)
                        val lng = getCellDouble(row, idxLng)

                        lista += ExcelStop(
                            atId = getCellString(row, idxAtId),
                            spxTn = getCellString(row, idxSpxTn),
                            destinationAddress = address,
                            bairro = getCellString(row, idxBairro),
                            city = getCellString(row, idxCity),
                            zipcode = getCellString(row, idxZip),
                            latitude = lat,
                            longitude = lng
                        )
                    }
                }
            }
        }
        lista
    }

    private fun getCellString(row: Row, colIndex: Int?): String? {
        if (colIndex == null) return null
        val cell = row.getCell(colIndex) ?: return null
        return cell.toString().trim().ifBlank { null }
    }

    private fun getCellDouble(row: Row, colIndex: Int?): Double? {
        if (colIndex == null) return null
        val cell: Cell = row.getCell(colIndex) ?: return null
        return try {
            cell.numericCellValue
        } catch (e: Exception) {
            // Tenta ler como string e converter (caso venha " -23,555 " etc.)
            cell.toString().replace(",", ".").trim().toDoubleOrNull()
        }
    }
}
