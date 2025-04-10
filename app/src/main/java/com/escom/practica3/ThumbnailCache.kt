package com.escom.practica3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File

class ThumbnailCache(private val context: Context) {
    
    // Cargar y cachear una miniatura para un archivo
    fun loadThumbnail(file: File, imageView: ImageView, size: Int = 100) {
        if (isImageFile(file)) {
            // Para archivos de imagen, carga una miniatura
            Glide.with(context)
                .load(file)
                .override(size, size)
                .centerCrop()
                .into(imageView)
        } else if (file.isDirectory) {
            // Para directorios, muestra un icono de carpeta
            imageView.setImageResource(R.drawable.ic_folder)
        } else {
            // Para otros tipos de archivo, muestra un icono genérico
            imageView.setImageResource(R.drawable.ic_file)
        }
    }
    
    // Cargar una miniatura y devolverla como Bitmap (útil para composables)
    fun loadThumbnailAsBitmap(file: File, size: Int = 100, callback: (Bitmap?) -> Unit) {
        if (!isImageFile(file)) {
            callback(null)
            return
        }
        
        Glide.with(context)
            .asBitmap()
            .load(file)
            .override(size, size)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback(resource)
                }
                
                override fun onLoadCleared(placeholder: Drawable?) {
                    callback(null)
                }
                
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    callback(null)
                }
            })
    }
    
    // Limpiar la caché
    fun clearCache() {
        Glide.get(context).clearMemory()
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
    
    // Comprobar si un archivo es una imagen
    private fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
    }
}