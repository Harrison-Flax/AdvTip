package com.example.advtip

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

// Instantiate the Gemini AI (API)
class GeminiService {
    private val generativeModel = GenerativeModel(
        // Using the free model
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    // Function to generate a tip suggestion
    suspend fun generateTipSuggestion(
        // Parameters for the tip suggestion
        billAmount: Double,
        serviceQuality: String,
        groupSize: Int
    ): String {
        val prompt = """
            I have a bill of $${billAmount} for ${groupSize} people. 
            The service was ${serviceQuality}. 
            Can you suggest an appropriate tip percentage and explain why? 
            Also provide the exact tip amount and total amount to pay.
            Please base everything from your own knowledge and the legend of tip percentages.
            There should NEVER be a tip prediction of $0. 
            Keep the response concise and friendly.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Unable to generate tip suggestion"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}