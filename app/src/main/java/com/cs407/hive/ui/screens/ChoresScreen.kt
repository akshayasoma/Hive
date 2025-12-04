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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ChoresScreen(deviceId: String, groupId: String,onNavigateToHome: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
//    var chores by remember { mutableStateOf(listOf<Triple<String, String, String>>()) } // OLD SYSTEM
    var chores by remember { mutableStateOf(listOf<UiChore>()) }
    var showInfo by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(false) }
    val CooperBt = FontFamily(
        Font(R.font.cooper_bt_bold)
    )

    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val api = remember { ApiClient.instance }
    val scope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
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
                                        toastMessage = "Chore '${chore.name}' deleted successfully!"
                                    }
                                    catch(e: Exception){
                                        Log.e("ChoresScreen", "Error deleting chore: $e")
                                        toastMessage = "Failed to delete chore: ${e.message}"
                                    }

                                }
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No chores yet! Add chores using the button below!",
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
                        fontFamily = CooperBt, //font added
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },

                text = {
                    Column {
                        OutlinedTextField(
                            value = choreName,
                            onValueChange = { choreName = it },
                            label = {
                                Text(
                                    text = "Chore Name",
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            }, //font added
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = points,
                            onValueChange = { points = it },
                            label = {
                                Text(
                                    text = "Points",
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            }, //font added
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = {
                                Text(
                                    text = "Description",
                                    //fontFamily = CooperBt,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = textColor
                                )
                            }, //font added
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
                                    toastMessage = "Chore '$formattedName' created successfully!"
                                    chores = updatedChores.map { UiChore(
                                        name = it.name,
                                        description = it.description,
                                        points = it.points,
                                        status = it.status,
                                        assignee = it.assignee
                                    ) }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    toastMessage = "Failed to create chore: ${e.message}"
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
                        Text("Add", fontFamily = CooperBt,) //font added
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
    deleteMode: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    val cardGraphicsModifier = modifier
        .fillMaxWidth(0.9f)
        .then(if (deleteMode) swipeToDeleteModifier else Modifier)
        .graphicsLayer(
            //rotation for shaking
            rotationZ = if (shaking && deleteMode) (rotation.value * 2f) - 1f else 0f,
            //swipe offset when dragging
            translationX = swipeOffset.value,
            //fade-out effect deletion
            alpha = cardAlpha.value
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
                            fontFamily = CooperBt, //font added
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,

                            )
                        Text(
                            text = username,
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
                        fontFamily = CooperBt, //font added
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        text = status,
                        color = contentTint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
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
                        fontFamily = CooperBt, //font added
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
                        //fontFamily = CooperBt, //font added
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
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