package com.cs407.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.hive.ui.screens.ChoresScreen
import com.cs407.hive.ui.screens.CreateScreen
import com.cs407.hive.ui.screens.GroceryScreen
import com.cs407.hive.ui.screens.HomeScreen
import com.cs407.hive.ui.screens.JoinScreen
import com.cs407.hive.ui.screens.LogInScreen
import com.cs407.hive.ui.screens.RecipeScreen
import com.cs407.hive.ui.theme.HiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiveTheme {
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

    // NavHost sets up the navigation graph for the app
    NavHost(
        navController = navController, // Controller that handles navigation
        startDestination = "logIn" // First screen to display when app starts
    ) {

        composable("logIn") {
            LogInScreen(
                onNavigateToCreate = { navController.navigate("create")},
                onNavigateToJoin = { navController.navigate("join") }
            )
        }
        composable("create") {
            CreateScreen(
                onNavigateToLogIn = { navController.navigate("logIn") },
                onNavigateToHome = { navController.navigate("home")}
            )
        }
        composable("join") {
            JoinScreen(
                onNavigateToLogIn = { navController.navigate("logIn")},
                onNavigateToHome = { navController.navigate("home")}
            )
        }
        composable("home"){
            HomeScreen(
                onNavigateToChores = { navController.navigate("chore")},
                onNavigateToGrocery = { navController.navigate("grocery")},
                onNavigateToRecipe = { navController.navigate("recipe")}
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


    }


}
