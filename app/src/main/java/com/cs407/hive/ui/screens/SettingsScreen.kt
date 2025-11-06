package com.cs407.hive.ui.screens

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    group: GroupRequest,
    onNavigateToHome: () -> Unit,
    darkModeState: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {

    var editable by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(darkModeState) }
    var currentUserName by remember { mutableStateOf(group.creatorName) }
    var currentGroupName by remember { mutableStateOf(group.groupName) }
    var currentGroupId by remember { mutableStateOf(group.groupId) }
    var selectedProfilePic by remember { mutableStateOf(R.drawable.ai_bee) }
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
                        value = TextFieldValue(currentUserName),
                        onValueChange = { currentUserName = it.text },
                        label = { Text("Username", color = MaterialTheme.colorScheme.onSecondary) },
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
                        value = TextFieldValue(currentGroupName),
                        onValueChange = { currentGroupName = it.text },
                        label = {
                            Text(
                                "Group Name",
                                color = MaterialTheme.colorScheme.onSecondary
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
                        onValueChange = {
                            currentGroupId = it.text
                        }, //Need to connect to db to change to a new grp if possible
                        label = { Text("Group ID", color = MaterialTheme.colorScheme.onSecondary) },
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
                    onClick = { editable = !editable },
                    label = {
                        Text(
                            if (editable) "Save" else "Edit",
                            color = MaterialTheme.colorScheme.onSecondary
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
                    onClick = { /* TODO: delete account but alert to all grp mates and a confirmation dialog */ },
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
                    onClick = { /* TODO: logout alert dialog then logout */ },
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
            group = GroupRequest(
                groupId = "placeholder",
                creatorName = "User1",
                groupName = "Grp1",
                peopleList = listOf()
            ),
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
            group = GroupRequest(
                groupId = "placeholder",
                creatorName = "User1",
                groupName = "Grp1",
                peopleList = listOf()
            ),
            onNavigateToHome = {},
            darkModeState = true,
            onDarkModeChange = {}
        )
    }
}