package com.cs407.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.hive.ui.screens.CreateScreen
import com.cs407.hive.ui.screens.HomeScreen
import com.cs407.hive.ui.screens.JoinScreen
import com.cs407.hive.ui.screens.LogInScreen
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
            HomeScreen()
        }

    }


}
