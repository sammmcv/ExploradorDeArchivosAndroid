package com.escom.practica3

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.escom.practica3.ui.theme.Practica3Theme
import com.escom.practica3.ui.theme.ThemeType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    // Añadir estas propiedades
    private lateinit var recentFilesManager: RecentFilesManager
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var thumbnailCache: ThumbnailCache
    
    // Lanzador para solicitar permisos de lectura de almacenamiento (para versiones anteriores a Android 11)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, podemos acceder a los archivos
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            // No necesitamos llamar a accessFiles() explícitamente ya que la UI se actualizará
        } else {
            // Permiso denegado
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador para solicitar el permiso MANAGE_EXTERNAL_STORAGE (para Android 11 y superiores)
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Permiso de gestión de almacenamiento concedido", Toast.LENGTH_SHORT).show()
                // Aquí puedes llamar a la función que accede a los archivos si es necesario
            } else {
                Toast.makeText(this, "Permiso de gestión de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
   
    private fun openFileWithChooser(fileUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf") // Cambia el tipo MIME según el archivo
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val chooser = Intent.createChooser(intent, "Abrir con")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }
    internal fun openFile(context: Context, file: File) {
        // Añadir el archivo a recientes
        recentFilesManager.addRecentFile(file)
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val mimeType = getMimeType(file.name)
        
        when {
            // Archivos de texto
            mimeType?.startsWith("text/") == true || 
            file.extension.lowercase() in listOf("txt", "md", "json", "xml", "csv", "log") -> {
                val intent = Intent(context, TextFileViewerActivity::class.java).apply {
                    data = uri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
            
            // Imágenes
            mimeType?.startsWith("image/") == true -> {
                val intent = Intent(context, ImageViewerActivity::class.java).apply {
                    data = uri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            }
            
            // Otros tipos de archivos
            else -> {
                showOpenWithDialog(context, uri, mimeType)
            }
        }
    }
    
    private fun getMimeType(fileName: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    
    private fun showOpenWithDialog(context: Context, uri: Uri, mimeType: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Abrir archivo")
            .setMessage("¿Cómo deseas abrir este archivo?")
            .setPositiveButton("Abrir con otra aplicación") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType ?: "*/*")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                val chooser = Intent.createChooser(intent, "Abrir con")
                try {
                    context.startActivity(chooser)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No hay aplicaciones disponibles para abrir este archivo", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar los managers
        recentFilesManager = RecentFilesManager(this)
        favoritesManager = FavoritesManager(this)
        thumbnailCache = ThumbnailCache(this)
        
        // Verificar y solicitar permisos
        checkPermission()
    
        // Leer las preferencias de tema guardadas
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val savedThemeType = prefs.getInt("theme_type", 0)
        val savedThemeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        // Aplicar el modo de tema (claro/oscuro)
        AppCompatDelegate.setDefaultNightMode(savedThemeMode)
        
        // Convertir el tipo de tema guardado al enum ThemeType
        val themeType = when(savedThemeType) {
            1 -> ThemeType.ESCOM
            else -> ThemeType.IPN
        }
    
        enableEdgeToEdge()
        setContent {
            Practica3Theme(
                themeType = themeType,
                // El modo oscuro/claro ya está configurado por AppCompatDelegate
            ) {
                CompositionLocalProvider(LocalContext provides this) {
                    FileManagerApp(onOpenThemeSettings = { openThemeSettings() })
                }
            }
        }
    }
    
    // Add this function inside the MainActivity class
    private fun openThemeSettings() {
        val intent = Intent(this, ThemeSettingsActivity::class.java)
        startActivity(intent)
    }

    // Función para verificar y solicitar permisos
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Para Android 11 (API nivel 30) y superiores, se usa MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                // Mostrar un Toast explicando al usuario por qué necesitamos el permiso
                Toast.makeText(
                    this,
                    "Esta aplicación necesita acceso a todos los archivos para funcionar correctamente",
                    Toast.LENGTH_LONG
                ).show()
                
                try {
                    // Usar directamente la acción para gestionar todos los archivos
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    // Si hay algún error, intentar con la forma alternativa
                    try {
                        val uri = Uri.parse("package:${applicationContext.packageName}")
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                        manageStorageLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Si todo falla, mostrar un mensaje al usuario
                        Toast.makeText(
                            this,
                            "No se puede abrir la configuración de permisos. Por favor, habilita el permiso manualmente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            // Para versiones anteriores a Android 11, se usan READ_EXTERNAL_STORAGE y WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicitar el permiso READ_EXTERNAL_STORAGE
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    // Add these methods for managing favorites
    fun addToFavorites(file: File) {
        favoritesManager.addFavorite(file)
        Toast.makeText(this, "${file.name} añadido a favoritos", Toast.LENGTH_SHORT).show()
    }
    
    fun removeFromFavorites(file: File) {
        favoritesManager.removeFavorite(file.absolutePath)
        Toast.makeText(this, "${file.name} eliminado de favoritos", Toast.LENGTH_SHORT).show()
    }
    
    fun isFavorite(file: File): Boolean {
        return favoritesManager.isFavorite(file.absolutePath)
    }
    
    // Obtener archivos recientes
    fun getRecentFiles(): List<RecentFile> {
        return recentFilesManager.getRecentFiles()
    }
    
    // Obtener favoritos
    fun getFavorites(): List<FavoriteFile> {
        return favoritesManager.getFavorites()
    }
}

// Resto del código de la UI (FileManagerApp, FileItem, getFileSize)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerApp(onOpenThemeSettings: () -> Unit = {}) {
    var currentPath by remember { mutableStateOf(Environment.getExternalStorageDirectory().path) }
    var currentFiles by remember { mutableStateOf(listOf<File>()) }
    var parentPath by remember { mutableStateOf("") }
    val context = LocalContext.current
    val mainActivity = context as MainActivity
    
    // Estado para controlar la pestaña seleccionada
    var selectedTab by remember { mutableStateOf(0) }
    
    // Cargar archivos del directorio actual
    LaunchedEffect(currentPath) {
        val directory = File(currentPath)
        parentPath = directory.parent ?: ""
        currentFiles = directory.listFiles()
            ?.filter { !it.isHidden }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestor de Archivos", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (currentPath != Environment.getExternalStorageDirectory().path && selectedTab == 0) {
                        IconButton(onClick = { currentPath = parentPath }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenThemeSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configuración de Tema")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Folder, contentDescription = "Archivos") },
                    label = { Text("Archivos") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Recientes") },
                    label = { Text("Recientes") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> FilesTab(
                currentPath = currentPath,
                currentFiles = currentFiles,
                innerPadding = innerPadding,
                onFileClick = { clickedFile ->
                    if (clickedFile.isDirectory) {
                        currentPath = clickedFile.absolutePath
                    } else {
                        mainActivity.openFile(context, clickedFile)
                    }
                },
                onFavoriteToggle = { file, isFavorite ->
                    if (isFavorite) {
                        mainActivity.removeFromFavorites(file)
                    } else {
                        mainActivity.addToFavorites(file)
                    }
                },
                isFavorite = { file -> mainActivity.isFavorite(file) }
            )
            1 -> RecentFilesTab(
                recentFiles = mainActivity.getRecentFiles(),
                innerPadding = innerPadding,
                onFileClick = { recentFile ->
                    val file = recentFile.toFile()
                    if (file.exists()) {
                        if (file.isDirectory) {
                            currentPath = file.absolutePath
                            selectedTab = 0 // Cambiar a la pestaña de archivos
                        } else {
                            mainActivity.openFile(context, file)
                        }
                    } else {
                        Toast.makeText(context, "El archivo ya no existe", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            2 -> FavoritesTab(
                favorites = mainActivity.getFavorites(),
                innerPadding = innerPadding,
                onFileClick = { favoriteFile ->
                    val file = favoriteFile.toFile()
                    if (file.exists()) {
                        if (file.isDirectory) {
                            currentPath = file.absolutePath
                            selectedTab = 0 // Cambiar a la pestaña de archivos
                        } else {
                            mainActivity.openFile(context, file)
                        }
                    } else {
                        Toast.makeText(context, "El archivo ya no existe", Toast.LENGTH_SHORT).show()
                        mainActivity.removeFromFavorites(file)
                    }
                },
                onRemoveFavorite = { favoriteFile ->
                    val file = favoriteFile.toFile()
                    mainActivity.removeFromFavorites(file)
                }
            )
        }
    }
}

@Composable
fun FilesTab(
    currentPath: String,
    currentFiles: List<File>,
    innerPadding: PaddingValues,
    onFileClick: (File) -> Unit,
    onFavoriteToggle: (File, Boolean) -> Unit,
    isFavorite: (File) -> Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Mostrar ruta actual
        Text(
            text = "Ruta: $currentPath",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Lista de archivos
        if (currentFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Carpeta vacía o sin acceso")
            }
        } else {
            LazyColumn {
                items(currentFiles) { file ->
                    FileItemWithFavorite(
                        file = file,
                        onFileClick = onFileClick,
                        isFavorite = isFavorite(file),
                        onFavoriteToggle = { onFavoriteToggle(file, isFavorite(file)) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentFilesTab(
    recentFiles: List<RecentFile>,
    innerPadding: PaddingValues,
    onFileClick: (RecentFile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = "Archivos Recientes",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        if (recentFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay archivos recientes")
            }
        } else {
            LazyColumn {
                items(recentFiles) { recentFile ->
                    RecentFileItem(recentFile = recentFile, onFileClick = onFileClick)
                }
            }
        }
    }
}

@Composable
fun FavoritesTab(
    favorites: List<FavoriteFile>,
    innerPadding: PaddingValues,
    onFileClick: (FavoriteFile) -> Unit,
    onRemoveFavorite: (FavoriteFile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = "Favoritos",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay favoritos")
            }
        } else {
            LazyColumn {
                items(favorites) { favorite ->
                    FavoriteFileItem(
                        favoriteFile = favorite,
                        onFileClick = onFileClick,
                        onRemoveFavorite = onRemoveFavorite
                    )
                }
            }
        }
    }
}

@Composable
fun FileItemWithFavorite(
    file: File,
    onFileClick: (File) -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val lastModified = dateFormat.format(Date(file.lastModified()))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFileClick(file) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según tipo de archivo
        Icon(
            imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Default.Search,
            contentDescription = null,
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Información del archivo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (file.isDirectory) "Carpeta" else getFileSize(file.length()),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Fecha de modificación
        Text(
            text = lastModified,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(end = 8.dp)
        )
        
        // Botón de favorito
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }

    HorizontalDivider()
}

@Composable
fun RecentFileItem(recentFile: RecentFile, onFileClick: (RecentFile) -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val accessedDate = dateFormat.format(Date(recentFile.timestamp))
    val file = recentFile.toFile()
    val exists = file.exists()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = exists) { onFileClick(recentFile) }
            .padding(16.dp)
            .alpha(if (exists) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según tipo de archivo
        Icon(
            imageVector = if (recentFile.isDirectory) Icons.Filled.Folder else Icons.Default.Search,
            contentDescription = null,
            tint = if (recentFile.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Información del archivo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recentFile.name + (if (!exists) " (No disponible)" else ""),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = recentFile.path,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Fecha de acceso
        Text(
            text = accessedDate,
            style = MaterialTheme.typography.bodySmall
        )
    }

    HorizontalDivider()
}

@Composable
fun FavoriteFileItem(
    favoriteFile: FavoriteFile,
    onFileClick: (FavoriteFile) -> Unit,
    onRemoveFavorite: (FavoriteFile) -> Unit
) {
    val file = favoriteFile.toFile()
    val exists = file.exists()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = exists) { onFileClick(favoriteFile) }
            .padding(16.dp)
            .alpha(if (exists) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según tipo de archivo
        Icon(
            imageVector = if (favoriteFile.isDirectory) Icons.Filled.Folder else Icons.Default.Search,
            contentDescription = null,
            tint = if (favoriteFile.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Información del archivo
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favoriteFile.name + (if (!exists) " (No disponible)" else ""),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = favoriteFile.path,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Botón para eliminar de favoritos
        IconButton(onClick = { onRemoveFavorite(favoriteFile) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Eliminar de favoritos",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    HorizontalDivider()
}

// Añadir esta función si no existe
fun getFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}


@Composable
fun MainScreen(onOpenThemeSettings: () -> Unit) {
    // Here we're just calling the provided function parameter
    // No need to redefine it with another MainScreen call
    // Just add your UI components here
}