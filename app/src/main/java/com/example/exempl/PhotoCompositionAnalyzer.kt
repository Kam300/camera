package com.example.exempl

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PhotoCompositionAnalyzer(private val context: Context) {

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .build()
    )

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    fun analyzeImage(imageProxy: ImageProxy, onGuidanceGenerated: (String) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Детекция объектов
            objectDetector.process(image)
                .addOnSuccessListener { objects ->
                    // Детекция поз
                    poseDetector.process(image)
                        .addOnSuccessListener { pose ->
                            val guidance = generateCompositionGuidance(objects, pose, imageProxy)
                            if (guidance.isNotEmpty()) {
                                onGuidanceGenerated(guidance)
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
                .addOnFailureListener {
                    imageProxy.close()
                }
        }
    }

    private fun generateCompositionGuidance(
        objects: List<com.google.mlkit.vision.objects.DetectedObject>,
        pose: com.google.mlkit.vision.pose.Pose,
        imageProxy: ImageProxy
    ): String {
        val width = imageProxy.width
        val height = imageProxy.height

        // Находим главный объект (человека)
        val mainSubject = objects.firstOrNull { obj ->
            obj.labels.any { it.text == "Person" && it.confidence > 0.7f }
        }

        return when {
            mainSubject == null -> ""

            // Проверка правила третей
            !isInRuleOfThirds(mainSubject.boundingBox, width, height) -> {
                "Переместите человека ближе к линиям третей для лучшей композиции"
            }

            // Проверка центровки
            isObjectCentered(mainSubject.boundingBox, width, height) -> {
                "Попробуйте сместить объект из центра кадра"
            }

            // Проверка расстояния
            isObjectTooSmall(mainSubject.boundingBox, width, height) -> {
                "Подойдите ближе к объекту съемки"
            }

            isObjectTooLarge(mainSubject.boundingBox, width, height) -> {
                "Отойдите немного дальше от объекта"
            }

            // Проверка ориентации
            shouldSuggestPortrait(mainSubject.boundingBox) -> {
                "Поверните телефон вертикально для лучшего кадра"
            }

            shouldSuggestLandscape(mainSubject.boundingBox) -> {
                "Поверните телефон горизонтально для лучшего кадра"
            }

            else -> ""
        }
    }

    private fun isInRuleOfThirds(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val centerX = boundingBox.centerX().toFloat() / width
        val centerY = boundingBox.centerY().toFloat() / height

        // Линии третей: 1/3 и 2/3
        val thirdLines = listOf(0.33f, 0.67f)

        return thirdLines.any { line ->
            kotlin.math.abs(centerX - line) < 0.1f || kotlin.math.abs(centerY - line) < 0.1f
        }
    }

    private fun isObjectCentered(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val centerX = boundingBox.centerX().toFloat() / width
        val centerY = boundingBox.centerY().toFloat() / height

        return kotlin.math.abs(centerX - 0.5f) < 0.15f && kotlin.math.abs(centerY - 0.5f) < 0.15f
    }

    private fun isObjectTooSmall(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val objectArea = boundingBox.width() * boundingBox.height()
        val totalArea = width * height
        return (objectArea.toFloat() / totalArea) < 0.1f
    }

    private fun isObjectTooLarge(boundingBox: android.graphics.Rect, width: Int, height: Int): Boolean {
        val objectArea = boundingBox.width() * boundingBox.height()
        val totalArea = width * height
        return (objectArea.toFloat() / totalArea) > 0.7f
    }

    private fun shouldSuggestPortrait(boundingBox: android.graphics.Rect): Boolean {
        return boundingBox.height() > boundingBox.width() * 1.5f
    }

    private fun shouldSuggestLandscape(boundingBox: android.graphics.Rect): Boolean {
        return boundingBox.width() > boundingBox.height() * 1.5f
    }
}
