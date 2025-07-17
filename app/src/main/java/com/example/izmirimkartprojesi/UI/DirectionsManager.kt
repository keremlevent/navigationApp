package com.example.izmirimkartprojesi.UI

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.example.izmirimkartprojesi.R
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DirectionsManager(
    private val context: Context,
    private val mapView: MapView
) {
    private val routeSourceId = "route-source-id"
    private val routeLayerId = "route-layer-id"

    suspend fun getRoute(origin: Point, destination: Point): DirectionsRoute? {
        return withContext(Dispatchers.IO){
            val client = MapboxDirections.builder()
                .accessToken(context.getString(R.string.mapbox_access_token))
                .origin(origin)
                .destination(destination)
                .overview("full")
                .profile("walking") // yürüme profili (bus için değiştirilebilir)
                .build()

            try{
                val response = client.executeCall()
                if(response.isSuccessful && !response.body()?.routes().isNullOrEmpty()){
                    response.body()?.routes()?.first()
                }else{
                    Log.e("DirectionsManager","Rota alınamadı. Hata: ${response.message()}")
                    null
                }
            }catch(e: Exception){
                Log.e("DirectionsManager","İstisna: ${e.localizedMessage}",e)
                null
            }
        }
    }

    fun drawRoute(route: DirectionsRoute) {
        val geometry = route.geometry() ?: return
        val routeLineString = LineString.fromPolyline(geometry, 6)

        mapView.mapboxMap.getStyle { style ->
            val existingSource = style.getSourceAs<GeoJsonSource>(routeSourceId)

            if (existingSource == null) {
                // Yeni kaynak ve katman ekleniyor
                val routeSource = geoJsonSource(routeSourceId) {
                    geometry(routeLineString)
                }
                style.addSource(routeSource)

                style.addLayerBelow(
                    lineLayer(routeLayerId, routeSourceId) {
                        lineColor(Color.parseColor("#3b9ddd"))
                        lineWidth(6.0)
                        lineCap(LineCap.ROUND)
                        lineJoin(LineJoin.ROUND)
                    },
                    "road-label"
                )
            } else {
                // Sadece mevcut kaynağın geometrisi güncelleniyor
                existingSource.geometry(routeLineString)
            }
        }
    }
}
