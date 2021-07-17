package com.example.vin.metron.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vin.metron.*
import com.example.vin.metron.databinding.FragmentHistoryTabBinding
import com.example.vin.metron.home.HomeFragment

class HistoryTabFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(index: Int) =
            HistoryTabFragment().apply {
                arguments = Bundle().apply {
                    putInt("section", HomeFragment.TAB_TITLES[index])
                }
            }
    }

    private lateinit var binding: FragmentHistoryTabBinding
    private lateinit var plnViewModel: PlnViewModel
    private lateinit var pdamViewModel: PdamViewModel
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryTabBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        setRecyclerView(userPreferences)
    }

    private fun setRecyclerView(userPreferences: UserPreferences){
        val user = userPreferences.getUser()
        when(arguments?.get("section")){
            R.string.pln ->{
                binding.historyRV.layoutManager = LinearLayoutManager(requireContext())
                plnViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[PlnViewModel::class.java]
                plnViewModel.getPLNRecords(user?.no_pln).observe(viewLifecycleOwner,{ records ->
                    binding.progressBar.visibility = View.GONE
                    if(records.size == 0){
                        binding.noDataTV.visibility = View.VISIBLE
                    }else{
                        binding.historyRV.adapter = PLNRecordAdapter(records)
                    }
                })
            }
            R.string.pdam -> {
                binding.historyRV.layoutManager = LinearLayoutManager(requireContext())
                pdamViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[PdamViewModel::class.java]
                pdamViewModel.getPDAMRecords(user?.no_pdam).observe(viewLifecycleOwner,{ records ->
                    binding.progressBar.visibility = View.GONE
                    if(records.size == 0){
                        binding.noDataTV.visibility = View.VISIBLE
                    }else{
                        binding.historyRV.adapter = PDAMRecordAdapter(records)
                    }
                })
            }
        }
    }
}