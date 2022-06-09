package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiStatus
import com.example.android.politicalpreparedness.network.models.AdministrationBody
import com.example.android.politicalpreparedness.network.models.Division
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(private val dataSource: ElectionDao) : ViewModel() {
    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<CivicsApiStatus>()
    val status: LiveData<CivicsApiStatus>
        get() = _status

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _administrationBody = MutableLiveData<AdministrationBody>()
    val administrationBody: LiveData<AdministrationBody>
        get() = _administrationBody

    private val _url = MutableLiveData<String?>()
    val url: LiveData<String?>
        get() = _url

    private val _isElectionFollowed = MutableLiveData<Boolean>()
    val isElectionFollowed: LiveData<Boolean>
        get() = _isElectionFollowed

    fun getVoterInfo(division: Division, electionId: Int) {
        viewModelScope.launch {
            try {
                _status.value = CivicsApiStatus.LOADING
                val address = "${division.state}/${division.country}"
                val response = CivicsApi.retrofitService.getVoterInfo(address, electionId)

                _isElectionFollowed.value =
                    dataSource.getElectionWithId(electionId) == response.election

                _voterInfo.value = response
                _administrationBody.value = response.state?.first()?.electionAdministrationBody
            } catch (e: Exception) {
                _status.value = CivicsApiStatus.ERROR
            } finally {
                _status.value = CivicsApiStatus.DONE
            }
        }
    }

    fun setUrl(url: String) {
        _url.value = url
    }

    fun setUrlToNull() {
        _url.value = null
    }

    fun followOrUnfollowElection() {
        viewModelScope.launch {
            _voterInfo.value?.election?.let {
                if (_isElectionFollowed.value == true) {
                    dataSource.deleteElection(it)
                    _isElectionFollowed.value = false
                } else {
                    dataSource.addElection(it)
                    _isElectionFollowed.value = true
                }
            }
        }
    }
}