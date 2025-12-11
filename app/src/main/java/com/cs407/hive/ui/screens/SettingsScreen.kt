package com.cs407.hive.ui.screens

import android.R.attr.description
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import com.cs407.hive.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.data.model.GroupRequest
import com.cs407.hive.ui.theme.HiveTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.cs407.hive.MainActivity
import com.cs407.hive.data.local.clearGroupId
import com.cs407.hive.data.local.saveGroupId
import com.cs407.hive.data.model.GroupDetail
import com.cs407.hive.data.model.LeaveGroupRequest
import com.cs407.hive.data.model.UpdateGroupNameRequest
import com.cs407.hive.data.model.UpdateProfilePicRequest
import com.cs407.hive.data.model.UpdateUserNameRequest
import com.cs407.hive.data.model.UserDetail
import com.cs407.hive.data.network.ApiClient
import kotlinx.coroutines.launch
import kotlin.collections.plus

@Composable
fun SettingsScreen(
    deviceId: String,
    groupId: String,
    onNavigateToHome: () -> Unit,
    darkModeState: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    Log.d("SettingsScreen", "Composable started with deviceId=$deviceId groupId=$groupId")

    var showDialog by remember { mutableStateOf(false) }
    var showDialogLeave by remember { mutableStateOf(false) }
    var deletionIntent by remember { mutableStateOf("") }
    var leaveIntent by remember { mutableStateOf("") }


    var editable by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(darkModeState) }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //font
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    // Add these functions after the font declaration
    fun getProfilePicString(resId: Int): String {
        return when (resId) {
            R.drawable.profile_happy_bee -> "happy_bee"
            R.drawable.profile_honey_bee -> "honey_bee"
            R.drawable.profile_cool_bee -> "cool_bee"
            R.drawable.profile_heart_bee -> "heart_bee"
            R.drawable.profile_nerd_bee -> "nerd_bee"
            R.drawable.profile_guitar_bee -> "guitar_bee"
            R.drawable.ai_bee -> "ai_bee"
            else -> "ai_bee"
        }
    }

    fun getProfilePicResId(profilePicString: String): Int {
        return when (profilePicString) {
            "happy_bee" -> R.drawable.profile_happy_bee
            "honey_bee" -> R.drawable.profile_honey_bee
            "cool_bee" -> R.drawable.profile_cool_bee
            "heart_bee" -> R.drawable.profile_heart_bee
            "nerd_bee" -> R.drawable.profile_nerd_bee
            "guitar_bee" -> R.drawable.profile_guitar_bee
            "ai_bee" -> R.drawable.ai_bee
            else -> R.drawable.ai_bee
        }
    }

    // 1) These hold the live database data
    var user by remember { mutableStateOf<UserDetail?>(null) }
    var userOrig by remember { mutableStateOf<UserDetail?>(null) }
    var group by remember { mutableStateOf<GroupDetail?>(null) }
    var groupOrig by remember { mutableStateOf<GroupDetail?>(null) }

    // 2) Load from backend ONCE when the composable first appears
    LaunchedEffect(Unit) {
        try {
            Log.d("SettingsScreen", "LaunchedEffect STARTED")
            val userResp = ApiClient.instance.getUser(mapOf("userId" to deviceId))
            Log.d("SettingsScreen", "Fetched user = ${userResp.user}")
            user = userResp.user
            userOrig = userResp.user

            val groupResp = ApiClient.instance.getGroup(mapOf("groupId" to groupId))
            Log.d("SettingsScreen", "Fetched group = ${groupResp.group}")
            group = groupResp.group
            groupOrig = groupResp.group
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (user == null || group == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...",
                color = MaterialTheme.colorScheme.onSecondary,
                fontFamily = CooperBt //font added
            )
        }
        return
    }

    var currentUserName by remember { mutableStateOf(user!!.name) }
    var currentGroupName by remember { mutableStateOf(group!!.groupName) }
//    var currentGroupId by remember { mutableStateOf(group.groupId) }
    val currentGroupId = group!!.groupId
    var selectedProfilePic by remember { mutableStateOf(getProfilePicResId(user?.profilePic ?: "ai_bee")) }
    var showDropdown by remember { mutableStateOf(false) }


    val profileOptions = listOf(
        R.drawable.profile_happy_bee,
        R.drawable.profile_honey_bee,
        R.drawable.profile_cool_bee,
        R.drawable.profile_heart_bee,
        R.drawable.profile_nerd_bee,
        R.drawable.profile_guitar_bee
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 80.dp)
        ) {
            // profile pic (needs to be added to the db as well so that it persists and visible to all grp mates)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                    .clickable(enabled = editable) {
                        if (editable) showDropdown = !showDropdown
                    }
            ) {
                Image(
                    painter = painterResource(id = selectedProfilePic),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(80.dp)
                )

                // Dropdown of profile options
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)

                ) {
                    profileOptions.forEach { pic ->
                        DropdownMenuItem(
                            text = { Text("Option ${profileOptions.indexOf(pic) + 1}") },
                            onClick = {
                                selectedProfilePic = pic
                                showDropdown = false

                                scope.launch {
                                    try {
                                        val profilePicString = getProfilePicString(pic)
                                        val response = ApiClient.instance.updateProfilePic(
                                            UpdateProfilePicRequest(
                                                deviceId = deviceId,
                                                profilePic = profilePicString
                                            )
                                        )
                                        Log.d("SettingsScreen", "Profile picture updated: ${response.message}")

                                        user = user?.copy(profilePic = profilePicString)
                                        userOrig = userOrig?.copy(profilePic = profilePicString)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Log.e("SettingsScreen", "Failed to update profile picture", e)
                                    }
                                }
                            },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = pic),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                )
                            }
                        )
                    }
                }
            }

            // Card with group info
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                    },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.onPrimary
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //user name text field
                    OutlinedTextField(
                        value = currentUserName,
                        onValueChange = { currentUserName = it },
                        label = {
                            Text(
                                "Username",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontFamily = CooperBt
                            )
                        }, //font added
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSecondary),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            //unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = editable
                    )

                    //grp name field
                    OutlinedTextField(
                        value = currentGroupName,
                        onValueChange = { currentGroupName = it},
                        label = {
                            Text(
                                "Group Name",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontFamily = CooperBt //font added
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSecondary),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            //unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = editable
                    )

                    val context = LocalContext.current
                    // grp id field
                    OutlinedTextField(
                        value = TextFieldValue(currentGroupId),
                        onValueChange = {}, //Read Only
                        label = {
                            Text(
                                "Group ID",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontFamily = CooperBt
                            )
                        }, //font added
                        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSecondary),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowOutward,
                                contentDescription = "Share Group ID",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join my Hive group! Group ID: $currentGroupId"
                                            )
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    }

                            )
                        }
                    )

                    // Dark Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Sun Icon (Light Mode)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lightmode),
                            contentDescription = "Light Mode",
                            tint = if (!darkMode) MaterialTheme.colorScheme.onSecondary else Color.Gray,
                            modifier = Modifier.size(25.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = darkMode,
                            onCheckedChange = {
                                darkMode = it
                                onDarkModeChange(it)
                            },

                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Moon Icon (Dark Mode)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_darkmode),
                            contentDescription = "Dark Mode",
                            tint = if (darkMode) MaterialTheme.colorScheme.onSecondary else Color.Gray,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            // Edit/Save Button
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.End
            ) {
                AssistChip(
                    onClick = {
                        if (editable) {
                            scope.launch {
                                try {
                                    val userChanged = currentUserName != userOrig!!.name
                                    val groupChanged = currentGroupName != groupOrig!!.groupName

                                    if (!userChanged && !groupChanged) {
                                        editable = false
                                        return@launch
                                    }

                                    if (userChanged) {
                                        ApiClient.instance.updateUserName(
                                            UpdateUserNameRequest(
                                                userId = deviceId,
                                                newName = currentUserName
                                            )
                                        )
                                    }

                                    if (groupChanged) {
                                        ApiClient.instance.updateGroupName(
                                            UpdateGroupNameRequest(
                                                groupId = groupId,
                                                deviceId = deviceId,
                                                newName = currentGroupName
                                            )
                                        )
                                    }

                                    // Update local originals to reflect successful save
                                    if (userChanged) userOrig = userOrig!!.copy(name = currentUserName)
                                    if (groupChanged) groupOrig = groupOrig!!.copy(groupName = currentGroupName)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        editable = !editable

                    },
                    label = {
                        Text(
                            if (editable) "Save" else "Edit",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontFamily = CooperBt //font Added
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (editable) Icons.Filled.Check else Icons.Filled.Edit,
                            contentDescription = if (editable) "Save" else "Edit",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (showDialog) {
            val textColor = if (darkModeState) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Delete Hive?", color = MaterialTheme.colorScheme.onSecondary) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = deletionIntent,
                            onValueChange = { deletionIntent = it },
                            label = { Text("Type: I want this gone!", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                },
                confirmButton = {
                    val buttonColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }

                    val textColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        if (deletionIntent == "I want this gone!") {
                            scope.launch {
                                try {
                                    ApiClient.instance.deleteGroup(
                                        mapOf(
                                            "groupId" to groupId,
                                            "deviceId" to deviceId
                                        )
                                    )

                                    // Clear stored group ID (auto-logout from hive)
                                    clearGroupId(context)

                                    // Navigate back to login
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    context.startActivity(intent)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Add")
                    }
                },

                dismissButton = {
                    val buttonColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onTertiary.copy(alpha=0.15f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    }

                    val textColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        deletionIntent = ""
                        showDialog = false
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                titleContentColor = MaterialTheme.colorScheme.onSecondary,
                textContentColor = MaterialTheme.colorScheme.onSecondary
            )
        }

        if (showDialogLeave) {
            val textColor = if (darkModeState) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
            AlertDialog(
                onDismissRequest = { showDialogLeave = false },
                title = { Text("Leave Hive?", color = MaterialTheme.colorScheme.onSecondary) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = leaveIntent,
                            onValueChange = { leaveIntent = it },
                            label = { Text("Type: LEAVE", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                },
                confirmButton = {
                    val buttonColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }

                    val textColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        if (leaveIntent == "LEAVE") {
                            scope.launch {
                                try {

                                    val leaveGroup = LeaveGroupRequest(
                                        groupId = groupId,
                                        deviceId = deviceId
                                    )

                                    val res = ApiClient.instance.leaveGroup(leaveGroup)
                                    Log.d("SettingsScreen", "Leave Group Response: $res")

                                    // Clear stored group ID (auto-logout from hive)
                                    clearGroupId(context)

                                    // Navigate back to login
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    context.startActivity(intent)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Leave")
                    }
                },

                dismissButton = {
                    val buttonColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onTertiary.copy(alpha=0.15f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    }

                    val textColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        leaveIntent = ""
                        showDialogLeave = false
                    },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                titleContentColor = MaterialTheme.colorScheme.onSecondary,
                textContentColor = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Bottom Bar with Home button
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
                // Delete Button (Left)
                Button(
                    onClick = { showDialog = true },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                // Home Button (Center)
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

                // Logout Button (Right)
                Button(
                    onClick = { /* TODO: logout alert dialog then logout */
                        showDialogLeave = true
                    },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }


}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_NO,
    name = "Light Mode"
)
fun SettingsScreenPreviewLight() {
    var darkMode by remember { mutableStateOf(false) }
    HiveTheme(darkTheme = darkMode) {
        SettingsScreen(
//            group = GroupRequest(
//                groupId = "placeholder",
//                creatorName = "User1",
//                groupName = "Grp1",
//                creatorId = "placeholder",
////                peopleList = listOf()
//            ),
            deviceId = "placeholder",
            groupId = "placeholder",
            onNavigateToHome = {},
            darkModeState = darkMode,
            onDarkModeChange = { darkMode = it }
        )
    }
}

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark Mode"
)
fun SettingsScreenPreviewDark() {
    HiveTheme {
        SettingsScreen(
//            group = GroupRequest(
//                groupId = "placeholder",
//                creatorName = "User1",
//                groupName = "Grp1",
//                creatorId = "placeholder",
////                peopleList = listOf()
//            ),
            deviceId = "placeholder",
            groupId = "placeholder",
            onNavigateToHome = {},
            darkModeState = true,
            onDarkModeChange = {}
        )
    }
}