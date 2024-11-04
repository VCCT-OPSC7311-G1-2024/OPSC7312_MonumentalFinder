package com.example.monumentalfinder.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.monumentalfinder.BuildConfig
import com.example.monumentalfinder.MyFirebaseMessagingService
import com.example.monumentalfinder.R
import com.example.monumentalfinder.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient


class HomeFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient

    // Step Sensor
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var stepsTextView: TextView
    private var stepCount: Int = 0 // Initialize step count

    private var spinCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val key = BuildConfig.GOOGLE_MAPS_API_KEY

        // Initialize the Places SDK (provide your API key here)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), key)
        }

        // Initialize the placesClient
        placesClient = Places.createClient(requireContext())

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadSpinCount()

        // Initialize step sensor
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepsTextView = binding.stepsTextView

        // Check for activity recognition permission on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION_REQUEST_CODE)
            }
        }

        if (stepSensor == null) {
            Toast.makeText(requireContext(), "No Step Counter Sensor available!", Toast.LENGTH_SHORT).show()
        } else {
            MyFirebaseMessagingService.showNotification(
                requireContext(),
                "Steps",
                "Step Counter Active"
            )
        }

        return root
    }

    private fun loadSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        spinCount = sharedPreferences.getInt("spin_count", 0) // Default to 0 if not set
    }

    private fun saveSpinCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("spin_count", spinCount)
            apply()
        }
    }

    override fun onStart() {
        super.onStart()
        // Register the step sensor listener
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onStop() {
        super.onStop()
        // Unregister the sensor listener
        sensorManager.unregisterListener(this)
    }


    private fun updateSteps(count: Int) {
        stepsTextView.text = "Steps: $count / 10 000" // Update the TextView with the current step count
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Enable the location layer on the map to show the user's location
        googleMap.isMyLocationEnabled = true

        // Optional: Set a listener to move the camera to the user's current location when it's available
        googleMap.setOnMyLocationChangeListener { location ->
            val userLocation = LatLng(location.latitude, location.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        }

        fetchNearbyLandmarks()
    }

    private fun fetchNearbyLandmarks() {
        val request = FindCurrentPlaceRequest.newInstance(
            listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES)
        )

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnSuccessListener { response ->
                for (placeLikelihood in response.placeLikelihoods) {
                    val place = placeLikelihood.place
                    if (place.types != null && place.types!!.contains(Place.Type.TOURIST_ATTRACTION)) {
                        // Display each landmark as a marker on the map
                        val markerOptions = MarkerOptions()
                            .position(place.latLng ?: continue)
                            .title(place.name)
                        googleMap.addMarker(markerOptions)
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching landmarks: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, enable location layer
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt() // Total steps since last reboot

            if (stepCount >= 10000)
            {
                spinCount++
                saveSpinCount()
            }

            updateSteps(stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used, but required for SensorEventListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
