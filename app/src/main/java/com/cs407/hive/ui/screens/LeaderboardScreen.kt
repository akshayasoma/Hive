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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.ui.theme.HiveTheme
import com.cs407.hive.R

@Composable
fun LeaderboardScreen(onNavigateToHome: () -> Unit) {
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
                text = "Lead ya board",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

//            LBCard(
//                username = "Winner",
//                rank = "Queen Bee",
//                points = "Points",
//                status = "Status"
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            LBCard(
//                username = "Second",
//                rank = "worker bee",
//                points = "Points",
//                status = "Status"
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            repeat(3) {
//                LBCard(
//                    username = "User Icon",
//                    rank = "drone",
//                    points = "Points",
//                    status = "Status"
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//            }
            // LazyColumn for top 10
            val leaderboard = listOf(
                Triple("Winner", "Queen Bee", "1st Place"),
                Triple("Second", "Worker Bee", "2nd Place"),
                Triple("User3", "Drone", "3rd Place"),
                Triple("User4", "Drone", "4th Place"),
                Triple("User5", "Drone", "5th Place"),
                Triple("User6", "Drone", "6th Place"),
                Triple("User7", "Drone", "7th Place"),
                Triple("User8", "Drone", "8th Place"),
                Triple("User9", "Drone", "9th Place"),
                Triple("User10", "Drone", "10th Place")
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(leaderboard.size) { index ->
                    val (username, rank, status) = leaderboard[index]
                    LBCard(
                        username = username,
                        rank = rank,
                        points = "${(1000 - index * 50)} pts",
                        status = status
                    )
                }
            }
        }

        BottomAppBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = MaterialTheme.colorScheme.onTertiary,
            tonalElevation = 8.dp
        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {

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


//            }
            }
        }
    }
}

@Composable
fun getBeeProfileImage(role: String): Painter {
    return when (role.lowercase()) {
        "queen" -> painterResource(id = R.drawable.profile_queen_bee)
        "queen bee" -> painterResource(id = R.drawable.profile_queen_bee)
        "worker" -> painterResource(id = R.drawable.profile_cool_bee)
        "worker bee" -> painterResource(id = R.drawable.profile_cool_bee)
        else -> painterResource(id = R.drawable.profile_drone_bee)
    }
}

@Composable
fun LBCard(username: String, rank: String, points: String, status: String) {
    var img = getBeeProfileImage(rank)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
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
                // Circle “bee” placeholder
                Box(
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
//                    Text(
//                        text = "🐝",
//                        color = MaterialTheme.colorScheme.onSecondary,
//                        fontSize = 26.sp
//                    )
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rank.uppercase(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = points,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = status,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun LeaderboardPreviewLight() {
    HiveTheme(dynamicColor = false) {
        LeaderboardScreen( onNavigateToHome = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun LeaderboardPreviewDark() {
    HiveTheme(dynamicColor = false) {
        LeaderboardScreen(onNavigateToHome = {})
    }
}
