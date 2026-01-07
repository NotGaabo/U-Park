package com.kotlin.u_park.presentation.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.IncomeReport
import com.kotlin.u_park.domain.model.OccupancyReport
import com.kotlin.u_park.domain.model.SalidaResponse
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PdfGenerator {

    // --- Formateo de fecha ---
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(raw: String): String {
        return try {
            val instant = Instant.parse(raw)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (e: Exception) {
            raw.replace("T", " ")
        }
    }
    private fun generateSimpleReport(
        context: Context,
        title: String,
        garageName: String,
        period: String,
        rows: List<Pair<String, String>>
    ): File {

        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${title}_${System.currentTimeMillis()}.pdf"
        )

        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint()

        var y = 60

        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText(title, 40f, y.toFloat(), paint)

        y += 30
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Garage: $garageName", 40f, y.toFloat(), paint)

        y += 20
        canvas.drawText("Periodo: $period", 40f, y.toFloat(), paint)

        y += 40

        rows.forEach { (label, value) ->
            canvas.drawText("$label: $value", 40f, y.toFloat(), paint)
            y += 24
        }

        pdf.finishPage(page)
        pdf.writeTo(FileOutputStream(file))
        pdf.close()

        return file
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateOccupancyReport(
        context: Context,
        report: OccupancyReport
    ): File {

        return generateSimpleReport(
            context = context,
            title = "Reporte de Ocupación",
            garageName = report.garageName,
            period = "${report.startDate.toLocalDate()} → ${report.endDate.toLocalDate()}",
            rows = listOf(
                "Total de vehículos" to report.totalVehicles.toString(),
                "Tiempo promedio" to (report.averageStayMinutes?.toInt()?.toString() ?: "N/A") + " min"
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateIncomeReport(
        context: Context,
        report: IncomeReport
    ): File {

        return generateSimpleReport(
            context = context,
            title = "Reporte de Ingresos",
            garageName = report.garageName,
            period = "${report.startDate.toLocalDate()} → ${report.endDate.toLocalDate()}",
            rows = listOf(
                "Total de ingresos" to "$${String.format("%.2f", report.totalIncome)}",
                "Transacciones" to report.dailyIncome.sumOf { it.transactionCount }.toString()
            )
        )
    }

    // --- Formateo de duración ---
    private fun formatDuration(hours: Double): String {
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        return "%02d:%02d hrs".format(h, m)
    }

    private fun createPdfInDownloads(
        context: Context,
        fileName: String
    ): Pair<OutputStream, File> {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ✅ ANDROID 10+
            val resolver = context.contentResolver

            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/U-Park")
            }

            val uri = resolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw IllegalStateException("No se pudo crear el PDF")

            val outputStream = resolver.openOutputStream(uri)
                ?: throw IllegalStateException("No se pudo abrir OutputStream")

            // File "virtual" solo para compartir
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "U-Park/$fileName"
            )

            Pair(outputStream, file)

        } else {
            // ✅ ANDROID 7–9
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "U-Park"
            )

            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, fileName)
            Pair(FileOutputStream(file), file)
        }
    }


    // -------------------------------------------------------
    // ⚡ GENERAR PDF CON DISEÑO MODERNO
    // -------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateFacturaSalida(
        context: Context,
        ticket: SalidaResponse,
        vehiculoNombre: String,
        garageNombre: String,
        saveToDownloads: Boolean = false
    ): File {

        val fileName = "Ticket_${ticket.parking_id.take(8)}.pdf"

        val (outStream, file) = if (saveToDownloads) {
            createPdfInDownloads(context, fileName)
        } else {
            val dir = File(context.cacheDir, "facturas")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            Pair(FileOutputStream(file), file)
        }
        Pair(FileOutputStream(file), file.absolutePath)

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // 🎨 COLORES MODERNOS
        val primaryColor = Color.rgb(230, 0, 35) // Rojo U•Park
        val darkGray = Color.rgb(51, 51, 51)
        val lightGray = Color.rgb(153, 153, 153)
        val bgGray = Color.rgb(248, 248, 248)

        // ═══════════════════════════════════════════
        // 📌 HEADER CON FONDO
        // ═══════════════════════════════════════════
        paint.color = primaryColor
        canvas.drawRect(0f, 0f, 595f, 120f, paint)

        // Logo/Título en blanco
        paint.color = Color.WHITE
        paint.textSize = 32f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText("U•PARK", 40f, 55f, paint)

        paint.textSize = 16f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Sistema de Estacionamiento", 40f, 85f, paint)

        // Ticket ID en header
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 14f
        canvas.drawText("Ticket #${ticket.parking_id.take(8)}", 555f, 70f, paint)
        paint.textAlign = Paint.Align.LEFT

        // ═══════════════════════════════════════════
        // 📋 SECCIÓN: INFORMACIÓN DEL TICKET
        // ═══════════════════════════════════════════
        var y = 160f

        // Título de sección
        paint.color = darkGray
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText("TICKET DE SALIDA", 40f, y, paint)
        y += 10f

        // Línea separadora
        paint.color = lightGray
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 30f

        // ═══════════════════════════════════════════
        // 🚗 DATOS DEL VEHÍCULO Y GARAGE
        // ═══════════════════════════════════════════
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 13f

        fun drawRow(label: String, value: String) {
            paint.color = lightGray
            paint.textSize = 11f
            canvas.drawText(label, 40f, y, paint)

            paint.color = darkGray
            paint.textSize = 14f
            canvas.drawText(value, 40f, y + 20f, paint)
            y += 50f
        }

        drawRow("VEHÍCULO", vehiculoNombre)
        drawRow("GARAGE", garageNombre)

        // ═══════════════════════════════════════════
        // ⏰ TIEMPOS
        // ═══════════════════════════════════════════
        y += 10f

        // Fondo gris para sección de tiempos
        paint.color = bgGray
        canvas.drawRect(40f, y - 10f, 555f, y + 125f, paint)

        y += 15f

        fun drawTimeRow(label: String, value: String) {
            paint.color = lightGray
            paint.textSize = 11f
            canvas.drawText(label, 55f, y, paint)

            paint.color = darkGray
            paint.textSize = 13f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText(value, 55f, y + 20f, paint)
            paint.typeface = Typeface.DEFAULT
            y += 40f
        }

        drawTimeRow("ENTRADA", formatDate(ticket.hora_entrada))
        drawTimeRow("SALIDA", formatDate(ticket.hora_salida))
        drawTimeRow("DURACIÓN", formatDuration(ticket.duration_hours))

        y += 20f

        // ═══════════════════════════════════════════
        // 💰 TOTAL A PAGAR (DESTACADO)
        // ═══════════════════════════════════════════

        // Línea punteada separadora
        paint.color = lightGray
        paint.strokeWidth = 2f
        paint.pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        canvas.drawLine(40f, y, 555f, y, paint)
        paint.pathEffect = null
        y += 40f

        // Label "Total"
        paint.color = darkGray
        paint.textSize = 14f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("TOTAL A PAGAR", 40f, y, paint)

        // Monto en rojo y grande
        paint.color = primaryColor
        paint.textSize = 32f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("RD$ ${"%,.2f".format(ticket.total)}", 555f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        // ═══════════════════════════════════════════
        // 📝 PIE DE PÁGINA
        // ═══════════════════════════════════════════
        y = 780f

        paint.color = lightGray
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("Gracias por usar U•Park", 297.5f, y, paint)
        canvas.drawText("Documento generado automáticamente", 297.5f, y + 15f, paint)

        val fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())
        canvas.drawText("Fecha de emisión: $fecha", 297.5f, y + 30f, paint)

        paint.textAlign = Paint.Align.LEFT

        // ═══════════════════════════════════════════

        pdf.finishPage(page)

        pdf.writeTo(outStream)
        outStream.close()
        pdf.close()

        if (saveToDownloads) {
            notifyFileSaved(context, file)
        }

        return file
    }

    // -------------------------------------------------------
    // 🔔 NOTIFICAR QUE SE GUARDÓ EL ARCHIVO
    // -------------------------------------------------------
    private fun notifyFileSaved(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    file
                ),
                "application/pdf"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si no puede abrir el PDF, al menos se guardó
        }
    }

    // -------------------------------------------------------
    // 📤 COMPARTIR UNIVERSAL (WhatsApp, Gmail, Drive, etc.)
    // -------------------------------------------------------
    fun compartirFactura(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ticket de Salida - U•Park")
            putExtra(Intent.EXTRA_TEXT, "Adjunto el ticket de salida del estacionamiento.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Compartir ticket")
        context.startActivity(chooser)
    }

    // -------------------------------------------------------
    // 📱 COMPARTIR DIRECTO A WHATSAPP
    // -------------------------------------------------------
    fun compartirFacturaWhatsApp(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Ticket de salida - U•Park")
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado, usar selector universal
            compartirFactura(context, file)
        }
    }
}