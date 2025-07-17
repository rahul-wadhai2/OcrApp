package com.syncwos.ocrapp.repository

import android.graphics.Bitmap
import com.syncwos.ocrapp.model.BusinessCard
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BusinessCardRepository {

    private val realtimeDatabase: FirebaseDatabase = Firebase.database
    private val rtDbCardsRef: DatabaseReference = realtimeDatabase.getReference("businessCards_rt")

    suspend fun saveBusinessCardToRealtimeDB(card: BusinessCard): String = suspendCoroutine { continuation ->
        val newCardRef = rtDbCardsRef.push()
        val newCardId = newCardRef.key

        if (newCardId == null) {
            continuation.resumeWithException(Exception("Failed to generate unique key for Realtime DB."))
            return@suspendCoroutine
        }

        card.id = newCardId
        card.timestamp = System.currentTimeMillis()

        newCardRef.setValue(card)
            .addOnSuccessListener {
                continuation.resume(newCardId)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    private val recognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeTextFromImage(bitmap: Bitmap): Result<String> {
        return try {
            val visionImage = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(visionImage).await()
            Result.success(result.text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseBusinessCard(rawText: String): BusinessCard {
        val businessCard = BusinessCard()

        val phoneRegex = Regex("""(\+?\d[\d\s\-\(\)]{7,}\d)""")
        val emailRegex = Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}""")
        val websiteRegex =
            Regex("""(?:^|\s)((https?:\/\/|www\.)[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b(?:[-a-zA-Z0-9()@:%_\+.~#?&\/=]*))""")
        val addressRegex =
            Regex("""\d+\s+[A-Za-z0-9\s.,'-]+,\s*[A-Za-z\s]+,\s*[A-Za-z]{2}\s*\d{5}(-\d{4})?(\s+[A-Za-z\s]+)?\b""")
        val jobTitleRegex = Regex(
            """\b(?:Senior|Junior|Principal|Technical|Team|Chief)?\s*(?:Lead|Leader|Head|Executive|Technology|Financial|Information)?\s*(?:Manager|Director|Developer|Engineer|Architect|Specialist|Analyst|Officer|Head|Animator|CEO|CTO|CFO|CIO)\b(?:\s*(?:[IVX]+|\d+[a-z]*|Level\s*\d+))?\b""",
            RegexOption.IGNORE_CASE
        )

        businessCard.phone = phoneRegex.find(rawText)?.value ?: ""
        businessCard.email = emailRegex.find(rawText)?.value ?: ""
        businessCard.website = websiteRegex.find(rawText)?.value ?: ""
        businessCard.address = addressRegex.find(rawText)?.value ?: ""
        businessCard.jobTitle = jobTitleRegex.find(rawText)?.value ?: ""

        val lines = rawText.lines()
        if (lines.isNotEmpty()) businessCard.name = lines[0]
        if (lines.size > 1) businessCard.company = lines[1]

        return businessCard
    }
}
