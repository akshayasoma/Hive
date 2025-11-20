package com.cs407.hive.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.data.network.HiveApi
import com.cs407.hive.ui.theme.HiveTheme
import kotlinx.coroutines.launch

@Composable
fun ChoresScreen(deviceId: String, groupId: String,onNavigateToHome: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chores by remember { mutableStateOf(listOf<Triple<String, String, String>>()) }
    var showInfo by remember { mutableStateOf(false) }


    val api = remember { ApiClient.instance }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = api.getGroup(mapOf("groupId" to groupId))
                val serverChores = response.group.chores ?: emptyList()

                chores = serverChores.map { chore ->
                    Triple(chore.name, chore.points.toString(), chore.description)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

            // Info icon (top left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
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
                text = "CHORES",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                if (chores.isNotEmpty()) {
                    items(chores.reversed()) { (name, pts, desc) ->
                        Log.d("ChoresScreen", "Chore: $name, Points: $pts, Description: $desc")
                        ChoreCard(
                            username = "Unassigned",
                            chore = name,
                            points = "$pts pts",
                            status = "To do",
                            description = desc
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No chores yet! Add chores using the button below!",
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(32.dp)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* TODO: enable delete mode */ },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)

                    )
                }

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

                // Add Button
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
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Chore",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
        if (showDialog) {
            val textColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "Add a New Chore",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },

                text = {
                    Column {
                        OutlinedTextField(
                            value = choreName,
                            onValueChange = { choreName = it },
                            label = { Text("Chore Name", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = points,
                            onValueChange = { points = it },
                            label = { Text("Points", color = textColor) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = textColor) },
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        )
                    }
                },
                confirmButton = {
                    val buttonColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }

                    val textColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(onClick = {
                        if (choreName.isNotBlank() && points.isNotBlank()) {
                            val formattedName = choreName.split(" ")
                                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            Log.d("ChoresScreen", "Chore added: $formattedName, $points, $description")
                            val request = AddChoreRequest(
                                groupId = groupId,
                                deviceId = deviceId,
                                name = formattedName,
                                description = description,
                                points = points.toInt()

                            )

                            scope.launch {
                                try{
                                    api.addChore(request)
                                    Log.d("ChoresScreen", "Chore added: $formattedName, $points, $description")

//                                    chores = chores + Triple(formattedName, points, description)
                                    val updatedResponse = api.getGroup(mapOf("groupId" to groupId))
                                    val updatedChores = updatedResponse.group.chores ?: emptyList()
                                    chores = updatedChores.map { Triple(it.name, it.points.toString(), it.description) }
                                } catch (e: Exception) {
                                    e.printStackTrace()

                                }
                                finally{
                                    choreName = ""
                                    points = ""
                                    description = ""
                                }
                            }

                        }



                        showDialog = false
                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = buttonColor,
                        contentColor = textColor
                    )
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    val buttonColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onTertiary.copy(alpha=0.15f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    }

                    val textColor = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onSecondary
                    } else {
                        MaterialTheme.colorScheme.onSecondary
                    }
                    TextButton(
                        onClick = {
                        choreName = ""
                        points = ""
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
        //info box
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val addIconId = Icons.Default.Add
            val deleteIconId = Icons.Default.Delete

            val addIconKey = "addIcon"
            val deleteIconKey = "deleteIcon"

            val infoText = buildAnnotatedString {
                append("• Tap ")
                appendInlineContent(addIconKey, "[add]")
                append(" to add a new chore\n")

                append("• Tap ")
                appendInlineContent(deleteIconKey, "[delete]")
                append(" to enter delete mode")

                // Sub-bullet for delete
                val subBullet = "◦ Swipe RIGHT on a chore to delete it"
                addStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(firstLine = 24.sp, restLine = 34.sp),
                        lineHeight = 20.sp
                    ),
                    start = length,
                    end = length + subBullet.length
                )
                append(subBullet)

                val bulletThree = "• Shake phone to randomly assign chores"
                addStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(restLine = 10.sp),

                    ),
                    start = length,
                    end = length + bulletThree.length
                )
                append(bulletThree)
            }

            val inlineContent = mapOf(
                addIconKey to InlineTextContent(
                    Placeholder(
                        width = 18.sp,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = addIconId,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                },
                deleteIconKey to InlineTextContent(
                    Placeholder(
                        width = 18.sp,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Icon(
                        imageVector = deleteIconId,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            )
            // background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .clickable { showInfo = false }
            )

            // info card
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .padding(28.dp)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HOW TO USE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = infoText,
                            inlineContent = inlineContent,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun ChoreCard(
    username: String,
    chore: String,
    points: String,
    status: String,
    description: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Always visible section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile icon
                    Box(
                        modifier = Modifier
                            .border(2.dp, MaterialTheme.colorScheme.onSecondary, CircleShape)
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐝",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = chore.uppercase(),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = username,
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


            AnimatedVisibility(
                visible = isExpanded && description.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Description:",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
fun ChoresPreviewLight() {
    HiveTheme(dynamicColor = false) {
        ChoresScreen( deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun ChoresPreviewDark() {
    HiveTheme(dynamicColor = false) {
        ChoresScreen(deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
    }
}
