package com.serranoie.app.media.sorter.presentation.tutorial

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.presentation.sorter.SwipeableCard
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnBoardScreen(
    onGetStarted: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var keepProgress by remember { mutableFloatStateOf(0f) }
    var trashProgress by remember { mutableFloatStateOf(0f) }
    
    // Keep icon animations
    val keepIconAlpha = animateFloatAsState(keepProgress, animationSpec = tween(240)).value
    val keepIconScale = animateFloatAsState(0.7f + 0.5f * keepProgress, animationSpec = tween(240)).value
    val keepIconOffset = animateFloatAsState((-32f + 60f * keepProgress), animationSpec = tween(240)).value
    
    // Trash icon animations
    val trashIconAlpha = animateFloatAsState(trashProgress, animationSpec = tween(240)).value
    val trashIconScale = animateFloatAsState(0.7f + 0.5f * trashProgress, animationSpec = tween(240)).value
    val trashIconOffset = animateFloatAsState((32f - 60f * trashProgress), animationSpec = tween(240)).value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Clean Up Your Gallery",
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                Text(
                    text = "Organize your memories in seconds.",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, CircleShape, clip = false)
                            .background(colorScheme.errorContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Trash",
                            tint = colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SWIPE UP TO DELETE",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = colorScheme.error
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_upward),
                        contentDescription = "Up Arrow",
                        tint = colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
	            SwipeableCard(
		            modifier = Modifier
		                .fillMaxWidth(0.9f)
		                .fillMaxHeight(0.45f),
		            onKeep = {
			            scope.launch {
				            snackbarHostState.showSnackbar("Media kept!")
			            }
		            },
		            onTrash = {
			            scope.launch {
				            snackbarHostState.showSnackbar("Media deleted!")
			            }
		            },
		            onKeepGestureProgress = { keepProgress = it },
		            onTrashGestureProgress = { trashProgress = it },
		            cardContent = {
			            // Simple placeholder content
			            Box(
				            modifier = Modifier
					            .fillMaxSize()
					            .background(colorScheme.surfaceContainerHighest),
				            contentAlignment = Alignment.Center
			            ) {
				            Image(
					            painter = painterResource(id = R.drawable.demo_image),
					            contentDescription = "Demo media",
					            modifier = Modifier.fillMaxSize(),
					            contentScale = ContentScale.Crop
				            )
			            }
		            }
	            )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_downward),
                        contentDescription = "Down Arrow",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SWIPE DOWN TO KEEP",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(12.dp, CircleShape, clip = false)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@DevicePreview
@Composable
fun OnBoardScreenPreview() {
	PreviewWrapper {
		OnBoardScreen(
			onGetStarted = {}
		)
	}
}
