package com.cs407.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.hive.data.model.GroupRequest
import com.cs407.hive.ui.screens.ChoresScreen
import com.cs407.hive.ui.screens.CreateScreen
import com.cs407.hive.ui.screens.GroceryScreen
import com.cs407.hive.ui.screens.HomeScreen
import com.cs407.hive.ui.screens.JoinScreen
import com.cs407.hive.ui.screens.LeaderboardScreen
import com.cs407.hive.ui.screens.LogInScreen
import com.cs407.hive.ui.screens.RecipeScreen
import com.cs407.hive.ui.screens.SettingsScreen
import com.cs407.hive.ui.theme.HiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //This is not persistent...need to make the user light/dark mode preferences persistent
            val systemDarkTheme = isSystemInDarkTheme()

            var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

            HiveTheme(darkTheme = isDarkTheme) {
                AppNavigation(isDarkTheme = isDarkTheme, onDarkModeChange = { isDarkTheme = it })
            }
        }
    }
}

// Composable function responsible for navigation between screens
@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    // Creates and remembers a NavController to manage navigation state
    val navController = rememberNavController()

    // NavHost sets up the navigation graph for the app
    NavHost(
        navController = navController, // Controller that handles navigation
        startDestination = "logIn" // First screen to display when app starts
    ) {

        composable("logIn") {
            LogInScreen(
                onNavigateToCreate = { navController.navigate("create")},
                onNavigateToJoin = { navController.navigate("join") },
                onNavigateToHome = { navController.navigate("home") }
            )
        }
        composable("create") {
            CreateScreen(
                onNavigateToLogIn = { navController.navigate("logIn") },
                onNavigateToHome = { navController.navigate("home") }

            )
        }
        composable("join") {
            JoinScreen (
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToLogIn = { navController.navigate("logIn") }
            )
        }
        composable("home"){
            HomeScreen(
                onNavigateToChores = { navController.navigate("chore")},
                onNavigateToGrocery = { navController.navigate("grocery")},
                onNavigateToRecipe = { navController.navigate("recipe")},
                onNavigateToSettings = { navController.navigate("settings")},
                onNavigateToLeaderboard = { navController.navigate("leaderboard")}
            )
        }
        composable("chore"){
            ChoresScreen(
                onNavigateToHome = { navController.navigate("home")}
            )
        }
        composable("grocery"){
            GroceryScreen(
                onNavigateToHome = { navController.navigate("home")}
            )
        }
        composable("recipe"){
            RecipeScreen(
                onNavigateToHome = { navController.navigate("home")}
            )
        }

        composable("settings") {

            SettingsScreen(
                group = GroupRequest(
                    groupId = "placeholder",
                    creatorName = "User1",
                    groupName = "Grp1",
                    peopleList = listOf()
                ),
                onNavigateToHome = { navController.navigate("home") },
                darkModeState = isDarkTheme,        // passes current theme
                onDarkModeChange = onDarkModeChange // passes callback to update
            )

        }

        composable("leaderboard"){
            LeaderboardScreen (
                onNavigateToHome = { navController.navigate("home")}
            )
        }

    }


}
