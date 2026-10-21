package com.example.consucalcu.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.consucalcu.ConsuOrange
import com.example.consucalcu.LocalSoundManager
import com.example.consucalcu.ui.theme.ConsuRadius
import com.example.consucalcu.ui.theme.ConsuSpacing

private const val SupportClabe = "012180015733730647"

@Composable
fun VoluntarySupportModal(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(ConsuRadius.xxl),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Surface(shape = CircleShape, color = ConsuOrange.copy(alpha = 0.14f)) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = ConsuOrange,
                    modifier = Modifier.padding(ConsuSpacing.md)
                )
            }
        },
        title = {
            Text(
                text = "Ayúdanos a seguir mejorando",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ConsuSpacing.md)
            ) {
                Text(
                    text = "ConsuCalcu se desarrolla de forma independiente. Si te resulta útil, puedes apoyar voluntariamente desde $30 MXN o con el monto que tú decidas.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ConsuRadius.md),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(Modifier.padding(ConsuSpacing.lg)) {
                        Text("Transferencia bancaria", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("BBVA Bancomer", fontWeight = FontWeight.SemiBold)
                        Text("Jesús Maddián Morales Castillo", style = MaterialTheme.typography.bodySmall)
                        Text("•••• •••• •••• 730647", fontWeight = FontWeight.Bold)
                        Text("Concepto: APOYO CONSUCALCU", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    text = "Tu apoyo no desbloquea funciones ni cambia la experiencia de la aplicación.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("CLABE ConsuCalcu", SupportClabe))
                    soundManager?.play("copy_confirm")
                    Toast.makeText(context, "CLABE copiada", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ConsuOrange)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                    Text("  Copiar CLABE", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Quizá después")
            }
        }
    )
}
