package com.cs407.cardfolio.ui.screen

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cs407.cardfolio.FavoriteStore
import com.cs407.cardfolio.R
import com.cs407.cardfolio.ui.theme.AppTheme

@Composable
fun FavoriteScreen(onNavigateToHome: () -> Unit) {
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
            favorites(onNavigateToHome = onNavigateToHome)
        }
    }

}


@Composable
fun favorites(onNavigateToHome: () -> Unit) {
    val favorites = FavoriteStore.favorites
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
                text = stringResource(id = R.string.favorites),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )
        }

        if (favorites.isEmpty()) {
            Text(text = stringResource(id = R.string.no_cards))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { card ->

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

                            }
                            Text(text = "Hobby: ${card.hobby}", modifier = Modifier.padding(start = 20.dp), style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Age: ${card.age}", modifier = Modifier.padding(start = 20.dp), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
