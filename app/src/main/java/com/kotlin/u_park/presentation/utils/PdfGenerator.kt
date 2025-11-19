package com.kotlin.u_park.presentation.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.ParkingTicket
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateFactura(context: Context, ticket: ParkingTicket): File {

        val dir = File(context.cacheDir, "facturas")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "factura_${ticket.parkingId}.pdf")
        if (file.exists()) file.delete()

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Header
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        canvas.drawText("U-Park - Ticket Entrada", 40f, 50f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Parking ID: ${ticket.parkingId}", 40f, 90f, paint)
        canvas.drawText("Vehículo: ${ticket.plate}", 40f, 110f, paint)
        canvas.drawText("Hora Entrada: ${ticket.horaEntrada}", 40f, 130f, paint)
        canvas.drawText("Garage: ${ticket.garage}", 40f, 150f, paint)
        // Fotos URLs
        var y = 200f
        paint.textSize = 10f
        ticket.fotos.forEachIndexed { index, url ->
            canvas.drawText("${index + 1}. $url", 40f, y, paint)
            y += 15f
        }

        paint.textSize = 12f
        canvas.drawText("Generado: ${java.time.OffsetDateTime.now()}", 40f, 800f, paint)

        pdf.finishPage(page)

        val out = FileOutputStream(file)
        pdf.writeTo(out)
        out.close()
        pdf.close()

        return file
    }
}
