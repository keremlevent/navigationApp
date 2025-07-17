package com.example.izmirimkartprojesi.UI

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.izmirimkartprojesi.R
import com.example.izmirimkartprojesi.ViewModel.BusStopViewModel
import com.mapbox.maps.MapView
import kotlinx.coroutines.launch
import com.example.izmirimkartprojesi.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView
    lateinit var mapManager: MapManager
    private lateinit var permissionManager: PermissionManager
    private val busStopViewModel: BusStopViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView

        // Manager sınıflarının başlatılması
        mapManager = MapManager(this, mapView)

        permissionManager = PermissionManager(
            activity = this,
            onPermissionGranted = { mapManager.enableLocationComponent() }
        )

        // Haritayı başlat ve izin kontrolünü tetikle
        mapManager.initializeMap {
            permissionManager.checkLocationPermissions()
        }

        // Durakları çekmek ve gözlemlemek
        busStopViewModel.fetchBusStops()
        observeBusStops()

        // Haritayı kullanıcıya merkezleme işlemi
        binding.centerMapButton.setOnClickListener {
            mapManager.centerToUserLocationAndResumeFollow()
        }

        // navigasyon çıkış lsitener
        binding.exitRouteModeButton.setOnClickListener {
            exitRouteMode()
        }

        val bottomSheetContainer = findViewById<LinearLayout>(R.id.bottomSheetContainer)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN // ilk durumda gizli

    }

    private fun observeBusStops() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                busStopViewModel.busStops.collect { busStops ->
                    if (busStops.isNotEmpty()) {
                        BusStopRenderer(this@MainActivity, mapView, busStops).showBusStopsOnMap()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                busStopViewModel.errorMessage.collect { msg ->
                    msg?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun exitRouteMode() {
        mapManager.stopFollowingUser()
        mapManager.clearRoutePolyline()
        mapManager.centerToUserLocation()

        binding.bottomSheetContainer.visibility = View.GONE
        binding.exitRouteModeButton.visibility = View.GONE
    }
}
