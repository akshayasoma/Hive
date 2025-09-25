package com.cs407.cardfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.cs407.cardfolio.ui.theme.AppTheme
import com.cs407.cardfolio.ui.theme.CardfolioTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.cardfolio.ui.screen.AddCardScreen
import com.cs407.cardfolio.ui.screen.AllCardsScreen
import com.cs407.cardfolio.ui.screen.FavoriteScreen
import com.cs407.cardfolio.ui.screen.HomeScreen

// MainActivity is the entry point of the application
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Enables drawing behind system bars for a full-screen look
        enableEdgeToEdge()
// Sets the UI content for this Activity using Jetpack Compose
        setContent {
// Applies the app's theme
            CardfolioTheme {
// Calls the AppNavigation composable to set up navigation
                AppNavigation()
            }
        }
    }
}

// Composable function responsible for navigation between screens
@Composable
fun AppNavigation() {
// Creates and remembers a NavController to manage navigation state
    val navController = rememberNavController()
// Access the custom gradient colors from the app's theme
    val gradientTopColor = AppTheme.customColors.gradientTop
    val gradientBottomColor = AppTheme.customColors.gradientBottom
// NavHost sets up the navigation graph for the app
    NavHost(
        navController = navController, // Controller that handles navigation
        startDestination = "home" // First screen to display when app starts
    ) {
        composable("add_card") {
            AddCardScreen(
                onNavigateToHome = { navController.navigate("home") }
            )
        }
        composable("all_cards") {
            AllCardsScreen(
                onNavigateToHome = { navController.navigate("home") }
            )
        }
        composable("favorites") {
            FavoriteScreen(
                onNavigateToHome = { navController.navigate("home") }
            )
        }
// Defines the "home" route and what UI to display there
        composable("home") {
            HomeScreen(
                onNavigateToAddCard = { navController.navigate("add_card") },
                onNavigateToAllCards = { navController.navigate("all_cards") },
                onNavigateToFavorites = { navController.navigate("favorites") }
            )
        }
    }
}