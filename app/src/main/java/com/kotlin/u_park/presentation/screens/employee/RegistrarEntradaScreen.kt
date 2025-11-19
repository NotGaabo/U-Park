package com.kotlin.u_park.presentation.screens.employee

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrarEntradaScreen(
    navController: NavController,
    garageId: String
) {
    val ctx = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var placa by remember { mutableStateOf("") }
    var savedPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Launcher para tomar foto como Bitmap
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { result ->
            bitmap = result
        }
    )

    // Si necesitas pedir permiso camera (en la mayoría de casos TakePicturePreview usa la cámara integrada,
    // pero pedimos permiso por seguridad)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) cameraLauncher.launch(null)
            else Toast.makeText(ctx, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrar Entrada", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("Placa del vehículo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                val permission = Manifest.permission.CAMERA
                val isGranted = ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
                if (!isGranted) {
                    permissionLauncher.launch(permission)
                } else {
                    cameraLauncher.launch(null)
                }
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Foto")
            }

            Button(
                onClick = {
                    // Confirmar: generar pdf y (aquí podrías llamar al ViewModel para subir foto y crear registro)
                    if (placa.isBlank()) {
                        Toast.makeText(ctx, "Ingresa la placa", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (bitmap == null) {
                        Toast.makeText(ctx, "Toma al menos 1 foto", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    try {
                        val file = generatePdfWithPhoto(ctx, placa.trim(), bitmap!!)
                        savedPath = file.absolutePath
                        Toast.makeText(ctx, "PDF guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()

                        // Aquí podrías: subir foto, insertar registro en parking, navegar a pantalla de ticket, etc.
                        // navController.navigate("ticket/${...}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(ctx, "Error generando PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar Entrada")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(280.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        savedPath?.let { path ->
            Text("Último PDF: $path", modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Genera un PDF simple (A4-like) con los datos básicos y la foto incrustada.
 * Guarda el PDF en: context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
 * Devuelve el File generado.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun generatePdfWithPhoto(context: android.content.Context, placa: String, foto: Bitmap): File {
    // Carpeta privada/external de la app para documentos (no necesita permiso WRITE_EXTERNAL_STORAGE)
    val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        ?: context.filesDir

    if (!docsDir.exists()) docsDir.mkdirs()

    val outFile = File(docsDir, "factura_${placa}_${System.currentTimeMillis()}.pdf")

    val pdf = PdfDocument()

    // A4-like in points (595 x 842)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Dibujar header
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("U-Park - Ticket de Entrada", 40f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Placa: $placa", 40f, 90f, paint)
    canvas.drawText("Fecha: ${java.time.LocalDateTime.now()}", 40f, 110f, paint)

    // Reservar área para la imagen: desde x=40, y=140 hasta width = pageWidth - 80
    val imageLeft = 40f
    val imageTop = 140f
    val imageMaxWidth = (pageInfo.pageWidth - 80).toFloat()
    val imageMaxHeight = 400f // límite para no pasarse

    // Escalar bitmap para que encaje en el área disponible sin deformar
    val scale = minOf(imageMaxWidth / foto.width.toFloat(), imageMaxHeight / foto.height.toFloat(), 1f)
    val matrix = Matrix()
    matrix.postScale(scale, scale)
    val scaledWidth = (foto.width * scale).toInt()
    val scaledHeight = (foto.height * scale).toInt()
    val scaledBitmap = Bitmap.createScaledBitmap(foto, scaledWidth, scaledHeight, true)

    // Dibujar la imagen centrada horizontalmente en el área
    val drawX = imageLeft + (imageMaxWidth - scaledWidth) / 2f
    val drawY = imageTop
    canvas.drawBitmap(scaledBitmap, drawX, drawY, paint)

    // Texto adicional debajo de la imagen
    val afterImageY = drawY + scaledHeight + 20f
    paint.textSize = 12f
    canvas.drawText("Observaciones: Foto tomada al entrar", 40f, afterImageY, paint)

    // Footer
    paint.textSize = 10f
    canvas.drawText("Generado por U-Park", 40f, pageInfo.pageHeight - 40f, paint)

    pdf.finishPage(page)

    // Escribir a archivo
    FileOutputStream(outFile).use { out ->
        pdf.writeTo(out)
    }
    pdf.close()
    return outFile
}
