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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.cs407.hive.ui.theme.HiveTheme
import kotlin.collections.plus

@Composable
fun RecipeScreen(onNavigateToHome: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf<String>()) }
    var showInfo by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.Start
            ) {
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 150.dp)
            ) {
                items(ingredients.reversed()) { ingredient ->
                    RecipeCard(Item = ingredient)
                }
            }
        }
        Button(
            onClick = { /* TODO: Add logic */ },
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
                text = "Find Recipe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
                    onClick = { /* TODO: enable delete mode */ },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Delete",
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

                // Add Button
                Button(
                    onClick = { showDialog = true },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Chore",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
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
                            ingredients = ingredients + ingredientName.trim().replaceFirstChar { it.uppercase() }
                        }
                        ingredientName = ""
                        showDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        ingredientName = ""
                        showDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
        //info box
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val addIconId = Icons.Default.Add
            val cameraIconId = Icons.Filled.CameraAlt

            val addIconKey = "addIcon"
            val cameraIconKey = "cameraIcon"

            val infoText = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 16.sp)) {
                    append("• Tap ")
                    appendInlineContent(addIconKey, "[add]")
                    append(" to manually enter ingredients\n")
                }

                withStyle(SpanStyle(fontSize = 16.sp)) {
                    append("• Tap ")
                    appendInlineContent(cameraIconKey, "[camera]")
                    append(" to scan ingredients with the camera")
                }

                val subBullet = "◦ Tap on recipe card to view the full recipe"
                addStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(firstLine = 24.sp, restLine = 34.sp),
                        lineHeight = 20.sp
                    ),
                    start = length,
                    end = length + subBullet.length
                )
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
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            )
            // background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .clickable { showInfo = false }
            )

            // info card
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
fun RecipeCard(Item: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(90.dp)
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
    ) {
        Row (modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically){
            Text(
                text = Item.uppercase(),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun RecipePreviewLight() {
    HiveTheme(dynamicColor = false) {
        RecipeScreen( onNavigateToHome = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun RecipePreviewDark() {
    HiveTheme(dynamicColor = false) {
        RecipeScreen (onNavigateToHome = {})
    }
}
