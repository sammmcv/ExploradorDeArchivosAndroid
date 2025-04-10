package com.escom.practica3

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class TextFileViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_viewer)
        
        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Visor de Texto"

        val textViewContent = findViewById<TextView>(R.id.textViewContent)
        val fileUri = intent.data
        
        // Obtener el nombre del archivo para mostrar en el título
        fileUri?.let {
            val fileName = getFileName(it)
            supportActionBar?.subtitle = fileName
        }

        if (fileUri != null) {
            try {
                val content = readTextFromUri(fileUri)
                textViewContent.text = content
            } catch (e: Exception) {
                textViewContent.text = "Error al leer el archivo: ${e.message}"
            }
        } else {
            textViewContent.text = "No se proporcionó ningún archivo."
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append('\n')
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
    
    private fun getFileName(uri: Uri): String {
        var result = "Archivo desconocido"
        
        // Intentar obtener el nombre del archivo desde el URI
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex("_display_name")
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex)
                }
            }
        }
        
        return result
    }
}