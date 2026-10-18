package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.archivist.LocalArchivistService
import com.example.ui.theme.KnobstoneForest
import com.example.ui.theme.LanternAmber
import com.example.ui.theme.LanternAmberBright
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentPaper
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateNightElevated
import com.example.ui.theme.SlateNightSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArchivistAssistantDialog(
    initialQuery: String = "",
    siteContext: String? = null,
    archivistService: LocalArchivistService,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf(initialQuery) }
    var isLoading by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf<String?>(null) }

    val quickQuestions = listOf(
        "Underground Railroad river crossings in Floyd County?",
        "What is the true history behind William Culbertson's mansion?",
        "Why was Tunnelton's Big Tunnel bored in 1857?",
        "Suggest a driving lore tour from New Albany to Corydon"
    )

    fun sendQuery(q: String) {
        if (q.isBlank()) return
        isLoading = true
        scope.launch {
            val result = archivistService.askArchivist(q, siteContext)
            responseText = result.getOrNull()
            isLoading = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(24.dp))
                .testTag("archivist_assistant_dialog"),
            color = SlateNightElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LanternAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = LanternAmberBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "HISTORICAL ARCHIVIST",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = LanternAmberBright
                            )
                            Text(
                                text = "Offline notes on featured sites (not an AI assistant)",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ParchmentMuted
                        )
                    }
                }

                if (siteContext != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SlateNightSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = LanternAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Site Reference: $siteContext",
                                style = MaterialTheme.typography.labelSmall,
                                color = ParchmentPaper
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Box
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "Ask about local history, primary sources, forgotten tales...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("archivist_input_field"),
                    trailingIcon = {
                        IconButton(
                            onClick = { sendQuery(query) },
                            enabled = query.isNotBlank() && !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (query.isNotBlank() && !isLoading) LanternAmber else ParchmentMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ParchmentPaper,
                        unfocusedTextColor = ParchmentPaper,
                        focusedBorderColor = LanternAmber,
                        unfocusedBorderColor = SlateBorder
                    ),
                    maxLines = 3
                )

                // Quick Suggestions
                if (responseText == null && !isLoading) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "EXPLORE HISTORICAL INQUIRIES:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LanternAmber
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        quickQuestions.forEach { prompt ->
                            Surface(
                                color = SlateNightSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        query = prompt
                                        sendQuery(prompt)
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentPaper,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = LanternAmber,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Consulting Southern Indiana Archives...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ParchmentMuted
                        )
                    }
                }

                // Response Card
                if (responseText != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = SlateNightSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LanternAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ARCHIVAL RESPONSE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = LanternAmber
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(responseText ?: ""))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HistoryEdu,
                                            contentDescription = "Copy text",
                                            tint = ParchmentMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, responseText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, "Share Archival Findings")
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = LanternAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = responseText ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = ParchmentPaper
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ask New Question Button if response exists
                if (responseText != null) {
                    Button(
                        onClick = {
                            responseText = null
                            query = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KnobstoneForest,
                            contentColor = ParchmentPaper
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ask Another Inquiry")
                    }
                }
            }
        }
    }
}
