package com.cs407.hive.ui.screens

import android.R.attr.checked
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.hive.R
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.model.AddGroceryRequest
import com.cs407.hive.data.model.DeleteChoreRequest
import com.cs407.hive.data.model.DeleteGroceryRequest
import com.cs407.hive.data.model.UiChore
import com.cs407.hive.data.model.UiGrocery
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.ui.theme.HiveTheme
import com.cs407.hive.workers.WorkerTestUtils
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.math.roundToInt

@Composable
fun GroceryScreen(deviceId: String, groupId: String, onNavigateToHome: () -> Unit, darkModeState : Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
//    var groceries by remember { mutableStateOf(listOf<Triple<String, Boolean, String>>()) }
    var groceries by remember { mutableStateOf(listOf<UiGrocery>()) }
    var showInfo by remember { mutableStateOf(false) }

    val (completedGroceries, notCompletedGroceries) = remember(groceries) {
        groceries.partition { it.completed }
    }
    var deleteMode by remember { mutableStateOf(false) }
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    var itemNameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val api = remember { ApiClient.instance }
    val scope = rememberCoroutineScope()

    fun validateItemName(): Boolean {
        return if (itemName.isBlank()) {
            itemNameError = "Item name cannot be empty!"
            false
        } else if (itemName.length > 30) {
            itemNameError = "Item name too long!"
            false
        } else {
            itemNameError = null
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

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = api.getGroup(mapOf("groupId" to groupId))
                val serverGroceries = response.group.groceries ?: emptyList()

                groceries = serverGroceries.map { grocery ->
                    UiGrocery(
                        name = grocery.name,
                        description = grocery.description,
                        completed = grocery.completed
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(groceries) {
        if (groceries.isEmpty()) {
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
                text = "GROCERIES",
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
                if (notCompletedGroceries.isNotEmpty() || completedGroceries.isNotEmpty()) {
                    items(
                        items=notCompletedGroceries.reversed(),
                        key={grocery -> "${grocery.name}/${grocery.description}/${grocery.completed}"}
                        ) { grocery ->
                        GroceryCard(
                            item = grocery.name,
                            status = grocery.completed,
                            description = grocery.description,
                            deleteMode = deleteMode,
                            onDelete = {
                                //DONE: Delete the Grocery in db
                                scope.launch {
                                    try{
                                        val deleteGrocery = DeleteGroceryRequest(
                                            groupId = groupId,
                                            deviceId = deviceId,
                                            name = grocery.name,
                                            description = grocery.description,
                                            completed = grocery.completed
                                        )
                                        api.deleteGrocery(deleteGrocery)

                                        val updatedResponse = api.getGroup(mapOf("groupId" to groupId))
                                        val updatedGroceries = updatedResponse.group.groceries ?: emptyList()

                                        groceries = updatedGroceries.map {
                                            UiGrocery(
                                                name = it.name,
                                                description = it.description,
                                                completed = it.completed
                                            )
                                        }

                                        Log.d("GroceryScreen", "Grocery successfully deleted: ${grocery.name}")
                                        toastMessage = "Grocery '${grocery.name}' deleted successfully!"
                                    }
                                    catch(e: Exception){
                                        Log.e("GroceryScreen", "Error deleting grocery: $e")
                                        toastMessage = "Failed to delete chore: ${e.message}"
                                    }

                                }
                            },
                            darkModeState = darkModeState

                        )
                    }
                    items(
                        items = completedGroceries.reversed(),
                        key={grocery -> "${grocery.name}/${grocery.description}/${grocery.completed}"}
                    ) { grocery ->
                        GroceryCard(
                            item = grocery.name,
                            status = grocery.completed,
                            description = grocery.description,
                            deleteMode = deleteMode,
                            onDelete = {
                                //DONE: Delete the Grocery in db
                                scope.launch {
                                    try{
                                        val deleteGrocery = DeleteGroceryRequest(
                                            groupId = groupId,
                                            deviceId = deviceId,
                                            name = grocery.name,
                                            description = grocery.description,
                                            completed = grocery.completed
                                        )
                                        api.deleteGrocery(deleteGrocery)

                                        val updatedResponse = api.getGroup(mapOf("groupId" to groupId))
                                        val updatedGroceries = updatedResponse.group.groceries ?: emptyList()

                                        groceries = updatedGroceries.map {
                                            UiGrocery(
                                                name = it.name,
                                                description = it.description,
                                                completed = it.completed
                                            )
                                        }

                                        Log.d("GroceryScreen", "Grocery successfully deleted: ${grocery.name}")
                                    }
                                    catch(e: Exception){
                                        Log.e("GroceryScreen", "Error deleting grocery: $e")
                                    }

                                }
                            },
                            darkModeState = darkModeState
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No groceries yet! Add groceries using the button below!",
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            //fontFamily = CooperBt, //font added
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
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
                        itemNameError = null
                        descriptionError = null
                        showDialog = true},
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

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        "Add a New Grocery Item",
                        fontFamily = CooperBt, //font added
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                text = {
                    Column {
                        if (itemNameError != null) {
                            Text(
                                text = itemNameError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { newValue ->
                                itemName = newValue.take(31)
                                validateItemName() },
                            label = {
                                Text(
                                    text = "Item Name",
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            },//font added
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = itemNameError != null,
                            trailingIcon = {
                                if (itemNameError != null) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error
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

                        if (descriptionError != null) {
                            Text(
                                text = descriptionError!!,
                                color = MaterialTheme.colorScheme.error,
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
                                validateDescription() },
                            label = {
                                Text(
                                    text = "Description",
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            },//font added
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
                                        tint = MaterialTheme.colorScheme.error
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

                    TextButton(
                        onClick = {
                            itemNameError = null // Clear previous errors
                            descriptionError = null

                            val nameValid = validateItemName() // Run validation
                            val descValid = validateDescription()
                            val allValid = nameValid  && descValid
                            if (allValid) {
                                if (itemName.isNotBlank()) {
                                    val formattedName = itemName.split(" ")
                                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

                                    val request = AddGroceryRequest(
                                        groupId = groupId,
                                        deviceId = deviceId,
                                        name = formattedName,
                                        description = description,
                                        completed = false

                                    )

                                    scope.launch {
                                        try {
                                            api.addGrocery(request)
                                            Log.d(
                                                "GroceryScreen",
                                                "Grocery added: $formattedName, $description"
                                            )

//                                    chores = chores + Triple(formattedName, points, description)
                                            val updatedResponse =
                                                api.getGroup(mapOf("groupId" to groupId))
                                            val updatedGroceries =
                                                updatedResponse.group.groceries ?: emptyList()
                                            groceries = updatedGroceries.map {
                                                UiGrocery(
                                                    name = it.name,
                                                    description = it.description,
                                                    completed = it.completed
                                                )
                                            }
                                            toastMessage =
                                                "Grocery '$formattedName' added successfully!"
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            toastMessage = "Failed to add grocery: ${e.message}"
                                        } finally {
                                            itemName = ""
                                            description = ""
                                            itemNameError = null
                                            descriptionError = null
                                        }
                                    }

//                                groceries = groceries + UiGrocery(name = formattedName, description = description, completed = false)//Triple(formattedName, false, description)
                                }
                                showDialog = false
                            }

                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Add", fontFamily = CooperBt) //font added
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
                            itemName = ""
                            description = ""
                            itemNameError = null
                            showDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text("Cancel", fontFamily = CooperBt) //font added
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
                append(" to add a grocery item\n")

                append("• Tap ")
                appendInlineContent(deleteIconKey, "[delete]")
                append(" to enter delete mode\n")

                pushStyle(SpanStyle(fontSize = 14.sp))
                append("      ◦ Swipe LEFT on an item to delete\n\n")
                pop()
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

@Composable
fun GroceryCard(
    item: String,
    status: Boolean,
    description: String,
    deleteMode: Boolean,
    onDelete: () -> Unit,
    darkModeState : Boolean
) {
    var isChecked by remember { mutableStateOf(status) }
    var isExpanded by remember { mutableStateOf(false) }

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

    val baseAlpha = if (isChecked) 0.6f else 1f
    val combinedAlpha = baseAlpha * cardAlpha.value

    val cardGraphicsModifier = Modifier
        .fillMaxWidth(0.9f)
        .then(if (deleteMode) swipeToDeleteModifier else Modifier)
        .graphicsLayer(
            rotationZ = if (shaking && deleteMode) (rotation.value * 2f) - 1f else 0f, //shaking rotation
            translationX = swipeOffset.value, //swipe
            //combined alpha for both checked and delete animation
            alpha = combinedAlpha
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
//        modifier = Modifier
//            .fillMaxWidth(0.9f)
//            .clickable { isExpanded = !isExpanded }
//            .graphicsLayer {
//                alpha = if (isChecked) 0.6f else 1f
//            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            //containerColor = MaterialTheme.colorScheme.onPrimary
            containerColor = interpolatedColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Always visible section
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = contentTint,
                        uncheckedColor = contentTint,
                        checkmarkColor = defaultCardColor
                    ),
                    modifier = Modifier.size(40.dp)
                )

                Text(
                    text = item.uppercase(),
                    color = contentTint,
                    fontFamily = CooperBt, //font added
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
                )
            }

            AnimatedVisibility(
                visible = isExpanded && description.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 52.dp)
                ) {
                    Text(
                        text = "Description:",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontFamily = CooperBt, //font added
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                        //fontFamily = CooperBt, //font added
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_NO, name = "Light Mode")
//@Composable
//fun GroceryPreviewLight() {
//    HiveTheme(dynamicColor = false) {
//        GroceryScreen( deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
//    }
//}

//@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES, name = "Dark Mode")
//@Composable
//fun GroceryPreviewDark() {
//    HiveTheme(dynamicColor = false) {
//        GroceryScreen (deviceId="preview-device", groupId="preview-group", onNavigateToHome = {})
//    }
//}
