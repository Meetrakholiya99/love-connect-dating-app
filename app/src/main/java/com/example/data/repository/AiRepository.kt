package com.example.data.repository

import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.delay

data class AiBioSuggestion(
    val bioText: String,
    val styleTag: String,
    val isSampleResponse: Boolean = true,
    val notice: String = SAMPLE_AI_NOTICE
)

data class AiCompatibilityAnalysis(
    val scorePercent: Int,
    val summary: String,
    val sharedStrengths: List<String>,
    val suggestedDateIdea: String,
    val isSampleResponse: Boolean = true,
    val notice: String = SAMPLE_AI_NOTICE
)

data class AiIcebreakerResult(
    val suggestions: List<String>,
    val isSampleResponse: Boolean = true,
    val notice: String = SAMPLE_AI_NOTICE
)

const val SAMPLE_AI_NOTICE =
    "Sample AI response (Firebase disabled). Enable Firebase AI Logic in AI Studio to generate live Gemini responses."

interface AiRepository {
    suspend fun generateBio(
        name: String,
        profession: String,
        hobbies: List<String>,
        relationshipGoal: String
    ): AiBioSuggestion

    suspend fun analyzeCompatibility(
        currentUser: UserProfileEntity?,
        candidate: UserProfileEntity
    ): AiCompatibilityAnalysis

    suspend fun suggestIcebreakers(
        currentUser: UserProfileEntity?,
        matchProfile: UserProfileEntity
    ): AiIcebreakerResult
}

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * All outputs are clearly labeled as sample responses and never auto-send messages.
 */
class SampleAiRepository : AiRepository {
    override suspend fun generateBio(
        name: String,
        profession: String,
        hobbies: List<String>,
        relationshipGoal: String
    ): AiBioSuggestion {
        delay(350)
        val role = profession.ifBlank { "creative explorer" }
        val hobbyPhrase = if (hobbies.isNotEmpty()) {
            hobbies.take(3).joinToString(", ")
        } else {
            "weekend coffee walks, live music, and spontaneous adventures"
        }
        val goalPhrase = relationshipGoal.ifBlank { "meaningful connection" }.lowercase()
        return AiBioSuggestion(
            bioText = "Sample AI Bio: $role who brings curiosity to $hobbyPhrase. Looking for a $goalPhrase filled with genuine laughs and great conversations.",
            styleTag = "Warm & Authentic (Sample)"
        )
    }

    override suspend fun analyzeCompatibility(
        currentUser: UserProfileEntity?,
        candidate: UserProfileEntity
    ): AiCompatibilityAnalysis {
        delay(300)
        val userInterests = currentUser?.interests?.map { it.lowercase() }?.toSet() ?: emptySet()
        val candidateInterests = candidate.interests
        val shared = candidateInterests.filter { it.lowercase() in userInterests }
        val baseScore = 74 + (shared.size * 6).coerceAtMost(21) + if (currentUser?.relationshipGoal == candidate.relationshipGoal) 4 else 0
        val score = baseScore.coerceIn(68, 98)

        val strengths = if (shared.isNotEmpty()) {
            shared.map { "Shared passion for $it" }
        } else {
            listOf(
                "Complementary lifestyle in ${candidate.city}",
                "Aligned outlook: ${candidate.relationshipGoal}",
                "Verified & active community profile"
            )
        }

        val topInterest = shared.firstOrNull() ?: candidate.hobbies.firstOrNull() ?: "craft coffee"
        return AiCompatibilityAnalysis(
            scorePercent = score,
            summary = "Sample AI Insight: You and ${candidate.fullName.substringBefore(" ")} align strongly around $topInterest and ${candidate.relationshipGoal.lowercase()}.",
            sharedStrengths = strengths,
            suggestedDateIdea = "Sample Date Idea: Meet near ${candidate.city} for $topInterest and an unhurried sunset walk."
        )
    }

    override suspend fun suggestIcebreakers(
        currentUser: UserProfileEntity?,
        matchProfile: UserProfileEntity
    ): AiIcebreakerResult {
        delay(280)
        val firstName = matchProfile.fullName.substringBefore(" ")
        val firstHobby = matchProfile.hobbies.firstOrNull() ?: "weekend adventures"
        val secondInterest = matchProfile.interests.drop(1).firstOrNull() ?: "local spots"
        return AiIcebreakerResult(
            suggestions = listOf(
                "Hey $firstName! Your love for $firstHobby caught my eye — what's your favorite recent memory doing it?",
                "If we planned a spontaneous Saturday around $secondInterest in ${matchProfile.city}, where would we start?",
                "Quick debate for $firstName: morning espresso walk or sunset dinner first?"
            )
        )
    }
}
