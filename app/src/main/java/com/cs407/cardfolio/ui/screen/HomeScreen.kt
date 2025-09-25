package com.cs407.cardfolio.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cs407.cardfolio.R
import com.cs407.cardfolio.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToAddCard: () -> Unit,
               onNavigateToAllCards: () -> Unit,
               onNavigateToFavorites: () -> Unit) {
    // Drawer state
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Theme setup
    val gradientTopColor = AppTheme.customColors.gradientTop
    val gradientBottomColor = AppTheme.customColors.gradientBottom
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("All Cards") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAllCards()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Favorites") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToFavorites()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = true,
                    onClick = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
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
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                    Spacer(Modifier.width(50.dp))
                    Text(
                        text = stringResource(id = R.string.app_title),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 24.dp)
                            .weight(1f)
                    )
                    IconButton(onClick = onNavigateToAddCard) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
                Cardfolio()
            }
        }
    }
}

@Composable
fun Cardfolio() {
    // State variables
    var name by remember { mutableStateOf("") }
    var hobby by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(true) }
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
            // Row with image, name, hobby, and Assistant button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    // Using a placeholder image from resources
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .border(1.dp, outlineColor, CircleShape)
                )
                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    // Display default text if fields are empty
                    Text(
                        text = if (name.isBlank()) stringResource(id = R.string.card_name) else name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (hobby.isBlank()) stringResource(id = R.string.card_hobby) else hobby,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    // Toggle between edit and lock icons and labels
                    onClick = { isEditing = !isEditing },
                    label = { Text(if (isEditing) stringResource(id = R.string.edit_assist) else stringResource(id = R.string.lock_assist)) },
                    leadingIcon = {
                        Icon(
                            if (isEditing) Icons.Default.Edit else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                )
            }
            HorizontalDivider(color = outlineColor)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp, 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Input fields for name, hobby, and age
                OutlinedTextField(
                    // Name field
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.card_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    readOnly = !isEditing
                )
                OutlinedTextField(
                    // Hobby field
                    value = hobby,
                    onValueChange = { hobby = it },
                    label = { Text(stringResource(id = R.string.card_hobby_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    readOnly = !isEditing
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
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    supportingText = { if (isEditing) Text(stringResource(id = R.string.age_warning)) },
                    readOnly = !isEditing
                )
                Row {
                    // Buttons for editing and saving
                    Spacer(Modifier.width(100.dp))
                    OutlinedButton(
                        // Edit button enables input fields
                        onClick = { isEditing = true },
                        enabled = !isEditing
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.edit))
                    }
                    Spacer(Modifier.width(15.dp))
                    Button(
                        // Save button validates input fields before saving
                        onClick = {
                            // Check for missing fields
                            val missing = buildList {
                                if (name.isBlank()) add("Name")
                                if (hobby.isBlank()) add("Hobby")
                                if (age.isBlank()) add("Age")
                            }
                            if (missing.isEmpty()) {
                                // If no fields are missing, save and exit editing mode
                                isEditing = false
                                Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                // Show a message indicating which fields are missing
                                val message = "Please enter: ${missing.joinToString(", ")}"
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }},
                        enabled = isEditing
                    ) {
                        // Change icon and label based on editing state
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        if (isEditing) {
                            Text(stringResource(id = R.string.show))
                        } else {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                }
            }
        }
    }
}
