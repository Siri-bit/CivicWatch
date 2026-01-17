package com.civicwatch.claudeapp.ai

import android.graphics.Bitmap
import com.example.claudeapp.data.model.Issue
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateDetectionService @Inject constructor() {

    private val model = GenerativeModel(
        modelName = GeminiConfig.MODEL_NAME,
        apiKey = GeminiConfig.API_KEY
    )

    /**
     * Checks if the new issue (image + potential category) matches any of the nearby issues.
     * Returns the ID of the matching issue, or null if no duplicate found.
     */
    suspend fun findDuplicateIssue(
        newImage: Bitmap,
        newCategory: String,
        nearbyIssues: List<Issue>
    ): String? = withContext(Dispatchers.IO) {
        // Only check open issues
        val openIssues = nearbyIssues.filter { 
            it.status != "resolved" && it.status != "rejected"
        }

        if (openIssues.isEmpty()) return@withContext null

        // 1. Filter by Category & Distance (Strict pre-filter)
        // Only consider issues of the SAME category or seemingly similar category
        val candidates = openIssues.filter { issue ->
            isSameCategory(newCategory, issue.category)
        }.take(3) // LIMIT to top 3 closest to save bandwidth and tokens

        if (candidates.isEmpty()) return@withContext null

        // Resize newImage to avoid token limits and latency
        val resizedNewImage = resizeBitmap(newImage, 800)

        // 2. Prepare the prompt with MULTIPLE IMAGES
        // We will enable Gemini's multimodal compare capability
        
        val inputContent = content {
            text("""
                You are a Duplicate Issue Detector for a city maintenance app.
                
                I am providing you with a NEW REPORT image (labeled 'NEW_REPORT').
                I am also providing ${candidates.size} images of EXISTING reports nearby.
                
                Your task:
                Compare the 'NEW_REPORT' image visualy with each of the candidate images.
                
                CRITERIA FOR DUPLICATE:
                1. Does it show the EXACT SAME pothole, garbage pile, or infrastructure defect?
                2. Do the surrounding landmarks (walls, pavement cracks, trees, buildings) match?
                3. Even if the angle or lighting is different, does it depict the SAME SCENE?
                
                Product IDs:
            """.trimIndent())
            
            candidates.forEachIndexed { index, issue ->
                text("CANDIDATE_${index + 1} (ID: ${issue.issueId}): ${issue.description}")
            }
            
            text("\nNow, here are the images data. First ONE is the NEW_REPORT.")
            image(resizedNewImage) // The first image is the new one
            
            // Download and add candidate images
            candidates.forEach { issue ->
                val bitmap = downloadBitmap(issue.images.firstOrNull())
                if (bitmap != null) {
                    val resizedCandidate = resizeBitmap(bitmap, 800)
                    image(resizedCandidate)
                } else {
                    text("[Image missing for this candidate]")
                }
            }
            
            text("""
                
                QUESTION:
                Does the 'NEW_REPORT' image show the SAME physical issue as any of the Candidate images?
                
                If YES, return the ID of the matching candidate.
                If NO (it looks different or unrelated), return "null".
                
                Confidence threshold: Medium (>60%). Don't be too strict.
                Respond ONLY with the Issue ID or "null".
            """.trimIndent())
        }

        try {
            val response = model.generateContent(inputContent)
            val responseText = response.text?.trim() ?: return@withContext null
            
            println("Gemini Duplicate Check Response: $responseText")
            
            // Check if any candidate ID is in the response
            val matchedIssue = candidates.find { responseText.contains(it.issueId) }
            
            return@withContext matchedIssue?.issueId
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    private fun isSameCategory(cat1: String, cat2: String): Boolean {
        // Simple normalization
        val c1 = cat1.lowercase().replace(" ", "_").replace("potholes", "pothole")
        val c2 = cat2.lowercase().replace(" ", "_").replace("potholes", "pothole")
        if (c1.contains(c2) || c2.contains(c1)) return true
        return false
    }

    private fun downloadBitmap(url: String?): Bitmap? {
        if (url.isNullOrEmpty()) return null
        return try {
            val connection = java.net.URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            android.graphics.BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        var finalWidth = maxSize
        var finalHeight = maxSize
        
        if (ratioBitmap > 1) {
            finalWidth = maxSize
            finalHeight = (width / ratioBitmap).toInt()
        } else {
            finalHeight = maxSize
            finalWidth = (height * ratioBitmap).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}
