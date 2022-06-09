package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApiService
import com.example.android.politicalpreparedness.network.CivicsApiStatus
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

// Construct ViewModel and provide election datasource
class ElectionsViewModel(
    private val database: ElectionDao,
    private val apiService: CivicsApiService
) : ViewModel() {
    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<CivicsApiStatus>()
    // The external immutable LiveData for the request status
    val status: LiveData<CivicsApiStatus>
        get() = _status

    // MutableLiveData for upcoming elections
    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    val savedElections = database.getElections()

    // MutableLiveData to handle navigation to the selected election
    private val _navigateToSelectedElection = MutableLiveData<Election?>()
    val navigateToSelectedElection: LiveData<Election?>
        get() = _navigateToSelectedElection

    init {
        getUpcomingElections()
    }

    // Function that gets upcoming elections from the API
    private fun getUpcomingElections() {
        viewModelScope.launch {
            try {
                _status.value = CivicsApiStatus.LOADING
                val response = apiService.getElections()
                _upcomingElections.value = response.elections
            } catch (e: Exception) {
                _status.value = CivicsApiStatus.ERROR
            } finally {
                _status.value = CivicsApiStatus.DONE
            }
        }
    }

    /**
     * When election is clicked, set the [_navigateToSelectedElection] to that election
     */
    fun displayElectionDetails(election: Election) {
        _navigateToSelectedElection.value = election
    }

    // After the navigation has taken place, make sure navigateToSelectedElection is set to null
    fun displayElectionDetailsComplete() {
        _navigateToSelectedElection.value = null
    }
}