package com.example.rota_rapida.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rota_rapida.data.dto.RotaData
import com.example.rota_rapida.data.mapper.toRota
import com.example.rota_rapida.data.mapper.toRotaData
import com.example.rota_rapida.domain.model.Rota
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first // 👈 ADICIONE ESTA LINHA
import kotlinx.coroutines.flow.map   // 👈 ADICIONE ESTA LINHA

// Cria o DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rotas_datastore")

@Singleton
class LocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson // Hilt vai injetar o Gson que você definiu no AppModule
) {
    private val KEY_ROTAS = stringPreferencesKey("rotas_list_json")

    // Salva a lista inteira de DTOs como um único JSON
    private suspend fun salvarLista(rotas: List<RotaData>) {
        val json = gson.toJson(rotas)
        context.dataStore.edit { preferences ->
            preferences[KEY_ROTAS] = json
        }
    }

    // Lê o JSON e converte de volta para a lista de DTOs
    private fun getRotasDataFlow(): Flow<List<RotaData>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[KEY_ROTAS] ?: "[]"
            val type = object : TypeToken<List<RotaData>>() {}.type
            gson.fromJson<List<RotaData>>(json, type) ?: emptyList()
        }
    }

    // --- MÉTODOS PÚBLICOS (Iguais a antes, mas agora usam DataStore) ---

    fun getRotasFlow(): Flow<List<Rota>> {
        return getRotasDataFlow().map { list -> list.map { it.toRota() } }
    }

    suspend fun saveRota(rota: Rota) {
        val dto = rota.toRotaData()
        val listaAtual = getRotasDataFlow().first() // Pega o valor atual
        val novaLista = listaAtual.filterNot { it.id == dto.id } + dto
        salvarLista(novaLista)
    }

    suspend fun deleteRota(rotaId: String) {
        val listaAtual = getRotasDataFlow().first()
        val novaLista = listaAtual.filterNot { it.id == rotaId }
        salvarLista(novaLista)
    }
}