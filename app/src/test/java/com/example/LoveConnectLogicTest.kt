package com.example

import com.example.data.local.UserProfileEntity
import com.example.data.repository.SampleAiRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LoveConnectLogicTest {

    private val aiRepository = SampleAiRepository()

    @Test
    fun sampleAiRepository_bio_returnsLabeledSampleResponse() = runBlocking {
        val bio = aiRepository.generateBio("Alex", "Architect", listOf("Coffee", "Design"), "Long-term partner")
        assertTrue(bio.isSampleResponse)
        assertTrue(bio.bioText.contains("Sample AI Bio"))
        assertTrue(bio.notice.contains("Firebase"))
    }

    @Test
    fun sampleAiRepository_compatibility_computesSharedInterestStrengths() = runBlocking {
        val me = UserProfileEntity(
            id = "me",
            isCurrentUser = true,
            fullName = "Alex",
            dateOfBirth = "1998-01-01",
            age = 28,
            gender = "Man",
            interestedIn = "Everyone",
            bio = "Coffee and art",
            city = "San Francisco",
            distanceKm = 0,
            education = "UC Berkeley",
            profession = "Designer",
            hobbiesCsv = "Coffee|Art Galleries",
            interestsCsv = "Coffee|Art Galleries",
            relationshipGoal = "Long-term partner",
            languagesCsv = "English",
            heightCm = 180,
            photosCsv = "drawable:marcus"
        )
        val candidate = UserProfileEntity(
            id = "prof_sophia",
            fullName = "Sophia Laurent",
            dateOfBirth = "2001-04-18",
            age = 25,
            gender = "Woman",
            interestedIn = "Everyone",
            bio = "Photographer",
            city = "San Francisco",
            distanceKm = 3,
            education = "UC Berkeley",
            profession = "Photographer",
            hobbiesCsv = "Coffee",
            interestsCsv = "Coffee|Travel",
            relationshipGoal = "Long-term partner",
            languagesCsv = "English",
            heightCm = 169,
            photosCsv = "drawable:sophia"
        )
        val analysis = aiRepository.analyzeCompatibility(me, candidate)
        assertTrue(analysis.scorePercent in 68..98)
        assertTrue(analysis.sharedStrengths.any { it.contains("Coffee") })
        assertTrue(analysis.isSampleResponse)
    }

    @Test
    fun sampleAiRepository_icebreakers_returnsThreePersonalizedSuggestions() = runBlocking {
        val partner = UserProfileEntity(
            id = "prof_elena",
            fullName = "Elena Vance",
            dateOfBirth = "2000-02-22",
            age = 26,
            gender = "Woman",
            interestedIn = "Everyone",
            bio = "Curator",
            city = "San Francisco",
            distanceKm = 5,
            education = "RISD",
            profession = "Curator",
            hobbiesCsv = "Painting",
            interestsCsv = "Art Galleries|Travel",
            relationshipGoal = "Long-term partner",
            languagesCsv = "English",
            heightCm = 172,
            photosCsv = "drawable:elena"
        )
        val result = aiRepository.suggestIcebreakers(null, partner)
        assertEquals(3, result.suggestions.size)
        assertTrue(result.suggestions.first().contains("Elena"))
    }
}
