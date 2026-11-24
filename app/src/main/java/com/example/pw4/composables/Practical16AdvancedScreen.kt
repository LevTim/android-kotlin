package com.example.pw4.composables

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pw4.data.ChargingStation
import com.example.pw4.data.ChargingStationsRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.io.File

/**
 * ПЗ №16 (Просунутий рівень, OpenStreetMap без API-ключа):
 * - Робота з GPS (FusedLocationProviderClient) та обробка прав доступу;
 * - Відображення зарядних станцій та позиції користувача на мапі OpenStreetMap;
 * - Багаторівнева фільтрація (конектор + статус) і сортування через derivedStateOf;
 * - Повна двостороння синхронізація між LazyColumn та маркерами мапи.
 */
@SuppressLint("MissingPermission")
@Composable
fun Practical16AdvancedScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Налаштування osmdroid: унікальний User-Agent (щоб уникнути блокування 403 для com.example.*)
    // та окрема директорія кешу для завантаження чистих тайлів вулиць
    remember {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid_prefs_v2", Context.MODE_PRIVATE))
            userAgentValue = "KPI_PW4_EVChargingStationsApp/1.0"
            osmdroidBasePath = context.cacheDir
            osmdroidTileCache = File(context.cacheDir, "osmdroid_tiles_v2")
        }
        true
    }

    // Офіційний безкоштовний сервер тайлів OpenStreetMap (працює без API-ключа)
    val osmTileSource = remember {
        XYTileSource(
            "OpenStreetMap_EV",
            0,
            19,
            256,
            ".png",
            arrayOf("https://tile.openstreetmap.org/"),
            "© OpenStreetMap contributors"
        )
    }

    // Клієнт Google Play Services для отримання поточних GPS-координат користувача
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Колекція зарядних станцій для електромобілів
    val stations = remember {
        mutableStateListOf<ChargingStation>().apply {
            addAll(ChargingStationsRepository.getSampleStations())
        }
    }

    // Стани багаторівневої фільтрації та сортування
    var selectedConnector by remember { mutableStateOf("Всі") }
    var selectedStatus by remember { mutableStateOf("Всі") }
    var sortByPowerAsc by remember { mutableStateOf(false) }

    // Обрана зарядна станція (синхронізує виділення картки та фокус маркера на мапі)
    var selectedStation by remember { mutableStateOf<ChargingStation?>(null) }

    // Стан дозволу на геолокацію та поточних координат користувача (за замовчуванням центр Києва)
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var userLocation by remember { mutableStateOf(GeoPoint(50.4501, 30.5234)) }

    // Реактивний відфільтрований і відсортований список через derivedStateOf
    val filteredStations by remember(stations, selectedConnector, selectedStatus, sortByPowerAsc) {
        derivedStateOf {
            stations
                .filter { selectedConnector == "Всі" || it.connectorType == selectedConnector }
                .filter { selectedStatus == "Всі" || it.status == selectedStatus }
                .sortedBy { if (sortByPowerAsc) it.powerKw else -it.powerKw }
        }
    }

    // Ініціалізація примірника MapView (OpenStreetMap)
    val mapView = remember {
        MapView(context).apply {
            setTileSource(osmTileSource)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
            controller.setZoom(12.2)
            controller.setCenter(GeoPoint(50.4550, 30.5234))
        }
    }

    // Управління життєвим циклом MapView (onResume / onPause / onDetach)
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    // Обробник системного діалогу запиту дозволу на GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                }
            }
        }
    }

    // Отримання поточних GPS-координат при старті екрана
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Верхня панель багаторівневої фільтрації та сортування
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StationFilterDropdown(
                label = "Тип",
                options = listOf("Всі", "CCS2", "Type 2", "CHAdeMO"),
                selectedOption = selectedConnector,
                onOptionSelected = { selectedConnector = it }
            )

            StationFilterDropdown(
                label = "Статус",
                options = listOf("Всі", "Вільна", "Заряджає", "На ремонті"),
                selectedOption = selectedStatus,
                onOptionSelected = { selectedStatus = it }
            )

            Button(onClick = { sortByPowerAsc = !sortByPowerAsc }) {
                Text(if (sortByPowerAsc) "кВт ↑" else "кВт ↓")
            }
        }

        // Кнопка переходу до поточної GPS-позиції користувача
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (hasLocationPermission) {
                OutlinedButton(
                    onClick = {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                userLocation = GeoPoint(it.latitude, it.longitude)
                            }
                            mapView.controller.animateTo(userLocation, 14.5, 600L)
                        }
                    }
                ) {
                    Text("Моя GPS-позиція")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Надати доступ до GPS")
                }
            }
        }

        // Основна робоча область: зліва список LazyColumn, справа мапа OpenStreetMap
        Row(modifier = Modifier.fillMaxSize()) {
            // Ліва частина (38%): список відфільтрованих зарядних станцій
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F5F5))
            ) {
                itemsIndexed(filteredStations, key = { _, item -> item.id }) { _, station ->
                    val isSelected = station.id == selectedStation?.id
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .clickable {
                                // 1. Виділяємо картку у списку
                                selectedStation = station
                                // 2. Автоматично фокусуємо камеру мапи на обраній ЕЗС
                                mapView.controller.animateTo(
                                    GeoPoint(station.latitude, station.longitude),
                                    15.0,
                                    600L
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFBBDEFB) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${station.connectorType} • ${station.status}",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (station.status) {
                                    "Вільна" -> Color(0xFF2E7D32)
                                    "Заряджає" -> Color(0xFFEF6C00)
                                    else -> Color(0xFFC62828)
                                }
                            )
                            Text(
                                text = "Потужність: ${station.powerKw.toInt()} кВт",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Порти: ${station.freePorts}/${station.totalPorts}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Права частина (62%): контейнер із clipToBounds(), щоб мапа не перекривала сусідні елементи
            Box(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight()
                    .clipToBounds()
            ) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { map ->
                        InfoWindow.closeAllInfoWindowsOn(map)
                        map.overlays.clear()

                        var selectedMarkerToShow: Marker? = null

                        // Додаємо маркери для всіх відфільтрованих зарядних станцій
                        filteredStations.forEachIndexed { index, station ->
                            val isSelected = station.id == selectedStation?.id
                            val pinColor = when {
                                isSelected -> Color(0xFF1565C0)
                                station.status == "Вільна" -> Color(0xFF2E7D32)
                                station.status == "Заряджає" -> Color(0xFFEF6C00)
                                else -> Color(0xFFC62828)
                            }

                            val marker = Marker(map).apply {
                                position = GeoPoint(station.latitude, station.longitude)
                                title = station.name
                                snippet = "${station.connectorType} (${station.powerKw.toInt()} кВт) • ${station.status}\n" +
                                        "Порти: ${station.freePorts}/${station.totalPorts} • ${station.address}"
                                icon = createPinDrawable(context, pinColor, isSelected)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                                setOnMarkerClickListener { clickedMarker, _ ->
                                    selectedStation = station
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index)
                                    }
                                    clickedMarker.showInfoWindow()
                                    true
                                }
                            }

                            map.overlays.add(marker)
                            if (isSelected) {
                                selectedMarkerToShow = marker
                            }
                        }

                        // Додаємо маркер поточної GPS-позиції користувача
                        if (hasLocationPermission) {
                            val userMarker = Marker(map).apply {
                                position = userLocation
                                title = "Ви тут (GPS)"
                                snippet = "Ваше поточне місцезнаходження"
                                icon = createPinDrawable(context, Color(0xFF6A1B9A), isSelected = false)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            map.overlays.add(userMarker)
                        }

                        map.invalidate()

                        // Відкриваємо InfoWindow для вибраної станції
                        selectedMarkerToShow?.showInfoWindow()
                    }
                )
            }
        }
    }
}

/**
 * Генерує векторний маркер-пін заданого кольору без зовнішніх XML-ресурсів.
 */
private fun createPinDrawable(
    context: Context,
    color: Color,
    isSelected: Boolean
): Drawable {
    val size = if (isSelected) 92 else 74
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val cx = size / 2f
    val cy = size * 0.36f
    val radius = size * 0.28f

    val triangle = Path().apply {
        moveTo(cx - radius * 0.85f, cy + radius * 0.35f)
        lineTo(cx + radius * 0.85f, cy + radius * 0.35f)
        lineTo(cx, size * 0.94f)
        close()
    }
    canvas.drawPath(triangle, paint)
    canvas.drawCircle(cx, cy, radius, paint)
    canvas.drawCircle(cx, cy, radius, strokePaint)

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(cx, cy, radius * 0.42f, paint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Допоміжний компонент випадаючого меню (DropdownMenu) для фільтрації на мапі.
 */
@Composable
private fun StationFilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text("$label: $selectedOption")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}