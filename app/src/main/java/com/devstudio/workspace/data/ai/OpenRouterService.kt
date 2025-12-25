package com.devstudio.workspace.data.ai

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenRouterResponse(
    val choices: List<Choice>?
)

data class Choice(
    val message: Message
)

object OpenRouterService {
    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    private val gson = Gson()

    suspend fun generateCompletion(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(Exception("API Key is missing"))
        if (model.isBlank()) return@withContext Result.failure(Exception("Model name is missing"))

        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            // Optional headers for OpenRouter rankings
            connection.setRequestProperty("HTTP-Referer", "com.devstudio.workspace") 
            connection.setRequestProperty("X-Title", "Workspace Notes")
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000 // 30 seconds timeout
            connection.readTimeout = 30000

            val requestBody = OpenRouterRequest(
                model = model,
                messages = listOf(
                    Message("system", systemPrompt),
                    Message("user", userMessage)
                )
            )

            val jsonBody = gson.toJson(requestBody)

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonBody)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val apiResponse = gson.fromJson(response.toString(), OpenRouterResponse::class.java)
                val content = apiResponse.choices?.firstOrNull()?.message?.content
                    ?: return@withContext Result.failure(Exception("Empty response from AI"))
                
                Result.success(content)
            } else {
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val reader = BufferedReader(InputStreamReader(errorStream))
                    val errorResponse = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        errorResponse.append(line)
                    }
                    reader.close()
                    Result.failure(Exception("API Error: $responseCode - $errorResponse"))
                } else {
                    Result.failure(Exception("API Error: $responseCode"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
