package com.escom.practica3

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Obtener la lista de favoritos
    fun getFavorites(): List<FavoriteFile> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteFile>>() {}.type
        return gson.fromJson(json, type)
    }
    
    // Añadir un archivo a favoritos
    fun addFavorite(file: File) {
        val favorites = getFavorites().toMutableList()
        
        // Comprobar si el archivo ya está en favoritos
        if (favorites.any { it.path == file.absolutePath }) {
            return // Ya está en favoritos
        }
        
        // Añadir a favoritos
        favorites.add(FavoriteFile(
            name = file.name,
            path = file.absolutePath,
            isDirectory = file.isDirectory,
            addedTimestamp = System.currentTimeMillis()
        ))
        
        // Guardar la lista actualizada
        saveFavorites(favorites)
    }
    
    // Eliminar un archivo de favoritos
    fun removeFavorite(filePath: String) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.path == filePath }
        saveFavorites(favorites)
    }
    
    // Comprobar si un archivo está en favoritos
    fun isFavorite(filePath: String): Boolean {
        return getFavorites().any { it.path == filePath }
    }
    
    // Guardar la lista de favoritos
    private fun saveFavorites(favorites: List<FavoriteFile>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "FavoritesPrefs"
        private const val KEY_FAVORITES = "favorites"
    }
}

// Clase para representar un archivo favorito
data class FavoriteFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val addedTimestamp: Long
) {
    fun toFile(): File = File(path)
}