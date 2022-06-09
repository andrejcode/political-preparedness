package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiStatus
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {
    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address>
        get() = _address

    private val _status = MutableLiveData<CivicsApiStatus>()
    val status: LiveData<CivicsApiStatus>
        get() = _status

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    init {
        _address.value = Address("", null, "", "", "")
    }

    fun getRepresentativesList() {
        viewModelScope.launch {
            // Set representatives value to empty list
            _representatives.value = arrayListOf()
            if (_address.value != null && _address.value!!.line1.isNotBlank() &&
                _address.value!!.city.isNotBlank() && _address.value!!.zip.isNotBlank()
            ) {
                try {
                    _status.value = CivicsApiStatus.LOADING
                    val formattedAddress = _address.value?.toFormattedString()!!
                    val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(
                        formattedAddress
                    )
                    _representatives.value =
                        offices.flatMap { office -> office.getRepresentatives(officials) }
                } catch (e: Exception) {
                    _status.value = CivicsApiStatus.ERROR
                    _snackbar.value = "Error: ${e.message}"
                } finally {
                    _status.value = CivicsApiStatus.DONE
                }
            } else {
                _snackbar.value = "Please enter all necessary fields"
            }
        }
    }

    /**
     * Called immediately after the UI shows the snackbar.
     */
    fun onSnackbarShown() {
        _snackbar.value = null
    }

    fun setAddress(address: Address) {
        _address.value = address
    }
}