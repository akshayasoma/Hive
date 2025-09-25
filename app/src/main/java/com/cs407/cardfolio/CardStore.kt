package com.cs407.cardfolio

// Your Package declaration
import androidx.compose.runtime.mutableStateListOf
import java.io.Serializable
// Data class to represent a single card
data class CardEntry(
    val name: String,
    val hobby: String,
    val age: Int
) : Serializable
// Object to hold all cards in memory
object CardStore {
    val cards: HashMap<String, CardEntry> = hashMapOf()
}
// Object to hold and manage favorite cards
object FavoriteStore {
    val favorites = mutableStateListOf<CardEntry>()
    // Adds a card to favorites only if it's not already present
    fun add(entry: CardEntry) {
        if (favorites.none { it.name == entry.name
                    && it.hobby == entry.hobby && it.age == entry.age }) {
            favorites.add(entry)
        }
    }
    // Removes the specified card from favorites
    fun remove(entry: CardEntry) {
        favorites.removeAll { it.name == entry.name
                && it.hobby == entry.hobby && it.age == entry.age }
    }
}