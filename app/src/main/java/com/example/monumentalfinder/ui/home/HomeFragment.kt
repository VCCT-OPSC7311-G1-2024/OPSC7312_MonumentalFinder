package com.example.monumentalfinder.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.monumentalfinder.R
import com.example.monumentalfinder.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private lateinit var stepsTextView: TextView
    private var stepCount: Int = 0 // Initialize step count

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the TextView for steps
        stepsTextView = binding.stepsTextView

        // Update step count (example logic; replace with actual step tracking logic)
        updateSteps(stepCount)

        return root
    }

    private fun updateSteps(count: Int) {
        stepsTextView.text = "Steps: $count" // Update the TextView with the current step count
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

        // Enable the location layer on the map
        googleMap.isMyLocationEnabled = true



        // Set a default location (e.g., Sydney, Australia) or use the device's location
        val defaultLocation = LatLng(-34.0, 151.0) // Replace with your desired location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))

        // Add a marker for the user's location (if desired)
        googleMap.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
