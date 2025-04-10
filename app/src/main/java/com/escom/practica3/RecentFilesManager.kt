package com.escom.practica3

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*

class RecentFilesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Obtener la lista de archivos recientes
    fun getRecentFiles(): List<RecentFile> {
        val json = prefs.getString(KEY_RECENT_FILES, null) ?: return emptyList()
        val type = object : TypeToken<List<RecentFile>>() {}.type
        return gson.fromJson(json, type)
    }
    
    // Añadir un archivo al historial
    fun addRecentFile(file: File) {
        val recentFiles = getRecentFiles().toMutableList()
        
        // Comprobar si el archivo ya está en la lista
        val existingIndex = recentFiles.indexOfFirst { it.path == file.absolutePath }
        if (existingIndex != -1) {
            // Si existe, eliminarlo para añadirlo de nuevo al principio
            recentFiles.removeAt(existingIndex)
        }
        
        // Añadir el nuevo archivo al principio
        recentFiles.add(0, RecentFile(
            name = file.name,
            path = file.absolutePath,
            isDirectory = file.isDirectory,
            timestamp = System.currentTimeMillis()
        ))
        
        // Limitar la lista a MAX_RECENT_FILES
        if (recentFiles.size > MAX_RECENT_FILES) {
            recentFiles.removeAt(recentFiles.size - 1)
        }
        
        // Guardar la lista actualizada
        saveRecentFiles(recentFiles)
    }
    
    // Limpiar el historial
    fun clearRecentFiles() {
        prefs.edit().remove(KEY_RECENT_FILES).apply()
    }
    
    // Guardar la lista de archivos recientes
    private fun saveRecentFiles(recentFiles: List<RecentFile>) {
        val json = gson.toJson(recentFiles)
        prefs.edit().putString(KEY_RECENT_FILES, json).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "RecentFilesPrefs"
        private const val KEY_RECENT_FILES = "recent_files"
        private const val MAX_RECENT_FILES = 20
    }
}

// Clase para representar un archivo reciente
data class RecentFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val timestamp: Long
) {
    fun toFile(): File = File(path)
}