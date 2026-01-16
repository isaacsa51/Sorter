package com.serranoie.app.media.sorter.presentation.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serranoie.app.media.sorter.R
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import com.serranoie.app.media.sorter.presentation.sorter.SwipeableCard

@Composable
fun OnBoardScreen(
    onGetStarted: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var trashProgress = remember { mutableStateOf(0f) }
    val trashIconAlpha = animateFloatAsState(trashProgress.value, animationSpec = tween(240)).value
    val trashIconScale =
        animateFloatAsState(0.7f + 0.5f * trashProgress.value, animationSpec = tween(240)).value
    val trashIconOffset =
        animateFloatAsState((32f - 60f * trashProgress.value), animationSpec = tween(240)).value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorScheme.background, colorScheme.surfaceVariant),
                        startY = 0f,
                        endY = 2200f
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Clean Up Your Gallery",
                    color = colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                Text(
                    text = "Organize your memories in seconds.",
                    color = colorScheme.outlineVariant,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
                // Trash Swipe Up
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(12.dp, CircleShape, clip = false)
                            .background(colorScheme.surfaceDim, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Trash",
                            tint = colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SWIPE UP TO TRASH",
                        color = colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_upward),
                        contentDescription = "Up Arrow",
                        tint = colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Preview Media Card
	            SwipeableCard(
		            //fileName = "Nature_Trip_04.jpg",
		            //fileInfo = "12 MB • 2 days ago",
		            modifier = Modifier.size(320.dp, 340.dp),
		            onKeep = {
			            scope.launch {
				            snackbarHostState.showSnackbar("Kept file")
			            }
		            },
		            onTrash = {
			            scope.launch {
				            snackbarHostState.showSnackbar("Trashed file")
			            }
		            },
		            onKeepGestureProgress = {},
		            onTrashGestureProgress = { trashProgress.value = it },
		            cardContent = {

		            }
	            )
                // Keep Swipe Down
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_downward),
                        contentDescription = "Down Arrow",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SWIPE DOWN TO KEEP",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(12.dp, CircleShape, clip = false)
                            .background(colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                // Get Started Button
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    onClick = onGetStarted,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Started",
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
