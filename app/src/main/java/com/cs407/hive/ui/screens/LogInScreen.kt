package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.http.HttpException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.ui.theme.HiveTheme
import android.provider.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.cs407.hive.data.network.ApiClient
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.compose.runtime.getValue



@Composable
fun LogInScreen(onNavigateToCreate: () -> Unit,
                onNavigateToJoin: () -> Unit, onNavigateToHome: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.instance.checkLogin(mapOf("deviceId" to deviceId))
                if (response.message.contains("already in a group", ignoreCase = true)) {
                    onNavigateToHome()
                } else {
                    isChecking = false
                }
            } catch (e: IOException) {
                isChecking = false
            } catch (e: HttpException) {
                isChecking = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = {onNavigateToCreate()},
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp),      // taller button
                shape = RoundedCornerShape(20.dp), // rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "CREATE",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 60.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onNavigateToJoin() },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp),      // taller button
                shape = RoundedCornerShape(20.dp), // rounded rectangle
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "JOIN",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 60.sp
                )
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
fun LogInScreenPreview(){
    HiveTheme (dynamicColor = false) {
        LogInScreen(
            onNavigateToCreate = {},
            onNavigateToJoin = {},
            onNavigateToHome = {}
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
@Composable
fun LogInScreenPreviewLight() {
    HiveTheme(dynamicColor = false) {
        LogInScreen(
            onNavigateToCreate = {},
            onNavigateToJoin = {},
            onNavigateToHome = {}
        )
    }
}