package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.CivicsApiStatus
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class DetailFragment : Fragment() {
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private lateinit var representativeViewModel: RepresentativeViewModel
    private lateinit var binding: FragmentRepresentativeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        representativeViewModel = ViewModelProvider(this).get(RepresentativeViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.representativeViewModel = representativeViewModel
        binding.representativeList.adapter = RepresentativeListAdapter()

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            representativeViewModel.getRepresentativesList()
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            if (checkLocationPermissions()) {
                getLocation()
            }
        }

        // Observe status LiveData and show loading ProgressBar accordingly
        representativeViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                CivicsApiStatus.LOADING -> binding.representativesProgressBar.visibility =
                    View.VISIBLE
                CivicsApiStatus.ERROR -> binding.representativesProgressBar.visibility = View.GONE
                CivicsApiStatus.DONE -> binding.representativesProgressBar.visibility = View.GONE
            }
        }

        /**
         * Show Snackbar whenever snackbar LiveData is updated a non-null value
         */
        representativeViewModel.snackbar.observe(viewLifecycleOwner) { text ->
            text?.let {
                // Show error message
                Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
                representativeViewModel.onSnackbarShown()
            }
        }

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            getLocation()
        } else {
            // Show error message when permission is denied
            Snackbar.make(
                requireView(),
                getString(R.string.permission_denied),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        // Check if permission is already granted and return (true = granted, false = denied/other)
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val address = geoCodeLocation(location)
                    representativeViewModel.setAddress(address)
                    representativeViewModel.getRepresentativesList()
                }
            }
            .addOnFailureListener {
                Snackbar.make(
                    requireView(),
                    getString(R.string.unable_to_get_location),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}