package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.hive.data.perplexity.PerplexityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import com.cs407.hive.R

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
fun RecipeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCamera: () -> Unit = {},
    viewModel: RecipeViewModel = viewModel()
) {
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var ingredientName by remember { mutableStateOf("") }
    val myIngredients by viewModel.myIngredients.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showRecipes by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("None") }
    var expandedRecipeId by remember { mutableStateOf<String?>(null) }

    //font
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

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

    val sortedRecipes = remember(sortOption, hardcodedRecipes, myIngredients) {
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
        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Info icon (top left)
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
                fontFamily = CooperBt, //font added
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // My Ingredients Section
            if (myIngredients.isNotEmpty()) {
                Text(
                    text = "My Ingredients",
                    fontFamily = CooperBt, //font added
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
                                viewModel.removeIngredient(ingredient)
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
                        fontFamily = CooperBt, //font added
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                    if (sortOption != "None") {
                        Text(
                            text = "Sorted by: $sortOption",
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
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
                    when {
                        uiState.isLoading -> {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }

                        uiState.errorMessage != null -> {
                            item {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(32.dp)
                                )
                            }
                        }

                        uiState.recipes.isNotEmpty() -> {
                            items(uiState.recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    isExpanded = expandedRecipeId == recipe.id,
                                    onToggle = {
                                        expandedRecipeId =
                                            if (expandedRecipeId == recipe.id) null else recipe.id
                                    }
                                )
                            }
                        }

                        else -> {
                            items(sortedRecipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    isExpanded = expandedRecipeId == recipe.id,
                                    onToggle = {
                                        expandedRecipeId =
                                            if (expandedRecipeId == recipe.id) null else recipe.id
                                    }
                                )
                            }
                        }
                    }
                } else {
                    item {
                        if (myIngredients.isEmpty()) {
                            Text(
                                text = "Add ingredients you have to find recipes!",
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(32.dp)
                            )
                        } else {
                            Text(
                                text = "Click 'Find Recipes' to see recipe suggestions!",
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
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
                    viewModel.fetchRecipes(myIngredients)
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
                    fontFamily = CooperBt, //font added
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
                    onClick = {onNavigateToCamera()},
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

        if (showAddIngredientDialog) {
            val textColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
            AlertDialog(
                onDismissRequest = { showAddIngredientDialog = false },
                title = { Text("Add a New Ingredient", fontFamily = CooperBt) }, //font added
                text = {
                    Column {
                        OutlinedTextField(
                            value = ingredientName,
                            onValueChange = { ingredientName = it },
                            label = {
                                Text("Ingredient Name",
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                                    },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    val buttonColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }

                    val textColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        if (ingredientName.isNotBlank()) {
                            viewModel.addIngredient(
                                ingredientName.trim().replaceFirstChar { it.uppercase() })
                        }
                        ingredientName = ""
                        showAddIngredientDialog = false
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Add", fontFamily = CooperBt) //font added
                    }
                },
                dismissButton = {
                    val buttonColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onTertiary.copy(alpha=0.15f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    }

                    val textColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        ingredientName = ""
                        showAddIngredientDialog = false
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel", fontFamily = CooperBt) //font added
                    }
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                titleContentColor = MaterialTheme.colorScheme.onSecondary,
                textContentColor = MaterialTheme.colorScheme.onSecondary
            )
        }


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
                            fontFamily = CooperBt, //font added
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
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
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
                            fontFamily = CooperBt, //font added
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = infoText,
                            fontFamily = CooperBt, //font added
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
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = ingredient,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
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
fun RecipeCard(recipe: RecipeNote, isExpanded: Boolean, onToggle: () -> Unit) {
    //font
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(10.dp))
            .background(color = MaterialTheme.colorScheme.onPrimary)
            .clickable { onToggle() }
            .padding(16.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.dishName.uppercase(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontFamily = CooperBt, //font added
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${recipe.difficulty} • ${recipe.cookingTime}",
                    color = getDifficultyColor(recipe.difficulty),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                    //fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Text(
                    text = "Ingredients",
                    fontFamily = CooperBt, //font added
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.ingredients,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Instructions",
                    fontFamily = CooperBt, //font added
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.instructions,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )
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

class RecipeViewModel(
    private val repository: PerplexityRepository = PerplexityRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _myIngredients = MutableStateFlow<List<String>>(emptyList())
    val myIngredients: StateFlow<List<String>> = _myIngredients.asStateFlow()

    fun fetchRecipes(ingredients: List<String>) {
        if (ingredients.isEmpty()) return
        _uiState.value = RecipeUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val prompt = buildRecipePrompt(ingredients)
                val response = repository.askPrompt(prompt)
                val recipes = RecipeNoteParser.parse(response)
                _uiState.value = RecipeUiState(recipes = recipes, lastRawResponse = response)
            } catch (t: Throwable) {
                _uiState.value =
                    RecipeUiState(errorMessage = t.message ?: "Failed to fetch recipes")
            }
        }
    }

    fun addIngredient(ingredient: String) {
        if (ingredient.isNotBlank() && !_myIngredients.value.contains(ingredient)) {
            _myIngredients.value = _myIngredients.value + ingredient
        }
    }

    fun addIngredients(ingredients: List<String>) {
        if (ingredients.isEmpty()) return
        val current = _myIngredients.value.toMutableList()
        ingredients.forEach { ingredient ->
            if (ingredient.isNotBlank() && !current.contains(ingredient)) {
                current += ingredient
            }
        }
        _myIngredients.value = current
    }

    fun removeIngredient(ingredient: String) {
        _myIngredients.value = _myIngredients.value - ingredient
    }
}

data class RecipeUiState(
    val isLoading: Boolean = false,
    val recipes: List<RecipeNote> = emptyList(),
    val errorMessage: String? = null,
    val lastRawResponse: String? = null
)
