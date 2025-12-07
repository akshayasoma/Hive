package com.cs407.hive.ui.screens

import android.R.attr.name
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.R
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.model.DeleteChoreRequest
import com.cs407.hive.data.model.UiChore
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.data.network.HiveApi
import com.cs407.hive.ui.theme.HiveTheme
import com.cs407.hive.workers.WorkerTestUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun ChoresScreen(deviceId: String, groupId: String,onNavigateToHome: () -> Unit,darkModeState: Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
//    var chores by remember { mutableStateOf(listOf<Triple<String, String, String>>()) }
    var chores by remember { mutableStateOf(listOf<UiChore>()) }
    var showInfo by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(false) }
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    var choreNameError by remember { mutableStateOf<String?>(null) }
    var pointsError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val api = remember { ApiClient.instance }
    val scope = rememberCoroutineScope()

    // Keyboard configuration for points field
    val keyboardController = LocalSoftwareKeyboardController.current

    // Validation functions
    fun validateChoreName(): Boolean {
        return if (choreName.isBlank()) {
            choreNameError = "Chore name cannot be empty!"
            false
        } else if (choreName.length > 30) {
            choreNameError = "Chore name too long!"
            false
        } else {
            choreNameError = null
            true
        }
    }

    fun validatePoints(): Boolean {
        return if (points.isBlank()) {
            pointsError = "Points cannot be empty!"
            false
        } else if (!points.matches(Regex("^\\d+\$"))) {
            pointsError = "Only numbers are allowed!"
            false
        } else if (points.toIntOrNull() == null) {
            pointsError = "Invalid number!"
            false
        } else if (points.toInt() <= 0) {
            pointsError = "Points must be greater than 0!"
            false
        } else {
            pointsError = null
            true
        }
    }

    fun validateDescription(): Boolean {
        return if (description.length > 50) {
            descriptionError = "Description too long!"
            false
        } else {
            descriptionError = null
            true
        }
    }

    fun validateAll(): Boolean {
        val nameValid = validateChoreName()
        val pointsValid = validatePoints()
        val descValid = validateDescription()
        return nameValid && pointsValid && descValid
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = api.getGroup(mapOf("groupId" to groupId))
                val serverChores = response.group.chores ?: emptyList()

//                chores = serverChores.map { chore ->
//                    Triple(chore.name, chore.points.toString(), chore.description)
//                }
                chores = serverChores.map { chore ->
                    UiChore(
                        name = chore.name,
                        description = chore.description,
                        points = chore.points,
                        status = chore.status,
                        assignee = chore.assignee
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(chores) {
        if (chores.isEmpty()) {
            deleteMode = false
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
                fontFamily = CooperBt, //font added
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
                    items(
                        items=chores.reversed(),
                        key={chore -> "${chore.name}/${chore.points}/${chore.description}/${chore.status}/${chore.assignee}"}
                    ) { chore ->
                        Log.d("ChoresScreen", "Chore: ${chore.name}, Points: ${chore.points}, Description: ${chore.description}")
                        ChoreCard(
                            username = chore.assignee.ifBlank { "Unassigned" },
                            chore = chore.name,
                            points = "${chore.points} pts",
                            status = when (chore.status) {
                                0 -> "To do"
                                1 -> "In progress"
                                2 -> "Done"
                                else -> "Unknown"
                            },
                            description = chore.description,
                            deleteMode = deleteMode,
                            onDelete = {
                                //TODO: Delete the Chore in db
                                scope.launch {
                                    try{
                                        val deleteChore = DeleteChoreRequest(
                                            groupId = groupId,
                                            deviceId = deviceId,
                                            choreName = chore.name,
                                            description = chore.description,
                                            points = chore.points,
                                            status = chore.status,
                                            assignee = chore.assignee
                                        )
                                        api.deleteChore(deleteChore)

                                        val updatedResponse = api.getGroup(mapOf("groupId" to groupId))
                                        val updatedChores = updatedResponse.group.chores ?: emptyList()

                                        chores = updatedChores.map {
                                            UiChore(
                                                name = it.name,
                                                description = it.description,
                                                points = it.points,
                                                status = it.status,
                                                assignee = it.assignee
                                            )
                                        }

                                        Log.d("ChoresScreen", "Chore successfully deleted: ${chore.name}")
                                    }
                                    catch(e: Exception){
                                        Log.e("ChoresScreen", "Error deleting chore: $e")
                                    }

                                }
                            },
                            groupMembers = listOf("User1", "User2", "User3"), // Replace with actual group members
                            onAssignUser = { assignedUser: String ->
                                scope.launch {
                                    try {
                                        if (assignedUser.isEmpty()) {
                                            // Keep as Unassigned
                                            toastMessage = "Chore '$name' is unassigned"
                                        } else {
                                            toastMessage = "Assigned $name to $assignedUser"
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        toastMessage = "Failed to assign user: ${e.message}"
                                    }
                                }
                            },
                            onChangeStatus = { newStatus: String ->
                                scope.launch {
                                    try {
                                        val statusInt = when (newStatus) {
                                            "To do" -> 0
                                            "In progress" -> 1
                                            "Completed" -> 2
                                            else -> 0
                                        }
                                        toastMessage = "Updated $name status to $newStatus"
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        toastMessage = "Failed to update status: ${e.message}"
                                    }
                                }
                            },
                            darkModeState = darkModeState
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No chores yet! Add chores using the button below!",
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
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
                    onClick = { deleteMode = !deleteMode },
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
                    onClick = {
                        choreNameError = null
                        pointsError = null
                        descriptionError = null
                        showDialog = true },
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
            val textColor = if (darkModeState) {
                MaterialTheme.colorScheme.onTertiary
            } else {
                MaterialTheme.colorScheme.onSecondary
            }
            val borderColor = if (darkModeState) {
                Color.LightGray
            } else {
                Color.DarkGray
            }
            AlertDialog(
                onDismissRequest = {
                    choreNameError = null
                    descriptionError = null
                    showDialog = false },
                title = {
                    Text(
                        "Add a New Chore",
                        fontFamily = CooperBt, //font added
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },

                text = {
                    Column {
                        // Chore Name Field with Error
                        val errorColor = if (darkModeState) Color.Red else MaterialTheme.colorScheme.error
                        if (choreNameError != null) {
                            Text(
                                text = choreNameError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        OutlinedTextField(
                            value = choreName,
                            onValueChange = { newValue ->
                                choreName = newValue.take(31)
                                validateChoreName()
                                            },
                            label = { Text("Chore Name",
                                fontFamily = CooperBt, color = textColor) }, //font added
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = choreNameError != null,
                            trailingIcon = {
                                if (choreNameError != null) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = errorColor
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color.DarkGray,
                                errorBorderColor = Color.Red
                            ),
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Points Field with Error
                        if (pointsError != null) {
                            Text(
                                text = pointsError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = points,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d+\$"))) {
                                    points = newValue
                                }
                                validatePoints()
                                            },
                            label = { Text("Points", fontFamily = CooperBt, color = textColor) }, //font added
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = pointsError != null,
                            trailingIcon = {
                                if (pointsError != null) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = errorColor
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color.DarkGray,
                                errorBorderColor = Color.Red
                            ),
                            // Set keyboard to number pad
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Description Field with Error
                        if (descriptionError != null) {
                            Text(
                                text = descriptionError!!,
                                color = errorColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { newValue ->
                                description = newValue.take(51)
                                validateDescription()
                                            },
                            label = { Text("Description",
                                fontFamily = CooperBt, color = textColor) }, //font added
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            isError = descriptionError != null,
                            trailingIcon = {
                                if (descriptionError != null) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = errorColor
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.DarkGray,
                                unfocusedBorderColor = Color.DarkGray,
                                errorBorderColor = Color.Red
                            ),
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
                        // Clear previous errors first
                        choreNameError = null
                        pointsError = null
                        descriptionError = null

                        // Run all validations
                        val nameValid = validateChoreName()
                        val pointsValid = validatePoints()
                        val descValid = validateDescription()

                        val allValid = nameValid && pointsValid && descValid

                        if (allValid) {
                                if (choreName.isNotBlank() && points.isNotBlank()) {
                                    val formattedName = choreName.split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                    Log.d(
                                        "ChoresScreen",
                                        "Chore added: $formattedName, $points, $description"
                                    )
                                    val request = AddChoreRequest(
                                        groupId = groupId,
                                        deviceId = deviceId,
                                        name = formattedName,
                                        description = description,
                                        points = points.toInt()

                                    )

                                    scope.launch {
                                        try {
                                            api.addChore(request)
                                            Log.d(
                                                "ChoresScreen",
                                                "Chore added: $formattedName, $points, $description"
                                            )

//                                    chores = chores + Triple(formattedName, points, description)
                                            val updatedResponse =
                                                api.getGroup(mapOf("groupId" to groupId))
                                            val updatedChores =
                                                updatedResponse.group.chores ?: emptyList()
//                                    chores = updatedChores.map { Triple(it.name, it.points.toString(), it.description) }
                                            chores = updatedChores.map {
                                                UiChore(
                                                    name = it.name,
                                                    description = it.description,
                                                    points = it.points,
                                                    status = it.status,
                                                    assignee = it.assignee
                                                )
                                            }
                                            toastMessage =
                                                "Chore '$formattedName' created successfully"
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            toastMessage = "Failed to create chore: ${e.message}"
                                        } finally {
                                            choreName = ""
                                            points = ""
                                            description = ""
                                            choreNameError = null
                                            pointsError = null
                                            descriptionError = null
                                        }
                                    }
                                    showDialog = false
                                }
                        }

                    }, colors = ButtonDefaults.textButtonColors(
                        containerColor = buttonColor,
                        contentColor = textColor
                    )
                    ) {
                        Text("Add", fontFamily = CooperBt,) //font added
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
                    TextButton(
                        onClick = {
                            choreName = ""
                            points = ""
                            choreNameError = null
                            pointsError = null
                            descriptionError = null
                            showDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel", fontFamily = CooperBt,) //font added
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
                val subBullet = "◦ Swipe LEFT on a chore to delete it"
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
                            fontFamily = CooperBt, //font added
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = infoText,
                            inlineContent = inlineContent,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontFamily = CooperBt, //font added
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Test button for worker (development only)
                        Button(
                            onClick = {
                                WorkerTestUtils.runChoreCheckNow(context, groupId, deviceId)
                                toastMessage = "Testing worker - check for notification!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test Notifications", fontFamily = CooperBt, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreCard(
    username: String,
    chore: String,
    points: String,
    status: String,
    description: String,
    deleteMode: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    groupMembers: List<String> = emptyList(),
    onAssignUser: (String) -> Unit = {},
    onChangeStatus: (String) -> Unit = {},
    darkModeState : Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var currentAssignee by remember { mutableStateOf(username) }
    var currentStatus by remember { mutableStateOf(status) }
    val isCompleted = currentStatus == "Completed"

    val statusOptions = listOf("To do", "In progress", "Completed")
    val scope = rememberCoroutineScope()

    //Shaking Animation Variables
    val rotation = remember { Animatable(0f) }
    var shaking by remember { mutableStateOf(false) }

    // swipe animation
    val swipeOffset = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(1f) }
    val maxDragX = 150.dp

    //colors for interpolation
    val defaultCardColor = MaterialTheme.colorScheme.onPrimary
    val deleteColor = MaterialTheme.colorScheme.error
    val density = LocalDensity.current

    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    // Control shaking effect
    LaunchedEffect(deleteMode) {
        if (deleteMode) {
            shaking = true
            //nfinite rotation between 0 and 1
            rotation.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse // from 0 to 1, then 1 back to 0
                )
            )
        } else {
            shaking = false
            rotation.stop()
            rotation.snapTo(0f)
            swipeOffset.snapTo(0f)
            cardAlpha.snapTo(1f)
        }
    }

    val swipeToDeleteModifier = Modifier.pointerInput(Unit) {
        val maxDragXPx = maxDragX.toPx()

        detectHorizontalDragGestures(
            onDragEnd = {
                if (swipeOffset.value < -maxDragXPx / 2) {
                    //deletion animation
                    scope.launch {
                        swipeOffset.animateTo(-size.width.toFloat(), tween(300))
                        cardAlpha.animateTo(0f, tween(100))
                        onDelete() // DELETE FROM DB CALLED HERE
                    }
                } else {
                    scope.launch {
                        swipeOffset.animateTo(0f, tween(200))
                    }
                }
            },
            onDragCancel = {
                scope.launch {
                    swipeOffset.animateTo(0f, tween(200))
                }
            }
        ) { change, dragAmount ->
            change.consume()
            if (deleteMode) {
                scope.launch {
                    //right-to-left
                    val newOffset = (swipeOffset.value + dragAmount).coerceAtMost(0f)
                    swipeOffset.snapTo(newOffset)
                }
            }
        }
    }

    //progress of the color change (0.0 to 1.0)
    val swipeProgress = (-swipeOffset.value / with(density) { maxDragX.toPx()}).coerceIn(0f, 1f)

    //interpolated color based on swipe progress
    val interpolatedColor = lerp(defaultCardColor, deleteColor, swipeProgress)

    val cardGraphicsModifier = modifier
        .fillMaxWidth(0.9f)
        .then(if (deleteMode) swipeToDeleteModifier else Modifier)
        .graphicsLayer(
            //rotation for shaking
            rotationZ = if (shaking && deleteMode) (rotation.value * 2f) - 1f else 0f,
            //swipe offset when dragging
            translationX = swipeOffset.value,
            //fade-out effect deletion
            alpha = cardAlpha.value * (if (isCompleted) 0.6f else 1f)
        )
        // horizontal offset for shaking
        .offset {
            with(density) {
                if (shaking && deleteMode) {
                    IntOffset(
                        x = (rotation.value * 2.dp.toPx()).roundToInt() - 1.dp.toPx().roundToInt(),
                        y = 0
                    )
                } else {
                    IntOffset.Zero
                }
            }
        }
        .clickable(enabled = !deleteMode) { isExpanded = !isExpanded }

    val contentTint = lerp(MaterialTheme.colorScheme.onSecondary, MaterialTheme.colorScheme.onError, swipeProgress)

    Card(
        modifier = cardGraphicsModifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = interpolatedColor
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
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)) {
                    // Profile icon
                    Box(
                        modifier = Modifier
                            .border(2.dp, contentTint, CircleShape)
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🐝",
                            color = contentTint,
                            fontFamily = CooperBt, //font added
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = chore.uppercase(),
                            color = contentTint,
                            fontFamily = CooperBt,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                            )
                        )
                        Text(
                            text = currentAssignee,
                            color = contentTint,
                            //fontFamily = CooperBt, //font added
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = points,
                        color = contentTint,
                        fontFamily = CooperBt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        text = currentStatus,
                        color = when (currentStatus) {
                            "To do" -> androidx.compose.ui.graphics.Color(0xFFF44336)
                            "In progress" -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                            "Completed" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            else -> contentTint
                        },
                        //fontFamily = CooperBt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Description section
                    if (description.isNotBlank()) {
                        Text(
                            text = "Description:",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontFamily = CooperBt,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                            //fontFamily = CooperBt,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Button(
                            onClick = { showAssignDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSecondary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Edit/Assign",
                                fontFamily = CooperBt,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
    if (showAssignDialog) {
        val textColor = if (darkModeState) {
            MaterialTheme.colorScheme.onTertiary
        } else {
            MaterialTheme.colorScheme.onSecondary
        }

        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = {
                Text(
                    "Assign & Update Chore",
                    fontFamily = CooperBt,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            },
            text = {
                Column {
                    // Assignee Section
                    Text(
                        text = "Assignee:",
                        fontFamily = CooperBt,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Assignee options as radio buttons
                    Column {
                        // Unassigned option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentAssignee = "Unassigned"
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAssignee == "Unassigned",
                                onClick = { currentAssignee = "Unassigned" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unassigned",
                                color = MaterialTheme.colorScheme.onSecondary,
                                //fontFamily = CooperBt,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp
                            )
                        }

                        // Group members options
                        groupMembers.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentAssignee = member
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentAssignee == member,
                                    onClick = { currentAssignee = member }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = member,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Section
                    Text(
                        text = "Status:",
                        fontFamily = CooperBt,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Status options as radio buttons
                    Column {
                        statusOptions.forEach { statusOption ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentStatus = statusOption
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentStatus == statusOption,
                                    onClick = { currentStatus = statusOption }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusOption,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Call both callbacks
                        onAssignUser(currentAssignee)
                        onChangeStatus(currentStatus)
                        showAssignDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save", fontFamily = CooperBt)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAssignDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Cancel", fontFamily = CooperBt)
                }
            },
            containerColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onSecondary,
            textContentColor = MaterialTheme.colorScheme.onSecondary
        )
    }
}

//@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
//@Composable
//fun ChoresPreviewLight() {
//    HiveTheme(dynamicColor = false) {
//        ChoresScreen( deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
//    }
//}

//@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
//@Composable
//fun ChoresPreviewDark() {
//    HiveTheme(dynamicColor = false) {
//        ChoresScreen(deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
//    }
//}