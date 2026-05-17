package com.brzhang.gemma_ai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.brzhang.gemma_ai.ui.theme.*

/**
 * Card for displaying AI analysis results.
 */
@Composable
fun ResultCard(
    title: String,
    content: String,
    dangerLevel: Int = 1,
    modifier: Modifier = Modifier,
    footer: @Composable (() -> Unit)? = null
) {
    val backgroundColor = when {
        dangerLevel >= 4 -> DangerBackground
        dangerLevel >= 3 -> WarningBackground
        else -> SafeBackground
    }
    val borderColor = when {
        dangerLevel >= 4 -> DangerRed
        dangerLevel >= 3 -> WarningYellow
        else -> SafeGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = borderColor
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            footer?.let {
                Spacer(Modifier.height(16.dp))
                it()
            }
        }
    }
}

/**
 * Red danger banner for critical warnings.
 */
@Composable
fun DangerBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DangerRed)
            .padding(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextOnDanger
        )
    }
}

/**
 * Disclaimer bar shown below every AI result.
 */
@Composable
fun DisclaimerBar(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = WarningBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = "注意：以上结果由村医 AI 自动生成，可能出现偏差，仅供日常参考！用药及生大病请务必当面咨询专职村医！",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = WarningYellow
            )
        }
    }
}
