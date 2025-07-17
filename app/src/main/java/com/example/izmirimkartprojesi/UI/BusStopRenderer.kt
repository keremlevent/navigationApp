package com.example.izmirimkartprojesi.UI

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.izmirimkartprojesi.R
import com.example.izmirimkartprojesi.model.DuraklarModel
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

class BusStopRenderer(
    private val activity: AppCompatActivity,
    private val mapView: MapView,
    private val busStops: List<DuraklarModel>
) {
    private lateinit var dialog: BottomSheetDialog


    private val markerBitmap: Bitmap by lazy {
        val originalBitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.marker)
        originalBitmap.scale(70, 70, false)
    }
    
    fun showBusStopsOnMap() {
        val annotationManager = mapView.annotations.createPointAnnotationManager()
        dialog = BottomSheetDialog(activity)

        busStops.forEach { busStop ->
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(busStop.longitude, busStop.latitude))
                .withIconImage(markerBitmap)

            val pointAnnotation = annotationManager.create(pointAnnotationOptions)

            annotationManager.addClickListener { annotation ->
                if (annotation.id == pointAnnotation.id) {
                    showBottomSheet(busStop)
                    true
                } else false
            }
        }
    }


    private fun showBottomSheet(stop: DuraklarModel) {
        val bottomSheet = LayoutInflater.from(activity)
            .inflate(R.layout.bottom_sheet_stop_info, null)

        bottomSheet.findViewById<TextView>(R.id.stopNameTextView).text = stop.name
        bottomSheet.findViewById<TextView>(R.id.stopIdTextView).text = "Durak ID : ${stop.id}"
        bottomSheet.findViewById<TextView>(R.id.routesTextView).text = "Hatlar : ${stop.routes}"


        val navigationBtn = bottomSheet.findViewById<Button>(R.id.navigationButton)

        navigationBtn.setOnClickListener {
            val userLocation = (activity as MainActivity).mapManager.getLastKnownLocation() // düzenlenmeli gibi

            if (userLocation != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val directionsManager = DirectionsManager(activity, mapView)
                    val route = directionsManager.getRoute(
                        origin = userLocation,
                        destination = Point.fromLngLat(stop.longitude, stop.latitude)
                    )
                    if (route != null) {
                        directionsManager.drawRoute(route)
                        (activity as? MainActivity)?.mapManager?.followUserDirection()
                        dialog.dismiss()
                        showRouteInfoBottomSheet(route)
                        (activity as MainActivity).binding.exitRouteModeButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(activity, "Rota alınamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(activity, "Kullanıcı konumu alınamadı!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(bottomSheet)
        dialog.show()
    }

    private fun showRouteInfoBottomSheet(route: DirectionsRoute) {
        val routeInfoBottomSheet = activity.findViewById<View>(R.id.bottomSheetContainer)
        val bottomSheetBehavior = BottomSheetBehavior.from(routeInfoBottomSheet).apply{
            isHideable = false
            peekHeight = 150
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        val durationSec = route.duration()?.toLong() ?: 0L
        val distanceMeters = route.distance() ?: 0.0

        val durationMinutes = durationSec / 60
        val durationText = if (durationMinutes >= 60) {
            "${durationMinutes / 60} sa ${durationMinutes % 60} dk"
        } else {
            "$durationMinutes dk"
        }

        val distanceText = if (distanceMeters >= 1000) {
            "%.2f km".format(distanceMeters / 1000)
        } else {
            "%.0f metre".format(distanceMeters)
        }

        val currentTimeMillis = System.currentTimeMillis()
        val arrivalTimeMillis = currentTimeMillis + (durationSec * 1000)

        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val startTimeStr = formatter.format(java.util.Date(currentTimeMillis))
        val endTimeStr = formatter.format(java.util.Date(arrivalTimeMillis))

        routeInfoBottomSheet.findViewById<TextView>(R.id.arrivalTimeText).text =
            "Süre: $durationText"
        routeInfoBottomSheet.findViewById<TextView>(R.id.distanceText).text =
            "Mesafe: $distanceText"
        routeInfoBottomSheet.findViewById<TextView>(R.id.startTimeText).text =
            "Başlangıç: $startTimeStr"
        routeInfoBottomSheet.findViewById<TextView>(R.id.endTimeText).text =
            "Bitiş: $endTimeStr"


        routeInfoBottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        mapView.mapboxMap.addOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            true
        }
    }
}

