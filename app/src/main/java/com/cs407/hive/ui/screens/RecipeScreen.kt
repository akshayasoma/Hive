package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cs407.hive.ui.theme.HiveTheme

data class RecipeNote(
    val id: String,
    val dishName: String,
    val difficulty: String,
    val cookingTime: String,
    val ingredients: String,
    val instructions: String,
    val timestamp: String
)

@Composable
fun RecipeScreen(onNavigateToHome: () -> Unit) {
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showRecipeDetail by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var selectedRecipe by remember { mutableStateOf<RecipeNote?>(null) }
    var ingredientName by remember { mutableStateOf("") }
    var myIngredients by remember { mutableStateOf(listOf<String>()) }
    var showRecipes by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("None") }

    // Hardcoded recipe notes
    val hardcodedRecipes = remember {
        listOf(
            RecipeNote(
                id = "1",
                dishName = "Spaghetti Carbonara",
                difficulty = "Medium",
                cookingTime = "20 mins",
                ingredients = "• 400g spaghetti\n• 200g pancetta\n• 4 eggs\n• 100g Parmesan cheese\n• Black pepper\n• Salt",
                instructions = "1. Cook spaghetti al dente\n2. Fry pancetta until crispy\n3. Whisk eggs with Parmesan\n4. Combine everything while hot\n5. Season with black pepper",
                timestamp = "2024-01-01"
            ),
            RecipeNote(
                id = "2",
                dishName = "Chicken Stir Fry",
                difficulty = "Easy",
                cookingTime = "30 mins",
                ingredients = "• 2 chicken breasts\n• 1 bell pepper\n• 1 onion\n• 2 cloves garlic\n• Soy sauce\n• Vegetable oil",
                instructions = "1. Slice chicken and vegetables\n2. Heat oil in wok\n3. Stir-fry chicken until cooked\n4. Add vegetables and cook until tender\n5. Add soy sauce and serve",
                timestamp = "2024-01-01"
            ),
            RecipeNote(
                id = "3",
                dishName = "Beef Wellington",
                difficulty = "Hard",
                cookingTime = "2 hours",
                ingredients = "• 500g beef tenderloin\n• 200g mushrooms\n• 8 slices prosciutto\n• 1 sheet puff pastry\n• 2 tbsp mustard\n• 1 egg for egg wash",
                instructions = "1. Sear beef on all sides\n2. Process mushrooms into duxelles\n3. Layer prosciutto, duxelles, and beef\n4. Wrap in puff pastry\n5. Bake at 200°C for 25-30 mins",
                timestamp = "2024-01-01"
            )
        )
    }

    val sortedRecipes = remember(sortOption, hardcodedRecipes) {
        when (sortOption) {
            "Difficulty" -> hardcodedRecipes.sortedBy { recipe ->
                when (recipe.difficulty) {
                    "Easy" -> 1
                    "Medium" -> 2
                    "Hard" -> 3
                    else -> 4
                }
            }
            "Duration" -> hardcodedRecipes.sortedBy { recipe ->
                when {
                    recipe.cookingTime.contains("hour") -> {
                        val hours = recipe.cookingTime.replace(" hours", "").replace(" hour", "").toIntOrNull() ?: 0
                        hours * 60
                    }
                    recipe.cookingTime.contains("min") -> {
                        recipe.cookingTime.replace(" mins", "").replace(" min", "").toIntOrNull() ?: 0
                    }
                    else -> 0
                }
            }
            else -> hardcodedRecipes
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info button (left)
                Button(
                    onClick = { showInfo = !showInfo },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }

                // Sort button (right)
                if (showRecipes) {
                    Button(
                        onClick = { showSortOptions = !showSortOptions },
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Text(
                text = "RECIPE",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // My Ingredients Section
            if (myIngredients.isNotEmpty()) {
                Text(
                    text = "My Ingredients",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myIngredients) { ingredient ->
                        IngredientChip(
                            ingredient = ingredient,
                            onDelete = {
                                myIngredients = myIngredients - ingredient
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showRecipes) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recipe Suggestions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                    if (sortOption != "None") {
                        Text(
                            text = "Sorted by: $sortOption",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {
                if (showRecipes) {
                    items(sortedRecipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onDelete = { /* TODO */ },
                            onClick = {
                                selectedRecipe = recipe
                                showRecipeDetail = true
                            }
                        )
                    }
                } else {
                    item {
                        if (myIngredients.isEmpty()) {
                            Text(
                                text = "Add ingredients and click 'Find Recipes' to get suggestions!",
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(32.dp)
                            )
                        } else {
                            Text(
                                text = "Click 'Find Recipes' to see recipe suggestions!",
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }

        if (myIngredients.isNotEmpty()) {
            Button(
                onClick = {
                    showRecipes = true
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 125.dp)
                    .height(55.dp)
                    .width(200.dp)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(50))
            ) {
                Text(
                    text = "Find Recipes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom App Bar
        BottomAppBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.onTertiary,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* TODO: camera scanning logic */ },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Scan Ingredients",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Home Button
                Button(
                    onClick = { onNavigateToHome() },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home Screen",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Add Ingredient Button
                Button(
                    onClick = { showAddIngredientDialog = true },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Ingredient",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }

        // Add Ingredient Dialog (Simple - like your original)
        if (showAddIngredientDialog) {
            AlertDialog(
                onDismissRequest = { showAddIngredientDialog = false },
                title = { Text("Add a New Ingredient") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = ingredientName,
                            onValueChange = { ingredientName = it },
                            label = { Text("Ingredient Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (ingredientName.isNotBlank()) {
                            myIngredients = myIngredients + ingredientName.trim().replaceFirstChar { it.uppercase() }
                        }
                        ingredientName = ""
                        showAddIngredientDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        ingredientName = ""
                        showAddIngredientDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Recipe Detail Dialog
        if (showRecipeDetail && selectedRecipe != null) {
            RecipeDetailDialog(
                recipe = selectedRecipe!!,
                onDismiss = {
                    showRecipeDetail = false
                    selectedRecipe = null
                }
            )
        }

        // Sort Options Dialog
        if (showSortOptions) {
            Dialog(onDismissRequest = { showSortOptions = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Sort Recipes By",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sort options
                        listOf("None", "Difficulty", "Duration").forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        sortOption = option
                                        showSortOptions = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sortOption == option,
                                    onClick = {
                                        sortOption = option
                                        showSortOptions = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (option) {
                                        "None" -> "No sorting"
                                        "Difficulty" -> "Difficulty (Easy to Hard)"
                                        "Duration" -> "Cooking Time (Short to Long)"
                                        else -> option
                                    },
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Info Box
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val addIconId = Icons.Default.Add
            val cameraIconId = Icons.Filled.CameraAlt
            val sortIconId = Icons.Filled.Sort

            val addIconKey = "addIcon"
            val cameraIconKey = "cameraIcon"
            val sortIconKey = "sortIcon"

            val infoText = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 16.sp)) {
                    append("• Tap ")
                    appendInlineContent(addIconKey, "[add]")
                    append(" to manually enter ingredients\n")
                }

                withStyle(SpanStyle(fontSize = 16.sp)) {
                    append("• Tap ")
                    appendInlineContent(cameraIconKey, "[camera]")
                    append(" to scan ingredients with the camera\n")
                }

                withStyle(SpanStyle(fontSize = 16.sp)) {
                    append("• Tap ")
                    appendInlineContent(sortIconKey, "[sort]")
                    append(" to sort recipes by difficulty or duration\n")
                }

                val subBullet = "• Add ingredients and click 'Find Recipes' to see suggestions"
                append(subBullet)
            }

            val inlineContent = mapOf(
                addIconKey to InlineTextContent(
                    Placeholder(
                        width = 18.sp,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = addIconId,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                cameraIconKey to InlineTextContent(
                    Placeholder(
                        width = 18.sp,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = cameraIconId,
                        contentDescription = "Camera",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                sortIconKey to InlineTextContent(
                    Placeholder(
                        width = 18.sp,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = sortIconId,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            )

            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .clickable { showInfo = false }
            )

            // Info card
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .padding(28.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HOW TO USE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = infoText,
                            inlineContent = inlineContent,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientChip(ingredient: String, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .wrapContentWidth()
            .border(1.dp, MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = ingredient,
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeNote, onDelete: () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(120.dp)
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = recipe.dishName.uppercase(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.difficulty} • ${recipe.cookingTime}",
                    color = getDifficultyColor(recipe.difficulty),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RecipeDetailDialog(recipe: RecipeNote, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = recipe.dishName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "${recipe.difficulty} • ${recipe.cookingTime}",
                        color = getDifficultyColor(recipe.difficulty),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ingredients",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.ingredients,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Instructions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = recipe.instructions,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty) {
        "Easy" -> Color(0xFF4CAF50)
        "Medium" -> Color(0xFFFF9800)
        "Hard" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun RecipePreviewLight() {
    HiveTheme(dynamicColor = false) {
        RecipeScreen(onNavigateToHome = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun RecipePreviewDark() {
    HiveTheme(dynamicColor = false) {
        RecipeScreen(onNavigateToHome = {})
    }
}