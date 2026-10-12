package com.example.calmlift.data.model

data class MindsetQuote(
    val id: String,
    val quote: String,
    val sourceOrContext: String,
    val tag: String // e.g. "Spotlight Effect", "Pacing", "Perfectionism"
)

object MindsetQuotesDataSource {
    val quotes = listOf(
        MindsetQuote(
            id = "q1",
            quote = "Nobody is watching you. Everyone in the gym is preoccupied with their own body, insecurities, and playlist.",
            sourceOrContext = "The Spotlight Effect Illusion",
            tag = "Perspective"
        ),
        MindsetQuote(
            id = "q2",
            quote = "Just run the script. You did the thinking at home; right now you only execute the next set.",
            sourceOrContext = "Decision Hygiene",
            tag = "Focus"
        ),
        MindsetQuote(
            id = "q3",
            quote = "Leaving after 2 exercises is a victory. Consistency beats heroics every single week.",
            sourceOrContext = "Low-Friction Habit Building",
            tag = "Self-Compassion"
        ),
        MindsetQuote(
            id = "q4",
            quote = "Muscular fatigue is the signal you came for, not a flaw or weakness.",
            sourceOrContext = "Hypertrophy Physiology",
            tag = "Biology"
        ),
        MindsetQuote(
            id = "q5",
            quote = "Machines don't judge. Adjust the seat, lock the pin, and breathe.",
            sourceOrContext = "Friction Reduction",
            tag = "Simplicity"
        ),
        MindsetQuote(
            id = "q6",
            quote = "You are not here to impress anyone. You are here to stimulate muscle and leave.",
            sourceOrContext = "Gym Stoicism",
            tag = "Mindset"
        )
    )

    fun getRandomQuote(): MindsetQuote = quotes.random()
}
