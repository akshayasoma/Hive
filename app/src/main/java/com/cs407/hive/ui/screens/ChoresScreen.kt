package com.cs407.hive.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.R
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.model.CompleteChoreRequest
import com.cs407.hive.data.model.DeleteChoreRequest
import com.cs407.hive.data.model.GetUserNamesRequest
import com.cs407.hive.data.model.UiChore
import com.cs407.hive.data.model.UpdateChoreAssigneeRequest
import com.cs407.hive.data.model.UpdateChoreRequest
import com.cs407.hive.data.model.UpdateProfilePicRequest
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.workers.WorkerTestUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun ChoresScreen(
    deviceId: String,
    groupId: String,
    onNavigateToHome: () -> Unit,
    darkModeState: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chores by remember { mutableStateOf(listOf<UiChore>()) }
    var showInfo by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(false) }
    var groupMembers by remember { mutableStateOf<List<String>>(emptyList()) }
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    var choreNameError by remember { mutableStateOf<String?>(null) }
    var pointsError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val api = remember { ApiClient.instance }
    val scope = rememberCoroutineScope()
    var userMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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

    suspend fun fetchUserNames(): Map<String, String> {
        val newUserMap = mutableMapOf<String, String>()

        try {
            val response = api.getUserNames(
                GetUserNamesRequest(
                    groupId = groupId,
                    deviceId = deviceId
                )
            )

            Log.d("ChoresScreen", "getUserNames API returned names: ${response.names}")

            // Get member IDs from group
            val groupResponse = api.getGroup(mapOf("groupId" to groupId))
            val memberIds = groupResponse.group.peopleList ?: emptyList()

            Log.d("ChoresScreen", "Group member IDs: $memberIds")

            memberIds.forEachIndexed { index, deviceId ->
                if (index < response.names.size) {
                    val username = response.names[index]
                    if (username.isNotBlank()) {  // Only add if username is not empty
                        newUserMap[deviceId] = username
                    }
                } else {
                    // Don't create "User X" names - skip or use device ID
                    newUserMap[deviceId] = deviceId.takeLast(4) // Optional: use last 4 chars
                }
            }

        } catch (e: Exception) {
            Log.e("ChoresScreen", "Error fetching usernames: $e")
        }

        return newUserMap
    }

    // Function to refresh chores and members
    suspend fun refreshChoresAndMembers() {
        try {
            val response = api.getGroup(mapOf("groupId" to groupId))
            val serverChores = response.group.chores ?: emptyList()
            val memberIds = response.group.peopleList ?: emptyList()

            Log.d("ChoresScreen", "Group member IDs: $memberIds")

            // Fetch usernames for all member IDs
            val fetchedUserMap = fetchUserNames()
            userMap = fetchedUserMap

            // Create a list of usernames
            groupMembers = fetchedUserMap.values.toList().filter { it.isNotBlank() }.distinct()

            Log.d("ChoresScreen", "Group members (usernames): $groupMembers")

            val uniqueChores = mutableMapOf<String, UiChore>()

            serverChores.forEach { chore ->
                val normalizedName = chore.name.trim().lowercase()
                val normalizedDesc = chore.description.trim().lowercase()
                val key = "$normalizedName|$normalizedDesc"

                if (!uniqueChores.containsKey(key)) {
                    val assigneeUsername = if (chore.assignee.isNotBlank()) {
                        chore.assignee ?: "Unknown User"
                    } else {
                        ""
                    }

                    uniqueChores[key] = UiChore(
                        name = chore.name.trim(),
                        description = chore.description.trim(),
                        points = chore.points,
                        status = chore.status,
                        assignee = assigneeUsername,
                        profilePic = ""
                    )
                } else {
                    Log.w("ChoresScreen", "DUPLICATE REJECTED: $normalizedName")
                }
            }

            chores = uniqueChores.values.toList()
            Log.d("ChoresScreen", "Loaded ${chores.size} chores with usernames")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ChoresScreen", "Error loading data: $e")
            toastMessage = "Failed to load data: ${e.message}"
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            try {
                val response = api.getGroup(mapOf("groupId" to groupId))
                val serverChores = response.group.chores ?: emptyList()
                val memberIds = response.group.peopleList ?: emptyList()

                Log.d("ChoresScreen", "Initial load - member IDs: $memberIds")

                // Fetch usernames
                val fetchedUserMap = fetchUserNames()
                userMap = fetchedUserMap

                // Create username list
                groupMembers = fetchedUserMap.values.toList().filter { it.isNotBlank() }.distinct()

                Log.d("ChoresScreen", "Initial load - usernames: $groupMembers")

                val uniqueChores = mutableMapOf<String, UiChore>()

                serverChores.forEach { chore ->
                    // Create a normalized key (case-insensitive, trimmed)
                    val normalizedName = chore.name.trim().lowercase()
                    val normalizedDesc = chore.description.trim().lowercase()
                    val key = "$normalizedName|$normalizedDesc"

                    if (!uniqueChores.containsKey(key)) {
                        // Convert assignee to username
                        val assigneeUsername = if (chore.assignee.isNotBlank()) {
                            chore.assignee ?: "Unknown User"
                        } else {
                            ""
                        }

                        uniqueChores[key] = UiChore(
                            name = chore.name.trim(),
                            description = chore.description.trim(),
                            points = chore.points,
                            status = chore.status,
                            assignee = assigneeUsername,
                            profilePic = ""
                        )
                    } else {
                        Log.w("ChoresScreen", "DUPLICATE REJECTED: $normalizedName")
                    }
                }

                chores = uniqueChores.values.toList()
                Log.d("ChoresScreen", "Initial load - loaded ${chores.size} chores")

            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage = "Failed to load data: ${e.message}"
            } finally {
                isLoading = false
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
                    onClick = { showInfo = !showInfo
                        scope.launch {
                            try{
                                val req = UpdateProfilePicRequest(
                                    deviceId = deviceId,
                                    profilePic = "imageUrlTest2.0"
                                )
                                val response = api.updateProfilePic(req)
                                Log.d("SettingsScreen", "Updated pic: ${response.profilePic}")


                            }
                            catch(e: Exception){
                                e.printStackTrace()
                                Log.e("ChoresScreen", "Error loading data: $e")
                            }
                        }
                    },
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
                fontFamily = CooperBt,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    if (chores.isNotEmpty()) {
                        items(
                            items = chores.reversed(),
                            key = { chore ->
                                // Use normalized key to match deduplication logic
                                "${chore.name.trim().lowercase()}|${chore.description.trim().lowercase()}"
                            }
                        ) { chore ->
                            val currentChore by rememberUpdatedState(chore)

                            ChoreCard(
                                username = currentChore.assignee.ifBlank { "Unassigned" },
                                chore = currentChore.name,
                                points = "${currentChore.points} pts",
                                status = when (currentChore.status) {
                                    0 -> "To do"
                                    1 -> "In progress"
                                    2 -> "Completed"
                                    else -> "Unknown"
                                },
                                description = currentChore.description,
                                deleteMode = deleteMode,
                                onDelete = {
                                    scope.launch {
                                        try {
                                            val deleteChore = DeleteChoreRequest(
                                                groupId = groupId,
                                                deviceId = deviceId,
                                                choreName = currentChore.name,
                                                description = currentChore.description,
                                                points = currentChore.points,
                                                status = currentChore.status,
                                                assignee = currentChore.assignee
                                            )

                                            api.deleteChore(deleteChore)

                                            // Remove by unique key instead of comparing all fields
                                            chores = chores.filterNot {
                                                "${it.name}|${it.description}|${it.assignee}" ==
                                                        "${currentChore.name}|${currentChore.description}|${currentChore.assignee}"
                                            }

                                            toastMessage = "Chore '${currentChore.name}' deleted"

                                        } catch (e: Exception) {
                                            Log.e("ChoresScreen", "Error deleting chore: $e")
                                            toastMessage = "Failed to delete chore: ${e.message}"
                                            // Refresh from server on error
                                            refreshChoresAndMembers()
                                        }
                                    }
                                },
                                groupMembers = groupMembers,
                                onAssignUser = { assignedUsername: String ->
                                    Log.d("ChoreScreen", "Assign clicked. username=$assignedUsername")
                                    scope.launch {
                                        try {
                                            // Convert username back to device ID for the API
                                            val assignedDeviceId = userMap.entries
                                                .firstOrNull { it.value == assignedUsername }?.key ?: ""

                                            val updateRequest = UpdateChoreAssigneeRequest(
                                                groupId = groupId,
                                                deviceId = deviceId,
                                                choreName = currentChore.name,
                                                description = currentChore.description,
                                                points = currentChore.points,
                                                newAssignee = assignedDeviceId
                                            )

                                            val response = api.updateChoreAssignee(updateRequest)


                                            // Update local state with username
//                                            chores = chores.map { c ->
//                                                if (c.name == currentChore.name &&
//                                                    c.description == currentChore.description &&
//                                                    c.assignee == currentChore.assignee) {
//                                                    c.copy(assignee = assignedUsername)
//                                                } else c
//                                            }
                                            chores = response.chores.map { c ->
                                                UiChore(
                                                    name = c.name,
                                                    description = c.description,
                                                    points = c.points,
                                                    status = c.status,
                                                    assignee = c.assignee,
                                                    profilePic = ""
                                                )
                                            }



                                            toastMessage = if (assignedUsername.isEmpty() || assignedUsername == "Unassigned") {
                                                "Chore '${currentChore.name}' is unassigned"
                                            } else {
                                                "Assigned ${currentChore.name} to $assignedUsername"
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            toastMessage = "Failed to assign user: ${e.message}"
                                        }
                                    }
                                },
                                onChangeStatus = { newStatus: String ->
                                    Log.d("ChoreScreen", "Assign clicked. newStatus=$newStatus")
                                    scope.launch {
                                        try {
                                            val statusInt = when (newStatus) {
                                                "To do" -> 0
                                                "In progress" -> 1
                                                "Completed" -> 2
                                                else -> 0
                                            }

                                            val updateRequest = AddChoreRequest(
                                                groupId = groupId,
                                                deviceId = deviceId,
                                                name = currentChore.name,
                                                description = currentChore.description,
                                                points = currentChore.points,
                                                assignee = currentChore.assignee,
                                                status = statusInt
                                            )

                                            api.addChore(updateRequest)

                                            if (statusInt == 2 && currentChore.assignee.isNotBlank() && currentChore.assignee != "Unassigned") {
                                                try {
                                                    val assignedDeviceId = userMap.entries
                                                        .firstOrNull { it.value == currentChore.assignee }?.key
                                                        ?: ""

                                                    if (assignedDeviceId.isNotBlank()) {
                                                        val completeRequest = CompleteChoreRequest(
                                                            groupId = groupId,
                                                            deviceId = assignedDeviceId,
                                                            choreName = currentChore.name,
                                                            description = currentChore.description,
                                                            points = currentChore.points
                                                        )

                                                        api.completeChore(completeRequest)

                                                        val userResponse = api.getUser(mapOf("deviceId" to assignedDeviceId))
                                                        val newUserPoints = userResponse.user.points

                                                        toastMessage = "Updated ${currentChore.name} status to $newStatus. " +
                                                                "${currentChore.assignee} now has $newUserPoints points!"
                                                    } else {
                                                        toastMessage = "Updated ${currentChore.name} status to $newStatus"
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("ChoresScreen", "Error updating points via completeChore: $e")
                                                    toastMessage = "Updated ${currentChore.name} status to $newStatus (points update failed)"
                                                }
                                            } else {
                                                toastMessage = "Updated ${currentChore.name} status to $newStatus"
                                            }

                                            chores = chores.map { c ->
                                                if (c.name == currentChore.name &&
                                                    c.description == currentChore.description &&
                                                    c.assignee == currentChore.assignee) {
                                                    c.copy(status = statusInt)
                                                } else c
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            toastMessage = "Failed to update status: ${e.message}"
                                        }
                                    }
                                },
                                onSubmitChoreChange = {newAssignee: String, newStatus: String ->
                                    scope.launch {
                                        try {
                                            val statusInt = when (newStatus) {
                                                "To do" -> 0
                                                "In progress" -> 1
                                                "Completed" -> 2
                                                else -> 0
                                            }
                                            val request = UpdateChoreRequest(
                                                groupId = groupId,
                                                deviceId = deviceId,
                                                name = currentChore.name,
                                                description = currentChore.description,
                                                points = currentChore.points,
                                                newAssignee = newAssignee,
                                                newStatus = statusInt
                                            )

                                            api.updateChore(request)

                                            // Award points when chore is marked as completed
                                            if (statusInt == 2 && newAssignee.isNotBlank() && newAssignee != "Unassigned") {
                                                try {
                                                    val assignedDeviceId = userMap.entries
                                                        .firstOrNull { it.value == newAssignee }?.key
                                                        ?: ""

                                                    if (assignedDeviceId.isNotBlank()) {
                                                        val completeRequest = CompleteChoreRequest(
                                                            groupId = groupId,
                                                            deviceId = assignedDeviceId,
                                                            choreName = currentChore.name,
                                                            description = currentChore.description,
                                                            points = currentChore.points
                                                        )

                                                        api.completeChore(completeRequest)

                                                        val userResponse = api.getUser(mapOf("deviceId" to assignedDeviceId))
                                                        val newUserPoints = userResponse.user.points

                                                        toastMessage = "Chore completed! $newAssignee now has $newUserPoints points!"
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("ChoreScreen", "Error updating points via completeChore: $e")
                                                }
                                            }

                                            chores = chores.map { c ->
                                                if (c.name == currentChore.name &&
                                                    c.description == currentChore.description &&
                                                    c.points == currentChore.points
                                                ) {
                                                    c.copy(assignee = newAssignee, status = statusInt)
                                                } else c
                                            }

                                        } catch (e: Exception) {
                                            Log.e("ChoreScreen", "Update failed", e)
                                        }
                                    }
                                },
                                darkModeState = darkModeState
                            )
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No chores yet!",
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add chores using the + button below!",
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f),
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp
                                )
                            }
                        }
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
                        containerColor = if (deleteMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary
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
                        showDialog = true
                    },
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
            val errorColor = if (darkModeState) Color.Red else MaterialTheme.colorScheme.error

            AlertDialog(
                onDismissRequest = {
                    choreNameError = null
                    descriptionError = null
                    showDialog = false
                },
                title = {
                    Text(
                        "Add a New Chore",
                        fontFamily = CooperBt,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                text = {
                    Column {
                        // Chore Name Field with Error
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
                            label = {
                                Text(
                                    "Chore Name",
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            },
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
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
                                unfocusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
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
                            label = {
                                Text(
                                    "Points",
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            },
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
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
                                unfocusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
                                errorBorderColor = Color.Red
                            ),
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
                            label = {
                                Text(
                                    "Description",
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            },
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
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
                                unfocusedBorderColor = if (darkModeState) Color.LightGray else Color.DarkGray,
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

                    val confirmTextColor = MaterialTheme.colorScheme.onSecondary

                    TextButton(
                        onClick = {
                            choreNameError = null
                            pointsError = null
                            descriptionError = null

                            val nameValid = validateChoreName()
                            val pointsValid = validatePoints()
                            val descValid = validateDescription()

                            val allValid = nameValid && pointsValid && descValid

                            if (allValid) {
                                scope.launch {
                                    try {
                                        val formattedName = choreName.split(" ")
                                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

                                        val normalizedNewName = formattedName.trim().lowercase()
                                        val normalizedNewDesc = description.trim().lowercase()

                                        val existingChore = chores.firstOrNull { chore ->
                                            val normalizedExistingName = chore.name.trim().lowercase()
                                            val normalizedExistingDesc = chore.description.trim().lowercase()

                                            normalizedExistingName == normalizedNewName &&
                                                    normalizedExistingDesc == normalizedNewDesc
                                        }

                                        if (existingChore != null) {
                                            toastMessage = "Chore '$formattedName' already exists!"
                                            return@launch
                                        }
                                        val request = AddChoreRequest(
                                            groupId = groupId,
                                            deviceId = deviceId,
                                            name = formattedName,
                                            description = description,
                                            points = points.toInt()
                                        )

                                        api.addChore(request)

                                        // Refresh data
                                        refreshChoresAndMembers()

                                        toastMessage = "Chore '$formattedName' created successfully"

                                        // Reset form
                                        choreName = ""
                                        points = ""
                                        description = ""
                                        showDialog = false

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        toastMessage = "Failed to create chore: ${e.message}"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = confirmTextColor
                        )
                    ) {
                        Text("Add", fontFamily = CooperBt)
                    }
                },
                dismissButton = {
                    val buttonColor = if (darkModeState) {
                        MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    }

                    TextButton(
                        onClick = {
                            choreName = ""
                            points = ""
                            description = ""
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
                        Text("Cancel", fontFamily = CooperBt)
                    }
                },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                titleContentColor = MaterialTheme.colorScheme.onSecondary,
                textContentColor = MaterialTheme.colorScheme.onSecondary
            )
        }

        // Info Box
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

                val bulletThree = "\n• Shake phone to randomly assign chores"
                addStyle(
                    style = ParagraphStyle(
                        textIndent = TextIndent(restLine = 10.sp)
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

            // Background overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .clickable { showInfo = false }
            )

            // Info card
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
                            fontFamily = CooperBt,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = infoText,
                            inlineContent = inlineContent,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontFamily = CooperBt,
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
    onSubmitChoreChange: (String, String) -> Unit = { _, _ -> },
    darkModeState: Boolean
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var currentAssignee by remember { mutableStateOf(username) }
    var currentStatus by remember { mutableStateOf(status) }
    val isCompleted = currentStatus == "Completed"

    val statusOptions = listOf("To do", "In progress", "Completed")
    val scope = rememberCoroutineScope()

    // Shaking Animation Variables
    val rotation = remember { Animatable(0f) }
    var shaking by remember { mutableStateOf(false) }

    // Swipe animation
    val swipeOffset = remember { Animatable(0f) }
    val cardAlpha = remember { Animatable(1f) }
    val maxDragX = 150.dp

    // Colors for interpolation
    val defaultCardColor = MaterialTheme.colorScheme.onPrimary
    val deleteColor = MaterialTheme.colorScheme.error
    val density = LocalDensity.current

    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    // Control shaking effect
    LaunchedEffect(deleteMode) {
        if (deleteMode && chore.isNotEmpty()) { // Only shake if there are chores
            shaking = true
            rotation.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
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
                    scope.launch {
                        swipeOffset.animateTo(-size.width.toFloat(), tween(300))
                        cardAlpha.animateTo(0f, tween(100))
                        onDelete()
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
                    val newOffset = (swipeOffset.value + dragAmount).coerceAtMost(0f)
                    swipeOffset.snapTo(newOffset)
                }
            }
        }
    }

    // Progress of the color change (0.0 to 1.0)
    val swipeProgress = (-swipeOffset.value / with(density) { maxDragX.toPx() }).coerceIn(0f, 1f)

    // Interpolated color based on swipe progress
    val interpolatedColor = lerp(defaultCardColor, deleteColor, swipeProgress)

    val cardGraphicsModifier = modifier
        .fillMaxWidth(0.9f)
        .then(if (deleteMode) swipeToDeleteModifier else Modifier)
        .graphicsLayer(
            rotationZ = if (shaking && deleteMode) (rotation.value * 2f) - 1f else 0f,
            translationX = swipeOffset.value,
            alpha = cardAlpha.value * (if (isCompleted) 0.6f else 1f)
        )
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

    val contentTint = lerp(
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.onError,
        swipeProgress
    )

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
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
                            fontFamily = CooperBt,
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
                            style = TextStyle(
                                textDecoration = if (isCompleted)
                                    TextDecoration.LineThrough
                                else
                                    TextDecoration.None
                            )
                        )
                        Text(
                            text = currentAssignee,
                            color = contentTint,
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
                            "To do" -> Color(0xFFF44336)
                            "In progress" -> Color(0xFFFF9800)
                            "Completed" -> Color(0xFF4CAF50)
                            else -> contentTint
                        },
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
//                        onAssignUser(currentAssignee)
//                        onChangeStatus(currentStatus)
                        onSubmitChoreChange(currentAssignee, currentStatus)
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