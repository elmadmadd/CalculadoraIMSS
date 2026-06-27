package com.example.consucalcu.features

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.consucalcu.*
import java.io.File
import java.io.FileOutputStream

data class TicketData(
    val clientName: String,
    val date: String,
    val product: String,
    val amount: String,
    val payment: String,
    val term: Int,
    val previousPayment: String,
    val savings: String
)

@Composable
fun DigitalTicket(
    data: TicketData,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TicketShape(20.dp.value)) // Forma personalizada con zig-zag abajo (simulado visualmente o simple rounded)
                .background(Color.White)
                .border(1.dp, ConsuDividerLight, RoundedCornerShape(16.dp))
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(ConsuNavy)
            )
            
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo / Icono
                Surface(
                    shape = CircleShape,
                    color = JuicyBlue.copy(alpha=0.1f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("💸", fontSize = 32.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "OFERTA APROBADA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = JuicyBlue,
                    letterSpacing = 2.sp
                )
                
                Text(
                    data.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Línea punteada personalizada
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.Gray,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
                
                
                Spacer(Modifier.height(24.dp))
                
                // Detalles Principales
                TicketRow("Cliente", data.clientName)
                TicketRow("Producto", data.product)
                TicketRow("Plazo", "${data.term} Meses")
                
                Spacer(Modifier.height(16.dp))
                
                // Highlight Amount
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ConsuBgLight, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text("MONTO A RECIBIR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ConsuTextSecondary)
                    Text(
                        data.amount,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ConsuNavy
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("NUEVA RESEÑA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(data.payment, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("AHORRO MENSUAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JuicyGreen)
                        Text(data.savings, fontSize = 16.sp, fontWeight = FontWeight.Black, color = JuicyGreen)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Footer
                Text(
                    "Generado por ConsuCalcu App",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
            
            // Decorative ZigZag bottom
            Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                val width = size.width
                val height = size.height
                val path = Path()
                path.moveTo(0f, 0f)
                val step = 20f
                var x = 0f
                while (x < width) {
                    path.lineTo(x + step / 2, height)
                    path.lineTo(x + step, 0f)
                    x += step
                }
                path.close()
                drawPath(path, Color.White) // Simula recorte sobre el fondo
                // Stroke bottom
                drawPath(path, color = Color(0xFFE0E0E0), style = Stroke(width = 2f))
            }
        }
    }
}

@Composable
private fun TicketRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Text(value, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// Shape Helper (Placeholder for simple impl)
private fun TicketShape(cornerRadius: Float) = RoundedCornerShape(cornerRadius)

// Helper Function to Share Logic
fun shareTicket(context: Context, ticketBitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/ticket_offer.png")
        ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        
        val newFile = File(cachePath, "ticket_offer.png")
        val contentUri: Uri = FileProvider.getUriForFile(context, "com.example.consucalcu.fileprovider", newFile)
        
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            context.startActivity(Intent.createChooser(shareIntent, "Compartir Oferta"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
