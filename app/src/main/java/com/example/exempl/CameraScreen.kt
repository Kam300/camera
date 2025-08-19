package com.example.exempl

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var isGuidanceEnabled by remember { mutableStateOf(true) }
    var currentGuidance by remember { mutableStateOf("Наведите камеру на объект") }
    var showRuleOfThirds by remember { mutableStateOf(true) }

    val aiAnalyzer = remember { PhotoCompositionAnalyzer(context) }
    val voiceGuidance = remember { VoiceGuidanceManager(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                aiAnalyzer.analyzeImage(imageProxy) { guidance ->
                                    if (isGuidanceEnabled && guidance.isNotEmpty()) {
                                        currentGuidance = guidance
                                        voiceGuidance.speakGuidance(guidance)
                                    }
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        // Используем context как LifecycleOwner (работает для ComponentActivity)
                        val lifecycleOwner = ctx as LifecycleOwner
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        // Handle camera binding errors
                        currentGuidance = "Ошибка камеры: ${exc.message}"
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Rule of Thirds Overlay
        if (showRuleOfThirds) {
            CompositionGuideOverlay(modifier = Modifier.fillMaxSize())
        }

        // Top UI Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🤖 ИИ Советы",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentGuidance,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        // Bottom Control Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Voice Guidance Toggle
                Button(
                    onClick = {
                        isGuidanceEnabled = !isGuidanceEnabled
                        voiceGuidance.toggleGuidance()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGuidanceEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray
                    )
                ) {
                    Text(if (isGuidanceEnabled) "🔊 ВКЛ" else "🔇 ВЫКЛ")
                }

                // Capture Photo Button
                Button(
                    onClick = {
                        currentGuidance = "📸 Фото сохранено!"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("📸 ФОТО")
                }

                // Rule of Thirds Toggle
                Button(
                    onClick = { showRuleOfThirds = !showRuleOfThirds },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showRuleOfThirds)
                            MaterialTheme.colorScheme.tertiary
                        else
                            Color.Gray
                    )
                ) {
                    Text("📐")
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            voiceGuidance.shutdown()
        }
    }
}

@Composable
fun CompositionGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRuleOfThirdsLines()
    }
}

private fun DrawScope.drawRuleOfThirdsLines() {
    val width = size.width
    val height = size.height

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)

    // Vertical lines
    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(width / 3, 0f),
        end = androidx.compose.ui.geometry.Offset(width / 3, height),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(width * 2 / 3, 0f),
        end = androidx.compose.ui.geometry.Offset(width * 2 / 3, height),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    // Horizontal lines
    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(0f, height / 3),
        end = androidx.compose.ui.geometry.Offset(width, height / 3),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = androidx.compose.ui.geometry.Offset(0f, height * 2 / 3),
        end = androidx.compose.ui.geometry.Offset(width, height * 2 / 3),
        strokeWidth = 2f,
        pathEffect = pathEffect
    )
}
