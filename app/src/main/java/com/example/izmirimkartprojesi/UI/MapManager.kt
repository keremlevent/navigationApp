package com.example.izmirimkartprojesi.UI

import android.content.Context
import com.example.izmirimkartprojesi.R
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures

class MapManager(
    private val context: Context,
    private val mapView: MapView
) {
    private var lastKnownPosition: Point? = null
    private var hasCenteredMap = false

    private var positionListener: OnIndicatorPositionChangedListener? = null
    private var bearingListener: OnIndicatorBearingChangedListener? = null

    private val zoomRate = 14.0

    private var isFollowingUser = false

    fun initializeMap(onMapReady: () -> Unit) {
        mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
            onMapReady()
        }
    }

    fun enableLocationComponent() {
        val locationComponent = mapView.location

        locationComponent.updateSettings {
            enabled = true
            pulsingEnabled = true
            puckBearingEnabled = true
            puckBearing = PuckBearing.HEADING

            locationPuck = LocationPuck2D(
                topImage = ImageHolder.from(R.drawable.my_top),
                bearingImage = ImageHolder.from(R.drawable.my_bearing)
            )
        }

        positionListener = object : OnIndicatorPositionChangedListener {
            override fun onIndicatorPositionChanged(point: Point) {
                lastKnownPosition = point // Son konumu güncelleyebilirsiniz, isteğe bağlı
                if (!hasCenteredMap) {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(zoomRate) // İstediğiniz zoom seviyesi
                            .build()
                    )
                    hasCenteredMap = true
                    locationComponent.removeOnIndicatorPositionChangedListener(this)

                    positionListener?.let { listenerToRemove ->
                        locationComponent.removeOnIndicatorPositionChangedListener(listenerToRemove)
                        positionListener = null
                    }
                }
            }
        }

        // Tanımladığımız dinleyiciyi ekliyoruz
        positionListener?.let {
            locationComponent.addOnIndicatorPositionChangedListener(it)

        }
    }


    fun getLastKnownLocation(): Point? {
        return lastKnownPosition
    }

    fun followUserDirection() {
        val mapboxMap = mapView.mapboxMap

        isFollowingUser = true

        // Başlangıç ayarları: pitch ve zoom
        val initialCamera = CameraOptions.Builder()
            .pitch(30.0)
            .zoom(16.0)
            .build()

        mapView.camera.easeTo(
            cameraOptions = initialCamera,
            MapAnimationOptions.mapAnimationOptions {
                duration(1000)
            }
        )
        mapboxMap.setCamera(initialCamera)


        positionListener = OnIndicatorPositionChangedListener { point ->
            val updatedCamera = CameraOptions.Builder()
                .center(point)
                .build()
            mapView.mapboxMap.setCamera(updatedCamera)
        }
        mapView.location.addOnIndicatorPositionChangedListener(positionListener!!)


        bearingListener = OnIndicatorBearingChangedListener { bearing ->
            val updatedCamera = CameraOptions.Builder()
                .bearing(bearing)
                .build()
            mapView.mapboxMap.setCamera(updatedCamera)
        }
        mapView.location.addOnIndicatorBearingChangedListener(bearingListener!!)

        // Kullanıcı haritayı kaydırırsa takip iptal edilir
        mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: MoveGestureDetector) {
                if (isFollowingUser) {
                    stopFollowingUser()
                }
            }

            override fun onMove(detector: MoveGestureDetector): Boolean = false
            override fun onMoveEnd(detector: MoveGestureDetector) {}
        })
    }

    fun centerToUserLocation() {
        lastKnownPosition?.let { point ->
            mapView.mapboxMap.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .pitch(0.0)
                    .zoom(zoomRate)
                    .build()
            )
        }
    }

    fun stopFollowingUser() {
        val locationComponent = mapView.location

        positionListener?.let {
            locationComponent.removeOnIndicatorPositionChangedListener(it)
            positionListener = null
        }

        bearingListener?.let {
            locationComponent.removeOnIndicatorBearingChangedListener(it)
            bearingListener = null
        }

        isFollowingUser = false
    }



    fun clearRoutePolyline() {
        mapView.mapboxMap.getStyle { style ->
            style.removeStyleLayer("route-layer-id")
            style.removeStyleSource("route-source-id")
        }
    }

    fun centerToUserLocationAndResumeFollow() {
        centerToUserLocation()
    }
}
