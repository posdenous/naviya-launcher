package com.naviya.launcher.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Minimal stubs for missing UI components to enable compilation
 * These will be replaced with proper implementations later
 */

@Composable
internal fun ElderlyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        content = content
    )
}

@Composable
internal fun InfoCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Minimal stub for Security class
object Security {
    fun encryptSensitiveData(data: String): String = data
    fun decryptSensitiveData(data: String): String = data
}

// Minimal stub for BasicPreferences
object BasicPreferences {
    fun getString(key: String, default: String): String = default
    fun putString(key: String, value: String) {}
}
