package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.model.Dado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SupabaseClient(context: Context) {

    private val prefs = context.getSharedPreferences("supabase_config", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getUrl(): String {
        val custom = prefs.getString("custom_url", "")?.trim().orEmpty()
        if (custom.isNotBlank()) return custom.removeSuffix("/")
        val configUrl = runCatching { BuildConfig.SUPABASE_URL }.getOrDefault("")
        return configUrl.trim().removeSuffix("/")
    }

    fun getAnonKey(): String {
        val custom = prefs.getString("custom_anon_key", "")?.trim().orEmpty()
        if (custom.isNotBlank()) return custom
        return runCatching { BuildConfig.SUPABASE_ANON_KEY }.getOrDefault("").trim()
    }

    fun saveCustomConfig(url: String, anonKey: String) {
        prefs.edit()
            .putString("custom_url", url.trim().removeSuffix("/"))
            .putString("custom_anon_key", anonKey.trim())
            .apply()
    }

    fun clearCustomConfig() {
        prefs.edit().remove("custom_url").remove("custom_anon_key").apply()
    }

    fun isConfigured(): Boolean {
        val url = getUrl()
        val key = getAnonKey()
        return url.isNotBlank() &&
                !url.contains("your-project", ignoreCase = true) &&
                key.isNotBlank() &&
                !key.contains("your_supabase_anon_key", ignoreCase = true)
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val url = getUrl()
        val key = getAnonKey()
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase não configurado. Forneça a URL e a Anon Key nas configurações.")
            )
        }

        try {
            val endpoint = "$url/rest/v1/dados?select=count"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Prefer", "count=exact")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            response.use { res ->
                if (res.isSuccessful) {
                    val count = res.header("content-range")?.substringAfter("/") ?: "OK"
                    Result.success("Conectado com sucesso ao Supabase! Registros encontrados: $count")
                } else if (res.code == 404) {
                    Result.failure(
                        Exception("Tabela 'dados' não encontrada (Erro 404). Crie a tabela no SQL Editor do Supabase.")
                    )
                } else {
                    val errorBody = res.body?.string().orEmpty()
                    Result.failure(Exception("Falha na conexão (HTTP ${res.code}): $errorBody"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Erro ao testar conexão", e)
            Result.failure(e)
        }
    }

    suspend fun fetchDados(): Result<List<Dado>> = withContext(Dispatchers.IO) {
        val url = getUrl()
        val key = getAnonKey()
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase não configurado. Configure a URL e a Anon Key.")
            )
        }

        try {
            val endpoint = "$url/rest/v1/dados?select=*&order=id.desc"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            response.use { res ->
                val bodyStr = res.body?.string().orEmpty()
                if (res.isSuccessful) {
                    val list = mutableListOf<Dado>()
                    val jsonArray = JSONArray(bodyStr)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optLong("id", System.currentTimeMillis() + i)
                        val usuario = obj.optString("usuario", obj.optString("user", "Anônimo"))
                        val conteudo = obj.optString("conteudo", obj.optString("content", ""))
                        val createdAt = obj.optString("created_at", "")
                        list.add(
                            Dado(
                                id = id,
                                usuario = usuario,
                                conteudo = conteudo,
                                createdAt = createdAt,
                                isSyncedWithSupabase = true
                            )
                        )
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Erro HTTP ${res.code} ao buscar dados: $bodyStr"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Erro ao buscar dados do Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun insertDado(usuario: String, conteudo: String): Result<Dado> = withContext(Dispatchers.IO) {
        val url = getUrl()
        val key = getAnonKey()
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase não configurado. Configure a URL e a Anon Key.")
            )
        }

        try {
            val endpoint = "$url/rest/v1/dados"
            val jsonPayload = JSONObject().apply {
                put("usuario", usuario)
                put("conteudo", conteudo)
            }

            val requestBody = jsonPayload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            response.use { res ->
                val bodyStr = res.body?.string().orEmpty()
                if (res.isSuccessful) {
                    val jsonArray = JSONArray(bodyStr)
                    if (jsonArray.length() > 0) {
                        val created = jsonArray.getJSONObject(0)
                        val id = created.optLong("id", System.currentTimeMillis())
                        val user = created.optString("usuario", usuario)
                        val content = created.optString("conteudo", conteudo)
                        val createdAt = created.optString("created_at", "")
                        Result.success(
                            Dado(
                                id = id,
                                usuario = user,
                                conteudo = content,
                                createdAt = createdAt,
                                isSyncedWithSupabase = true
                            )
                        )
                    } else {
                        Result.success(
                            Dado(
                                id = System.currentTimeMillis(),
                                usuario = usuario,
                                conteudo = conteudo,
                                isSyncedWithSupabase = true
                            )
                        )
                    }
                } else {
                    Result.failure(Exception("Erro HTTP ${res.code} ao inserir dado: $bodyStr"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Erro ao inserir dado no Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDado(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val url = getUrl()
        val key = getAnonKey()
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Supabase não configurado.")
            )
        }

        try {
            val endpoint = "$url/rest/v1/dados?id=eq.$id"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .delete()
                .build()

            val response = httpClient.newCall(request).execute()
            response.use { res ->
                if (res.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val body = res.body?.string().orEmpty()
                    Result.failure(Exception("Erro HTTP ${res.code} ao excluir dado: $body"))
                }
            }
        } catch (e: Exception) {
            Log.e("SupabaseClient", "Erro ao excluir dado do Supabase", e)
            Result.failure(e)
        }
    }
}
