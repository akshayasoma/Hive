package com.cs407.hive

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.hive.data.local.loadGroupId
import com.cs407.hive.data.local.saveGroupId
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val context = this
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        setContent {
            //This is not persistent...need to make the user light/dark mode preferences persistent
            val systemDarkTheme = isSystemInDarkTheme()

            var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }

            var storedGroupId by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                storedGroupId = loadGroupId(context)
            }

            HiveTheme(darkTheme = isDarkTheme) {
//                AppNavigation(deviceId=deviceId, isDarkTheme = isDarkTheme, onDarkModeChange = { isDarkTheme = it }, initialGroupId = null )
                if (storedGroupId != null) {
                    // Auto-login, skip login screen
                    AppNavigation(
                        deviceId = deviceId,
                        initialGroupId = storedGroupId,
                        isDarkTheme = isDarkTheme,
                        onDarkModeChange = { isDarkTheme = it }
                    )
                } else {
                    // Normal login flow
                    AppNavigation(
                        deviceId = deviceId,
                        initialGroupId = null,
                        isDarkTheme = isDarkTheme,
                        onDarkModeChange = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}

// Composable function responsible for navigation between screens
@Composable
fun AppNavigation(
    deviceId: String,
    initialGroupId: String?,
    isDarkTheme: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    // Creates and remembers a NavController to manage navigation state
    val navController = rememberNavController()
    var groupId by remember { mutableStateOf(initialGroupId) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                onNavigateToHome = { newGroupId ->
                    groupId = newGroupId
                    // Persist it for auto-login
                    scope.launch {
                        saveGroupId(context, newGroupId)
                    }
                    navController.navigate("home")
                }
//                onNavigateToHome = { navController.navigate("home") }

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
                deviceId = deviceId,
                groupId = groupId!!,
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
//                group = GroupRequest(
//                    groupId = "placeholder",
//                    creatorName = "User1",
//                    groupName = "Grp1",
//                    creatorId = "placeholder",
////                    peopleList = listOf()
//                ),
                deviceId = deviceId,
                groupId = groupId!!,
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
