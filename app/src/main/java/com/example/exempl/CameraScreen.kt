package com.example.exempl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraScreen() {
    // Временная версия без использования LocalLifecycleOwner
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎥 Камера готова!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ИИ анализ композиции активирован",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // TODO: Реализация включения/выключения голосовых подсказок
                    }
                ) {
                    Text("🔊 Подсказки")
                }

                Button(
                    onClick = {
                        // TODO: Реализация захвата фото
                    }
                ) {
                    Text("📸 Фото")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Базовый интерфейс камеры создан!\nТеперь можно добавлять CameraX функциональность.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}
