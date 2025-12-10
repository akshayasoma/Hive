package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.ui.theme.HiveTheme
import com.cs407.hive.R
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun LeaderboardScreen(
    groupId: String,
    onNavigateToHome: () -> Unit,
    darkModeState: Boolean
) {
    //font
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    var leaderboardData by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val api = remember { ApiClient.instance }

    // Fetch leaderboard data
    LaunchedEffect(groupId) {
        scope.launch {
            isLoading = true
            try {
                // Get group details to fetch users (similar to ChoresScreen)
                val groupResponse = api.getGroup(mapOf("groupId" to groupId))
                val userIds = groupResponse.group.peopleList ?: emptyList()

                Log.d("LeaderboardScreen", "Found ${userIds.size} users in group")

                // Fetch each user's details to get their points
                val users = mutableListOf<LeaderboardUser>()
                for (userId in userIds) {
                    try {
                        val userResponse = api.getUser(mapOf("userId" to userId))
                        val user = userResponse.user
                        users.add(
                            LeaderboardUser(
                                userId = user.userId,
                                name = user.name,
                                points = user.points,
                                profilePic = user.profilePic,
                                role = getRoleForUser(user.points)
                            )
                        )
                        Log.d("LeaderboardScreen", "Loaded user: ${user.name} with ${user.points} points")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("LeaderboardScreen", "Error loading user $userId: $e")
                    }
                }

                // Sort by points in descending order and assign ranks
                val sortedUsers = users.sortedByDescending { it.points }
                    .mapIndexed { index, user ->
                        user.copy(rank = index + 1)
                    }

                leaderboardData = sortedUsers
                Log.d("LeaderboardScreen", "Sorted ${sortedUsers.size} users for leaderboard")

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("LeaderboardScreen", "Error loading leaderboard: $e")
                leaderboardData = emptyList()
            } finally {
                isLoading = false
            }
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

            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Text(
                text = "LEADERBOARD",
                fontFamily = CooperBt,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            } else if (leaderboardData.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No users in the group yet!",
                            fontFamily = CooperBt,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add members to see the leaderboard",
                            fontFamily = CooperBt,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            } else {
                // LazyColumn for leaderboard
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(leaderboardData.size) { index ->
                        val user = leaderboardData[index]

                        // Adjust width and opacity based on rank
                        val cardModifier = when (user.rank) {
                            1 -> Modifier.fillMaxWidth(0.95f).graphicsLayer(alpha = 1f)    // 1st: widest, fully opaque
                            2 -> Modifier.fillMaxWidth(0.9f).graphicsLayer(alpha = 1f)     // 2nd: slightly narrower
                            3 -> Modifier.fillMaxWidth(0.85f).graphicsLayer(alpha = 1f)    // 3rd: slightly narrower
                            else -> Modifier.fillMaxWidth(0.8f).graphicsLayer(alpha = 0.8f) // Others: narrower & 80% opacity
                        }

                        LBCard(
                            username = user.name,
                            rank = user.role,
                            points = "${user.points} pts",
                            status = getPlacementString(user.rank),
                            modifier = cardModifier
                        )
                    }
                }
            }
        }

        BottomAppBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.onTertiary,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
            }
        }
    }
}

@Composable
fun getBeeProfileImage(role: String): Painter {
    return when (role.lowercase()) {
        "queen", "queen bee" -> painterResource(id = R.drawable.profile_queen_bee)
        "worker", "worker bee" -> painterResource(id = R.drawable.profile_cool_bee)
        "harvester", "harvester bee" -> painterResource(id = R.drawable.profile_honey_bee)
        else -> painterResource(id = R.drawable.profile_drone_bee)
    }
}

@Composable
fun LBCard(
    username: String,
    rank: String,
    points: String,
    status: String,
    modifier: Modifier = Modifier
) {
    //font
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    val img = getBeeProfileImage(rank)
    Box(
        modifier = modifier
            .height(90.dp)
            .background(
                color = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = img,
                        contentDescription = "Bee icon",
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = username,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontFamily = CooperBt,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rank.uppercase(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = points,
                    fontFamily = CooperBt,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = status,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Helper function to determine role based on points
private fun getRoleForUser(points: Int): String {
    return when {
        points >= 1000 -> "Queen Bee"
        points >= 500 -> "Worker Bee"
        points >= 200 -> "Harvester"
        else -> "Drone"
    }
}

// Helper function to get placement string
private fun getPlacementString(rank: Int): String {
    return when (rank) {
        1 -> "1st Place"
        2 -> "2nd Place"
        3 -> "3rd Place"
        else -> "${rank}th Place"
    }
}

// Data class for leaderboard users
data class LeaderboardUser(
    val userId: String,
    val name: String,
    val points: Int,
    val profilePic: String,
    val role: String,
    val rank: Int = 0
)

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun LeaderboardPreviewLight() {
    HiveTheme(dynamicColor = false) {
        LeaderboardScreen(
            groupId = "preview-group",
            onNavigateToHome = {},
            darkModeState = false,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun LeaderboardPreviewDark() {
    HiveTheme(dynamicColor = false) {
        LeaderboardScreen(
            groupId = "preview-group",
            onNavigateToHome = {},
            darkModeState = true,
        )
    }
}