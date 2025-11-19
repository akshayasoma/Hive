package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.ui.theme.HiveTheme
import com.google.android.gms.games.leaderboard.Leaderboard
import com.cs407.hive.R



@Composable
fun HomeScreen (onNavigateToChores: () -> Unit,
                onNavigateToGrocery: () -> Unit,
                onNavigateToRecipe: () -> Unit,
                onNavigateToSettings: () -> Unit,
                onNavigateToLeaderboard: () -> Unit
                ){
    val userName = "UserName" //hardcoded needs to be connected to the db
//    val maxDisplayLength = 6
//    val truncatedName = if (userName.length > maxDisplayLength) {
//        // Truncate and add ellipsis
//        userName.substring(0, maxDisplayLength) + "..."
//    } else {
//        userName
//    }

    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        Scaffold(
            bottomBar = {
                BottomAppBar(
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

                        // Settings Button
                        Button(
                            onClick = { onNavigateToSettings() },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.settings_flower),
                                contentDescription = "Settings",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        // Leaderboard Button
                        Button(
                            onClick = { onNavigateToLeaderboard() },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_leaderboard),
                                contentDescription = "Leaderboard",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    // Greeting Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                            .padding(top = 40.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row {
                            Text(
                                text = "Hello! ",
                                fontSize = 40.sp,
                                fontFamily = CooperBt,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = userName,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                fontSize = 40.sp,
                                fontFamily = CooperBt,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontStyle = FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Happy Hive, Happy Life",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Buttons Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val buttons = listOf(
                                    Triple("CHORES", R.drawable.chores_icon, onNavigateToChores),
                                    Triple("GROCERY", R.drawable.grocery_icon, onNavigateToGrocery),
                                    Triple("RECIPES", R.drawable.recipes_icon, onNavigateToRecipe)
                                )

                                val iconOffsets = mapOf(
                                    "CHORES" to (-12).dp,
                                    "GROCERY" to (-23).dp,
                                    "RECIPES" to 0.dp
                                )

                                buttons.forEach { (text, iconRes, onClickAction) ->
                                    Button(
                                        onClick = { onClickAction() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        contentPadding = PaddingValues(10.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Icon(
                                                painter = painterResource(id = iconRes),
                                                contentDescription = text,
                                                tint = Color.Unspecified,
                                                modifier = Modifier
                                                    .size(140.dp)
                                                    .align(Alignment.CenterStart)
                                                    .offset(x = iconOffsets[text] ?: 0.dp)
                                            )

                                            Text(
                                                text = text,
                                                fontFamily = CooperBt,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 30.sp,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.align(Alignment.BottomEnd)
                                            )
                                        }
                                    }
                                }
                            }

                            // Left Bee
                            Icon(
                                painter = painterResource(id = R.drawable.home_left_bee),
                                contentDescription = "Left Bee",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-51).dp, y = 40.dp)
                            )

                            // Right Bee
                            Icon(
                                painter = painterResource(id = R.drawable.home_right_bee),
                                contentDescription = "Right Bee",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 40.dp, y = (-90).dp)
                            )

                        }
                    }
                }
            }
        }
    }

}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
@Composable
fun HomeScreenPreviewDark() {
    HiveTheme(dynamicColor = false) {
        HomeScreen( onNavigateToChores = {},
                    onNavigateToGrocery = {},
                    onNavigateToRecipe = {},
                    onNavigateToSettings = {},
                    onNavigateToLeaderboard = {})
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun HomeScreenPreviewLight() {
    HiveTheme(dynamicColor = false) {
        HomeScreen(onNavigateToChores = {},
                    onNavigateToGrocery = {},
                    onNavigateToRecipe = {},
                    onNavigateToSettings = {},
                    onNavigateToLeaderboard = {})
    }
}