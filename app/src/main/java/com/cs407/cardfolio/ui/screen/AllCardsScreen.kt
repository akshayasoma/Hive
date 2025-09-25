package com.cs407.cardfolio.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cs407.cardfolio.CardEntry
import com.cs407.cardfolio.CardStore
import com.cs407.cardfolio.FavoriteStore
import com.cs407.cardfolio.R
import com.cs407.cardfolio.ui.theme.AppTheme

@Composable
fun AllCardsScreen(onNavigateToHome: () -> Unit) {
    // Theme setup
    val gradientTopColor = AppTheme.customColors.gradientTop
    val gradientBottomColor = AppTheme.customColors.gradientBottom
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientTopColor,
                        gradientBottomColor
                    )
                )
            ),
        color = Color.Transparent
    ) {
        // Actual content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(30.dp))
            allCards(onNavigateToHome = onNavigateToHome)
        }
    }

}
@Composable
fun allCards(onNavigateToHome: () -> Unit) {
    var cardState by
        remember { mutableStateOf(CardStore.cards.toMutableMap()) }
    var favorites by remember { mutableStateOf(FavoriteStore.favorites.toMutableSet()) }


    var showDeleteDialog by remember { mutableStateOf(false) }
    var cardToDeleteId by remember { mutableStateOf<String?>(null) }
    var cardToDelete by remember { mutableStateOf<CardEntry?>(null) }
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigateToHome() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(100.dp))
            Text(
                text = stringResource(id = R.string.all_cards),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
        }

        if (cardState.isEmpty()) {
            Text(text = stringResource(id = R.string.no_cards))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cardState.entries.toList()) { (cardId, card) ->
                    val isFavorite = favorites.contains(card)

                    ElevatedCard(
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column (modifier = Modifier
                            .fillMaxWidth()){
                            // Row with image, name, hobby, and Assistant button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp, 16.dp),
                            ) {

                                // Display default text if fields are empty
                                Text(
                                    text = card.name,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        if (isFavorite) {
                                            FavoriteStore.remove(card)
                                        } else {
                                            FavoriteStore.add(card)
                                        }
                                        favorites = FavoriteStore.favorites.toMutableSet()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) {
                                            Icons.Filled.Favorite
                                        } else {
                                            Icons.Outlined.FavoriteBorder
                                        },
                                        contentDescription = if (isFavorite) {
                                            "Remove from favorites"
                                        } else {
                                            "Add to favorites"
                                        },
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        cardToDeleteId = cardId
                                        showDeleteDialog = true
                                        cardToDelete = card
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete card"
                                    )
                                }
                            }
                            Text(text = "Hobby: ${card.hobby}", modifier = Modifier.padding(start = 20.dp), style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Age: ${card.age}", modifier = Modifier.padding(start = 20.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for deletion
    if (showDeleteDialog && cardToDeleteId != null && cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text( text = "Confirm Delete" ) },
            text = { Text( text = "Are you sure you want to delete this card?" ) },
            confirmButton = {
                TextButton(onClick = {
                    cardState = cardState.toMutableMap().also { it.remove(key = cardToDeleteId) }
                    CardStore.cards.remove(key = cardToDeleteId)
                    FavoriteStore.remove(entry = cardToDelete!!)
                    if (favorites.contains(cardToDelete)) {
                        favorites = favorites.toMutableSet().also { it.remove(element = cardToDelete) }
                    }
                    Toast.makeText(context,"Card deleted.",Toast.LENGTH_SHORT).show()
                    showDeleteDialog = false
                }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(text = "No") }
            }
        )
    }

}