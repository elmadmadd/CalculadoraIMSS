package com.example.consucalcu.features

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.consucalcu.model.ClientData
import com.example.consucalcu.model.LoanOffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigDecimal
import java.text.DecimalFormat

object TicketGenerator {
    private fun BigDecimal.toCurrency(): String = DecimalFormat("#,###.00").format(this)

    suspend fun generateAndShare(
        context: Context,
        offer: LoanOffer,
        client: ClientData,
        advisor: String
    ) = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // --- PALETA DE COLORES PREMIUM ---
        val colorDarkBg = android.graphics.Color.parseColor("#0F172A") // Slate 900
        val colorCard = android.graphics.Color.WHITE
        val colorPrimary = android.graphics.Color.parseColor("#3B82F6") // Blue 500
        val colorSuccess = android.graphics.Color.parseColor("#22C55E") // Green 500
        val colorAccent = android.graphics.Color.parseColor("#F59E0B") // Amber 500
        val colorTextMain = android.graphics.Color.parseColor("#1E293B") // Slate 800
        val colorTextSec = android.graphics.Color.parseColor("#64748B") // Slate 500
        val colorDivider = android.graphics.Color.parseColor("#E2E8F0") // Slate 200

        // --- PINCELES ---
        val bgPaint = Paint().apply { color = colorDarkBg; style = Paint.Style.FILL }
        val cardPaint = Paint().apply {
            color = colorCard
            style = Paint.Style.FILL
            setShadowLayer(40f, 0f, 20f, android.graphics.Color.parseColor("#88000000"))
        }

        val textTitle = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val textSubtitle = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8") // Slate 400
            textSize = 36f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textLabel = Paint().apply {
            color = colorTextSec
            textSize = 34f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val textValue = Paint().apply {
            color = colorTextMain
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        val textGold = Paint().apply {
            color = colorAccent
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val dividerPaint = Paint().apply {
            color = colorDivider
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        // 1. FONDO
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. HEADER
        canvas.drawText("CONSUCALCU PRO", width / 2f, 150f, textTitle)
        canvas.drawText("Simulación Oficial de Crédito", width / 2f, 210f, textSubtitle)

        // 3. TARJETA FLOTANTE
        val cardMargin = 60f
        val cardTop = 300f
        val cardBottom = height - 350f
        val cardRect = android.graphics.RectF(cardMargin, cardTop, width - cardMargin, cardBottom)
        canvas.drawRoundRect(cardRect, 40f, 40f, cardPaint)

        // 4. CONTENIDO TARJETA
        var y = cardTop + 100f
        val xLeft = cardMargin + 60f
        val xRight = width - cardMargin - 60f

        // Badge de Aprobado
        val badgeWidth = 400f
        val badgeHeight = 80f
        val badgeRect = android.graphics.RectF(
            (width - badgeWidth) / 2f,
            y,
            (width + badgeWidth) / 2f,
            y + badgeHeight
        )
        val badgePaint = Paint().apply { color = android.graphics.Color.parseColor("#DCFCE7"); style = Paint.Style.FILL } // Green 100
        canvas.drawRoundRect(badgeRect, 40f, 40f, badgePaint)
        
        val textBadge = Paint().apply {
            color = colorSuccess // Green 600
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val fontMetrics = textBadge.fontMetrics
        val badgeTextY = badgeRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText("¡PROPUESTA APROBADA!", width / 2f, badgeTextY, textBadge)

        y += 180f

        // Monto Gigante (Hero)
        val textCurrency = Paint().apply {
            color = colorPrimary
            textSize = 140f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(10f, 0f, 5f, android.graphics.Color.parseColor("#443B82F6"))
        }
        val textCurrencyLabel = Paint().apply {
            color = colorTextSec
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        canvas.drawText("MONTO AUTORIZADO", width / 2f, y, textCurrencyLabel)
        y += 140f
        canvas.drawText("$${offer.amount.toCurrency()}", width / 2f, y, textCurrency)
        
        y += 100f
        canvas.drawLine(xLeft, y, xRight, y, dividerPaint)
        y += 80f

        // Función Helper para filas
        fun drawDetailRow(label: String, value: String, highlight: Boolean = false) {
            canvas.drawText(label, xLeft, y, textLabel)
            
            // Safe Paint Clone
            val valPaint = Paint(textValue).apply {
                 if(highlight) {
                     color = colorPrimary
                     textSize = 50f
                 }
            }
            canvas.drawText(value, xRight, y, valPaint)
            y += 90f
        }

        drawDetailRow("Cliente", client.name.take(20) + if(client.name.length>20)"..." else "")
        drawDetailRow("Producto", offer.product)
        drawDetailRow("Plazo", "${offer.term} Meses")
        
        y += 20f
        // Caja secundaria para pago
        val paymentBg = Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9"); style = Paint.Style.FILL } // Slate 100
        val paymentRect = android.graphics.RectF(cardMargin + 20f, y, width - cardMargin - 20f, y + 140f)
        canvas.drawRoundRect(paymentRect, 20f, 20f, paymentBg)
        
        val oldY = y
        y += 90f // Centrar verticalmente en la caja
        val paymentLabelX = xLeft + 40f
        val paymentValueX = xRight - 40f
        
        // Safe Paint Clones
        val paymentLabelPaint = Paint(textLabel).apply { typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("MENSUALIDAD", paymentLabelX, y, paymentLabelPaint)
        
        val paymentValuePaint = Paint(textValue).apply { textSize = 60f; color = colorTextMain }
        canvas.drawText("$${offer.monthlyPayment.toCurrency()}", paymentValueX, y, paymentValuePaint)
        
        y = oldY + 140f + 60f // Salir de la caja + margen

        drawDetailRow("CAT Promedio", offer.cat)
        drawDetailRow("Tasa de Interés", offer.tipoTasa)
        
        y += 40f
        canvas.drawLine(xLeft, y, xRight, y, dividerPaint)
        y += 80f
        
        // Footer de la tarjeta
        val textFooter = Paint().apply {
             color = colorTextSec
             textSize = 30f
             textAlign = Paint.Align.CENTER
             isAntiAlias = true
        }
        canvas.drawText("Oferta válida por 7 días. Sujeto a aprobación final.", width / 2f, cardBottom - 60f, textFooter)

        // 5. FOOTER GLOBAL
        val footerY = height - 150f
        canvas.drawText("ASESOR CERTIFICADO", width / 2f, footerY, textGold)
        
        val footerNamePaint = Paint(textTitle).apply { textSize = 44f }
        canvas.drawText(advisor.uppercase(), width / 2f, footerY + 60f, footerNamePaint)

        try {
            val filename = "Ticket_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ConsuCalcu")
                } else {
                    // Legacy Support (API < 29)
                    @Suppress("DEPRECATION")
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val imageDir = File(imagesDir, "ConsuCalcu")
                    if (!imageDir.exists()) {
                        imageDir.mkdirs()
                    }
                    val imageFile = File(imageDir, filename)
                    put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
                }
            }
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            imageUri?.let { uri ->
                context.contentResolver.openOutputStream(uri)
                    ?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                withContext(Dispatchers.Main) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_STREAM, uri); type =
                        "image/jpeg"; addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Enviar Ticket Dorado"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error generando ticket",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
