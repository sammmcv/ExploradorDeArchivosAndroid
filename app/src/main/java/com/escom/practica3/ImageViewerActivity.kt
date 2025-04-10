package com.escom.practica3

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

class ImageViewerActivity : AppCompatActivity() {
    private lateinit var imageView: SubsamplingScaleImageView
    private var currentRotation = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        
        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Visor de Imágenes"

        imageView = findViewById(R.id.imageView)
        val imageUri = intent.data

        if (imageUri != null) {
            loadImage(imageUri)
        }
    }
    
    private fun loadImage(uri: Uri) {
        try {
            imageView.setImage(ImageSource.uri(uri))
            
            // Configurar opciones de zoom
            imageView.maxScale = 10f
            imageView.minScale = 0.5f
        } catch (e: Exception) {
            // Manejar error al cargar la imagen
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_viewer_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_rotate_left -> {
                rotateImage(-90)
                true
            }
            R.id.action_rotate_right -> {
                rotateImage(90)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun rotateImage(degrees: Int) {
        currentRotation = (currentRotation + degrees) % 360
        if (currentRotation < 0) currentRotation += 360
        
        imageView.orientation = currentRotation
    }
}