package com.cs407.cardfolio.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cs407.cardfolio.CardEntry
import com.cs407.cardfolio.CardStore
import com.cs407.cardfolio.R
import com.cs407.cardfolio.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun AddCardScreen(onNavigateToHome: () -> Unit) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(30.dp))
            Solofolio(
                onNavigateToHome = onNavigateToHome
            )
        }
    }

}

@Composable
fun Solofolio(onNavigateToHome: () -> Unit) {
    // State variables
    var name by remember { mutableStateOf("") }
    var hobby by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    val outlineColor = MaterialTheme.colorScheme.outline
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with title and menu icon
                Text(text = "Add Card", style = MaterialTheme.typography.headlineMedium)
                // Input fields for name, hobby, and age
                OutlinedTextField(
                    // Name field
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.card_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    // Hobby field
                    value = hobby,
                    onValueChange = { hobby = it },
                    label = { Text(stringResource(id = R.string.card_hobby_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    // Age field with numeric keyboard and validation
                    value = age,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            age = input
                        }
                    },
                    label = { Text(stringResource(id = R.string.card_age_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    // Buttons for editing and saving
                    Spacer(Modifier.width(150.dp))
                    OutlinedButton(
                        // Edit button enables input fields
                        onClick = { onNavigateToHome()},
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(Modifier.width(15.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || hobby.isBlank() || age.isBlank()) {
                                // Display an error message if fields are empty
                                Toast.makeText(
                                    context,
                                    "Please fill all fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Generate a unique ID and create a CardEntry object
                                val id = UUID.randomUUID().toString()
                                val entry = CardEntry(name, hobby, age.toInt())
                                // Store the new card in CardStore
                                CardStore.cards[id] = entry
                                // Show success message and navigate back
                                Toast.makeText(
                                    context,
                                    "Card added successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onNavigateToHome()
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}
