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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
                onNavigateToSettings: () -> Unit
                ){

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painterResource(id = R.drawable.home_screen_bee),
            contentDescription = "Bee",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(150.dp)
                .offset(x = 0.dp, y = -235.dp).graphicsLayer(scaleX = -1f) // move right and down
        )
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ){
            Button(
                onClick = {onNavigateToChores ()},
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(100.dp),      // taller button
                shape = RoundedCornerShape(20.dp), // rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "CHORES",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 60.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onNavigateToGrocery() },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(100.dp),      // taller button
                shape = RoundedCornerShape(20.dp), // rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "GROCERIES",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 50.sp
                )
            }



            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onNavigateToRecipe() },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(100.dp),      // taller button
                shape = RoundedCornerShape(20.dp), // rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "RECIPE",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 60.sp
                )
            }
        }

        Box (
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopStart
        ){
            Icon(
                painter = painterResource(id = R.drawable.home_screen_bee),
                contentDescription = "Bee",
                tint = Color.Unspecified,
                modifier = Modifier.size(75.dp)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.home_screen_bee),
            contentDescription = "Bee",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(75.dp)
                .offset(x = 150.dp, y = 160.dp).graphicsLayer(scaleX = -1f) // move right and down
        )


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
                // Settings Button
                Button(
                    onClick = { onNavigateToSettings()},
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        painter = painterResource(id=R.drawable.settings_flower),
                        contentDescription = "Settings",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(50.dp)

                    )
                }

                // Leaderboard Button
                Button(
                    onClick = { /* TODO: open leaderboard */ },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        painter = painterResource(id=R.drawable.ic_leaderboard),
                        contentDescription = "Leaderboard",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
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
                    onNavigateToSettings = {})
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
                    onNavigateToSettings = {})
    }
}