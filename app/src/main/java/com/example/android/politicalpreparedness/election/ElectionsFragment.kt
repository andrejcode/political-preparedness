package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.CivicsApiStatus

class ElectionsFragment : Fragment() {
    private lateinit var electionViewModel: ElectionsViewModel
    private lateinit var binding: FragmentElectionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)

        val localDataSource = ElectionDatabase.getInstance(requireContext()).electionDao
        val remoteDataSource = CivicsApi.retrofitService
        val viewModelFactory = ElectionsViewModelFactory(localDataSource, remoteDataSource)

        electionViewModel =
            ViewModelProvider(this, viewModelFactory).get(ElectionsViewModel::class.java)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this
        binding.electionViewModel = electionViewModel

        binding.upcomingElectionsList.adapter = ElectionListAdapter(ElectionListener {
            electionViewModel.displayElectionDetails(it)
        })

        binding.savedElectionsList.adapter = ElectionListAdapter(ElectionListener {
            electionViewModel.displayElectionDetails(it)
        })

        electionViewModel.navigateToSelectedElection.observe(viewLifecycleOwner) { election ->
            election?.let {
                this.findNavController().navigate(
                    ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                        election.name,
                        election.id,
                        election.division,
                        election.electionDay.toString()
                    )
                )
                electionViewModel.displayElectionDetailsComplete()
            }
        }

        electionViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                CivicsApiStatus.LOADING -> binding.upcomingElectionsProgressBar.visibility =
                    View.VISIBLE
                CivicsApiStatus.ERROR -> binding.upcomingElectionsProgressBar.visibility = View.GONE
                CivicsApiStatus.DONE -> binding.upcomingElectionsProgressBar.visibility = View.GONE
            }
        }

        return binding.root
    }
}