package com.escom.practica3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class ThemeSettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)
        
        // Configurar la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configuración de Tema"
        
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radioGroupTheme)
        val radioGroupMode = findViewById<RadioGroup>(R.id.radioGroupMode)
        val btnApply = findViewById<Button>(R.id.btnApplyTheme)
        
        // Cargar preferencias guardadas
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val savedTheme = prefs.getInt("theme_type", 0)
        val savedMode = prefs.getInt("theme_mode", -1)
        
        // Establecer selección guardada
        when (savedTheme) {
            0 -> radioGroupTheme.check(R.id.radioIPN)
            1 -> radioGroupTheme.check(R.id.radioESCOM)
        }
        
        when (savedMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> radioGroupMode.check(R.id.radioLight)
            AppCompatDelegate.MODE_NIGHT_YES -> radioGroupMode.check(R.id.radioDark)
            else -> radioGroupMode.check(R.id.radioSystem)
        }
        
        // Botón para aplicar cambios
        btnApply.setOnClickListener {
            // Inside the btnApply.setOnClickListener block
            // Guardar selección de tema
            val selectedThemeId = radioGroupTheme.checkedRadioButtonId
            val themeType = when (selectedThemeId) {
                R.id.radioESCOM -> 1  // ESCOM
                else -> 0  // IPN por defecto
            }
            
            // Guardar selección de modo
            val selectedModeId = radioGroupMode.checkedRadioButtonId
            val themeMode = when (selectedModeId) {
                R.id.radioLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.radioDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            // Guardar preferencias
            prefs.edit().apply {
                putInt("theme_type", themeType)
                putInt("theme_mode", themeMode)
                apply()
            }
            
            // Aplicar modo de tema
            AppCompatDelegate.setDefaultNightMode(themeMode)
            
            // Reiniciar la aplicación para aplicar el tema
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        // Replace deprecated onBackPressed() with onBackPressedDispatcher
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}