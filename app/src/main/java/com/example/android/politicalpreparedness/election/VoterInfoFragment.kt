package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.example.android.politicalpreparedness.network.CivicsApiStatus

class VoterInfoFragment : Fragment() {
    private lateinit var voterInfoViewModel: VoterInfoViewModel
    private lateinit var binding: FragmentVoterInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)

        val dataSource = ElectionDatabase.getInstance(requireContext()).electionDao
        val viewModelFactory = VoterInfoViewModelFactory(dataSource)

        voterInfoViewModel =
            ViewModelProvider(this, viewModelFactory).get(VoterInfoViewModel::class.java)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.voterInfoViewModel = voterInfoViewModel

        // Get Voter Info arguments from bundle
        val election = VoterInfoFragmentArgs.fromBundle(requireArguments())

        binding.electionName.title = election.electionName
        binding.electionDate.text = election.electionDate

        voterInfoViewModel.getVoterInfo(election.argDivision, election.argElectionId)

        voterInfoViewModel.url.observe(viewLifecycleOwner) { url ->
            url?.let {
                loadUrl(url)
                voterInfoViewModel.setUrlToNull()
            }
        }

        voterInfoViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                CivicsApiStatus.LOADING -> binding.voterInfoProgressBar.visibility = View.VISIBLE
                CivicsApiStatus.ERROR -> binding.voterInfoProgressBar.visibility = View.GONE
                CivicsApiStatus.DONE -> binding.voterInfoProgressBar.visibility = View.GONE
            }
        }
        return binding.root
    }

    // Method to load URL intents
    private fun loadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}