package com.example.consucalcu.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.text.htmlEncode
import com.example.consucalcu.model.LoanOffer

object PdfExporter {

    fun exportToPdf(
        context: Context,
        offer: LoanOffer,
        clientName: String,
        advisorName: String,
        table: List<AmortizationRow>,
        onCompleted: () -> Unit = {}
    ) {
        val normalizedProduct = offer.product.uppercase()
        val brandColor = when {
            normalizedProduct.contains("CONSUBANCO") || normalizedProduct.contains("CSB") -> "#1E3A8A" // Azul / Navy
            normalizedProduct.contains("CONSUPAGO") || normalizedProduct.contains("CSP") -> "#D97706" // Dorado / Ámbar
            normalizedProduct.contains("OPCIPRES") || normalizedProduct.contains("OPC") -> "#059669"  // Verde
            normalizedProduct.contains("MASNOMINA") || normalizedProduct.contains("MSN") -> "#DC2626"  // Rojo
            else -> "#E67E22" // Naranja por defecto
        }

        // Datos para el gráfico de refinanciamiento
        val totalNewCredit = offer.amount
        val netDeposit = offer.mainValue
        val liquidationAmount = totalNewCredit.subtract(netDeposit)

        val isRefinance = totalNewCredit > netDeposit && netDeposit > java.math.BigDecimal.ZERO
        val totalDouble = totalNewCredit.toDouble()
        val liqPercent = if (totalDouble > 0) (liquidationAmount.toDouble() / totalDouble * 100).toInt() else 0
        val netPercent = 100 - liqPercent
        val productName = offer.product.htmlEncode()
        val agreementSubtitle = offer.agreementLabel.ifBlank { "Propuesta de Crédito" }.htmlEncode()
        val safeClientName = clientName.htmlEncode()
        val safeAdvisorName = advisorName.htmlEncode()
        val periodUnit = if (offer.paymentFrequency == "QUINCENAL") "Quincena" else "Mes"

        val htmlContent = buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body {
                            font-family: sans-serif;
                            margin: 0.5in;
                            color: #1E293B;
                            background-color: #FFFFFF;
                        }
                        .header {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            border-bottom: 2px solid $brandColor;
                            padding-bottom: 15px;
                            margin-bottom: 25px;
                        }
                        .brand-container {
                            display: flex;
                            flex-direction: column;
                        }
                        .brand-title {
                            font-size: 26px;
                            font-weight: 800;
                            color: $brandColor;
                            margin: 0;
                            text-transform: uppercase;
                        }
                        .brand-subtitle {
                            margin: 4px 0 0 0;
                            font-size: 13px;
                            color: #64748B;
                            font-weight: 600;
                            letter-spacing: 1px;
                            text-transform: uppercase;
                        }
                        .meta-info {
                            text-align: right;
                            font-size: 11px;
                            color: #475569;
                            line-height: 1.5;
                        }
                        .meta-info b {
                            color: #0F172A;
                        }
                        .summary-card {
                            background: #F8FAFC;
                            border: 1.5px solid #E2E8F0;
                            border-radius: 12px;
                            padding: 16px;
                            display: flex;
                            justify-content: space-around;
                            margin-bottom: 30px;
                        }
                        .summary-item {
                            text-align: center;
                            flex: 1;
                        }
                        .summary-label {
                            font-size: 10px;
                            color: #64748B;
                            font-weight: 600;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                        }
                        .summary-value {
                            font-size: 20px;
                            color: $brandColor;
                            font-weight: 800;
                            margin-top: 6px;
                        }
                        .chart-container {
                            border: 1.5px solid #E2E8F0;
                            border-radius: 12px;
                            padding: 20px;
                            margin-bottom: 30px;
                            background-color: #FFFFFF;
                        }
                        .chart-title {
                            font-size: 11px;
                            font-weight: 800;
                            color: #0F172A;
                            margin: 0 0 12px 0;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                        }
                        .stacked-bar {
                            display: flex;
                            height: 24px;
                            width: 100%;
                            border-radius: 12px;
                            overflow: hidden;
                            background-color: #E2E8F0;
                        }
                        .bar-segment-liq {
                            background-color: #94A3B8;
                            height: 100%;
                        }
                        .bar-segment-net {
                            background-color: $brandColor;
                            height: 100%;
                        }
                        .chart-legend {
                            display: flex;
                            justify-content: space-around;
                            margin-top: 15px;
                            font-size: 11px;
                        }
                        .legend-item {
                            display: flex;
                            align-items: center;
                        }
                        .legend-dot {
                            width: 10px;
                            height: 10px;
                            border-radius: 50%;
                            margin-right: 8px;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 15px;
                            font-size: 11px;
                        }
                        th {
                            background-color: $brandColor;
                            color: #FFFFFF;
                            padding: 10px 8px;
                            text-align: right;
                            font-weight: 600;
                            text-transform: uppercase;
                            font-size: 10px;
                            letter-spacing: 0.5px;
                        }
                        th:first-child {
                            text-align: center;
                            border-top-left-radius: 6px;
                            border-bottom-left-radius: 6px;
                        }
                        th:last-child {
                            border-top-right-radius: 6px;
                            border-bottom-right-radius: 6px;
                        }
                        td {
                            padding: 10px 8px;
                            border-bottom: 1px solid #E2E8F0;
                            text-align: right;
                            color: #334155;
                        }
                        td:first-child {
                            text-align: center;
                            font-weight: 600;
                            color: #0F172A;
                        }
                        tr:nth-child(even) {
                            background-color: #F8FAFC;
                        }
                        .footer {
                            margin-top: 40px;
                            font-size: 9px;
                            color: #64748B;
                            text-align: center;
                            border-top: 1px solid #E2E8F0;
                            padding-top: 15px;
                            line-height: 1.4;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div class="brand-container">
                            <h1 class="brand-title">$productName</h1>
                            <p class="brand-subtitle">$agreementSubtitle</p>
                        </div>
                        <div class="meta-info">
                            <p style="margin: 0;"><b>Cliente:</b> $safeClientName</p>
                            <p style="margin: 4px 0 0 0;"><b>Asesor:</b> $safeAdvisorName</p>
                            <p style="margin: 4px 0 0 0;"><b>Fecha:</b> ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())}</p>
                        </div>
                    </div>

                    <div class="summary-card">
                        <div class="summary-item">
                            <div class="summary-label">Monto Solicitado</div>
                            <div class="summary-value">$${offer.amount.toCurrency()}</div>
                        </div>
                        <div class="summary-item" style="border-left: 1.5px solid #E2E8F0; border-right: 1.5px solid #E2E8F0;">
                            <div class="summary-label">Plazo</div>
                            <div class="summary-value">${offer.termDisplay}</div>
                        </div>
                        <div class="summary-item">
                            <div class="summary-label">${offer.paymentLabel}</div>
                            <div class="summary-value">$${offer.monthlyPayment.toCurrency()}</div>
                        </div>
                    </div>
            """)

            if (isRefinance) {
                append("""
                    <div class="chart-container">
                        <h3 class="chart-title">Distribución del Crédito (Refinanciamiento)</h3>
                        <div class="stacked-bar">
                            <div class="bar-segment-liq" style="width: $liqPercent%;"></div>
                            <div class="bar-segment-net" style="width: $netPercent%;"></div>
                        </div>
                        <div class="chart-legend">
                            <div class="legend-item">
                                <span class="legend-dot" style="background-color: #94A3B8;"></span>
                                <span><b>A liquidar:</b> $${liquidationAmount.toCurrency()} ($liqPercent%)</span>
                            </div>
                            <div class="legend-item">
                                <span class="legend-dot" style="background-color: $brandColor;"></span>
                                <span><b>Neto a recibir (Dispersión):</b> $${netDeposit.toCurrency()} ($netPercent%)</span>
                            </div>
                        </div>
                    </div>
                """)
            }

            append("""
                    <table>
                        <thead>
                            <tr>
                                <th style="text-align: center; width: 10%;">$periodUnit</th>
                                <th style="width: 18%;">Pago Fijo</th>
                                <th style="width: 18%;">Capital</th>
                                <th style="width: 18%;">Interés</th>
                                <th style="width: 14%;">IVA</th>
                                <th style="width: 22%;">Saldo Insoluto</th>
                            </tr>
                        </thead>
                        <tbody>
            """)

            table.forEach { row ->
                append("""
                    <tr>
                        <td style="text-align: center;">${row.month}</td>
                        <td>$${row.payment.toCurrency()}</td>
                        <td>$${row.principal.toCurrency()}</td>
                        <td>$${row.interest.toCurrency()}</td>
                        <td>$${row.iva.toCurrency()}</td>
                        <td>$${row.balance.toCurrency()}</td>
                    </tr>
                """)
            }

            append("""
                        </tbody>
                    </table>

                    <div class="footer">
                        Esta propuesta de amortización tiene carácter meramente informativo y de simulación.
                        No constituye una oferta vinculante y su validez está sujeta a la aprobación de las políticas vigentes de la financiera.<br>
                        <b>Vigencia:</b> 7 días naturales a partir de su generación.
                    </div>
                </body>
                </html>
            """)
        }

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val safeFileProduct = offer.product.replace("[^A-Za-z0-9_-]".toRegex(), "_")
                val safeFileClient = clientName.replace("[^A-Za-z0-9_-]".toRegex(), "_")
                val printAdapter = view.createPrintDocumentAdapter("Propuesta_${safeFileProduct}_$safeFileClient")
                val jobName = "Propuesta_${safeFileProduct}_$safeFileClient"

                val printJob = printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
                val handler = Handler(Looper.getMainLooper())
                val startedAt = SystemClock.elapsedRealtime()
                val monitor = object : Runnable {
                    override fun run() {
                        when {
                            printJob.isCompleted -> {
                                onCompleted()
                                view.stopLoading()
                                view.destroy()
                            }
                            printJob.isCancelled || printJob.isFailed ||
                                SystemClock.elapsedRealtime() - startedAt >= PRINT_MONITOR_TIMEOUT_MS -> {
                                view.stopLoading()
                                view.destroy()
                            }
                            else -> handler.postDelayed(this, PRINT_MONITOR_INTERVAL_MS)
                        }
                    }
                }
                handler.post(monitor)
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent.toString(), "text/html", "utf-8", null)
    }

    private const val PRINT_MONITOR_INTERVAL_MS = 500L
    private const val PRINT_MONITOR_TIMEOUT_MS = 10 * 60 * 1000L
}
