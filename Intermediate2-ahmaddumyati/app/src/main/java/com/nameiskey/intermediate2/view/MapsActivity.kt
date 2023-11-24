package com.nameiskey.intermediate2.view

import android.content.res.Resources
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.nameiskey.intermediate2.R
import com.nameiskey.intermediate2.databinding.ActivityMapsBinding
import com.nameiskey.intermediate2.model.StoryList
import com.nameiskey.intermediate2.util.HttpResponseCode
import com.nameiskey.intermediate2.util.Preferences
import com.nameiskey.intermediate2.viewmodel.MapsViewModel
import java.io.IOException
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapsViewModel: MapsViewModel

    private lateinit var mUserPreference: Preferences

    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.text_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapsViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[MapsViewModel::class.java]

        mUserPreference = Preferences(this)
        val userToken = mUserPreference.getUser()

        mapsViewModel.setAuth(userToken.token)

        mapsViewModel.isLoading.observe(this) {
            if (it) Toast.makeText(
                this@MapsActivity,
                resources.getString(R.string.text_loading),
                Toast.LENGTH_SHORT
            ).show()
        }

        mapsViewModel.responseType.observe(this) { response ->
            if (response != HttpResponseCode.SUCCESS) showMarkerFailed(response)
        }
    }

    private fun showMarkerFailed(statusCode: Int?) {
        val message = when (statusCode) {
            HttpResponseCode.NOT_FOUND -> resources.getString(R.string.error_story_not_found)
            HttpResponseCode.FAILED -> resources.getString(R.string.error_story_failed)
            else -> resources.getString(R.string.error_server)
        }
        Toast.makeText(this@MapsActivity, "$statusCode: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                if (mMap.mapType != GoogleMap.MAP_TYPE_NORMAL)
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                if (mMap.mapType != GoogleMap.MAP_TYPE_SATELLITE)
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                if (mMap.mapType != GoogleMap.MAP_TYPE_TERRAIN)
                    mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                if (mMap.mapType != GoogleMap.MAP_TYPE_HYBRID)
                    mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        setMapStyle()

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        mapsViewModel.story.observe(this) {
            setMarker(mMap, it)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun setMarker(mMap: GoogleMap, storyList: List<StoryList>) {
        storyList.forEach { story ->
            val latLng = story.lat?.let { lat ->
                story.lon?.let { lon ->
                    LatLng(lat, lon)
                }
            }

            latLng?.let { position ->
                val addressName = getAddressName(position.latitude, position.longitude)
                MarkerOptions().position(position)
                    .title(story.name)
                    .snippet(resources.getString(R.string.desc_address, addressName))
            }?.let { marker ->
                mMap.addMarker(marker)
            }

            if (latLng != null) {
                boundsBuilder.include(latLng)
            }
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                300
            )
        )
    }

    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
        try {
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressName
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}