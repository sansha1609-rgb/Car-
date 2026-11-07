package com.example.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineMapCacheManager(
    private val scope: CoroutineScope
) {
    private val initialRegions = listOf(
        CachedRegion(
            id = "reg_1",
            title = "NH-48 Corridor & Expressway Hub",
            sizeMb = 185,
            isDownloaded = true,
            downloadProgress = 1.0f,
            roadsCount = "62,400 km vectors",
            description = "High-speed highway offline maps & emergency service stations"
        ),
        CachedRegion(
            id = "reg_2",
            title = "Himalayan Remote Mountain Pass (Leh - Manali)",
            sizeMb = 240,
            isDownloaded = true,
            downloadProgress = 1.0f,
            roadsCount = "18,200 km vectors",
            description = "Zero cellular zone cached topography, steep passes & offline SOS points"
        ),
        CachedRegion(
            id = "reg_3",
            title = "Delhi NCR Mega Urban Grid & Ring Roads",
            sizeMb = 210,
            isDownloaded = true,
            downloadProgress = 1.0f,
            roadsCount = "95,000 km vectors",
            description = "Lane-level geometry, junction maneuvers & flyover offline guidance"
        ),
        CachedRegion(
            id = "reg_4",
            title = "Western Ghats Coastal & Forest Ghats",
            sizeMb = 135,
            isDownloaded = false,
            downloadProgress = 0f,
            roadsCount = "31,800 km vectors",
            description = "Dense rainforest offline contour navigation & remote fuel pumps"
        ),
        CachedRegion(
            id = "reg_5",
            title = "Thar Desert Cross-Country Corridor",
            sizeMb = 110,
            isDownloaded = false,
            downloadProgress = 0f,
            roadsCount = "24,500 km vectors",
            description = "Isolated highway network with offline oasis & refueling landmarks"
        )
    )

    private val _regions = MutableStateFlow<List<CachedRegion>>(initialRegions)
    val regions: StateFlow<List<CachedRegion>> = _regions.asStateFlow()

    fun downloadRegion(regionId: String) {
        val current = _regions.value.find { it.id == regionId } ?: return
        if (current.isDownloaded) return

        scope.launch(Dispatchers.Default) {
            for (step in 1..10) {
                delay(300L)
                val progress = step / 10f
                _regions.value = _regions.value.map {
                    if (it.id == regionId) {
                        it.copy(downloadProgress = progress, isDownloaded = progress >= 1.0f)
                    } else it
                }
            }
        }
    }

    fun deleteRegion(regionId: String) {
        _regions.value = _regions.value.map {
            if (it.id == regionId) {
                it.copy(isDownloaded = false, downloadProgress = 0f)
            } else it
        }
    }

    fun getTotalCacheUsageMb(): Int {
        return _regions.value.filter { it.isDownloaded }.sumOf { it.sizeMb }
    }
}
