package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.ui.theme.HiveTheme

@Composable
fun RecipeScreen(onNavigateToHome: () -> Unit) {
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
                var showInfo by remember { mutableStateOf(false) }

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

            repeat(3) {
                RecipeCard(
                    Item = "Item"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                    onClick = { /* TODO: open add pop-up/modal */ },
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
