package com.example.vin.metron.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.vin.metron.*
import com.example.vin.metron.databinding.FragmentResultBinding
import com.example.vin.metron.entities.PDAMRecord
import com.example.vin.metron.entities.PLNRecord
import com.example.vin.metron.profile.AlarmReceiver
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ResultFragment : Fragment() {
    private lateinit var binding: FragmentResultBinding
    private val alarmReceiver: AlarmReceiver = AlarmReceiver()
    private val resultViewModel: ResultViewModel by viewModels()
    private lateinit var userPreferences: UserPreferences
    private lateinit var plnViewModel: PlnViewModel
    private lateinit var pdamViewModel: PdamViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(layoutInflater)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        plnViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.NewInstanceFactory()
        )[PlnViewModel::class.java]
        pdamViewModel = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.NewInstanceFactory()
        )[PdamViewModel::class.java]
        userPreferences = UserPreferences(requireContext())

        setUIContent()
        binding.btnBack.setOnClickListener{
            when(arguments?.getString(SERVICE_TYPE) == resources.getString(R.string.pln)){
                true -> {
                    findNavController().navigate(R.id.action_resultFragment_to_plnMainFragment)
                }
                false -> {
                    findNavController().navigate(R.id.action_resultFragment_to_pdamMainFragment)
                }
            }

        }
    }

    private fun setUIContent() {
        binding.apply {
            ivResultFail.visibility = View.GONE
            ivResultSucess.visibility = View.GONE
            btnBack.visibility = View.GONE
            tvDesc.text = ""
            tvUsage.text = ""
        }

        val isPLN = arguments?.getString(SERVICE_TYPE) == resources.getString(R.string.pln)
        val isFake = arguments?.getBoolean(IS_FAKE, true) ?: false
        val numberRead = arguments?.getDouble(OCR_READING)
        val confidence = arguments?.getDouble(CONFIDENCE)

        val type = if (isPLN) "listrik" else "air"
        val unit = if (isPLN) "kW/h" else "meter kubik"
        try {
            if (confidence != null) {
                if (isFake && confidence>75.00) {
                    saveToDatabase(numberRead, isPLN)
                    scheduleAlarm(isPLN = isPLN)
                    binding.apply {
                        ivResultSucess.visibility = View.VISIBLE
                        btnBack.visibility = View.VISIBLE
                        tvDesc.text = "Data penggunaan $type berhasil tersimpan"
                        tvUsage.text = "$numberRead $unit"
                        btnBack.text = getString(R.string.kembali)
                    }
                } else{
                    binding.btnBack.text = getString(R.string.coba_lagi)
                    throw Exception("Gagal, Gunakan foto yang asli meteran anda yang jelas")
                }
            }
        } catch (e: Exception) {
            binding.apply {
                ivResultFail.visibility = View.VISIBLE
                tvDesc.text = getString(R.string.simpan_gagal)
                btnBack.visibility = View.VISIBLE
                btnBack.text = getString(R.string.coba_lagi)
            }
            showToast("ERROR: ${e.message}")
        }
    }

    private fun scheduleAlarm(isPLN: Boolean) {
        val schedule = resultViewModel.getUpdatedOrNewlyCreatedAlarmSchedule(
            context = requireContext(),
            isPLN = isPLN
        )
        if(schedule != null) {
            alarmReceiver.setAlarm(context = requireContext(), schedule = schedule, isPLN = isPLN)
        }
    }

    private fun saveToDatabase(numberRead: Double?, isPLN: Boolean) {
        val user = userPreferences.getUser()
        val db = FirebaseFirestore.getInstance()
        when (isPLN) {
            true -> {
                plnViewModel.getPreviousRecord(user?.no_pln)
                    .observe(viewLifecycleOwner) { recordResult ->
                        var record: PLNRecord? = null
                        if(recordResult!=null) {
                            record = PLNRecord(
                                user?.no_pln,
                                recordResult.time_end,
                                Timestamp.now(),
                                numberRead,
                                (numberRead!! - recordResult.number_read!!)
                            )
                        }else{
                            record = PLNRecord(
                                user?.no_pln,
                                Timestamp.now(),
                                Timestamp.now(),
                                numberRead,
                                numberRead
                            )
                        }
                        db.collection("records_pln").add(record)
                    }
            }

            false -> {
                pdamViewModel.getPreviousRecord(user?.no_pdam)
                    .observe(viewLifecycleOwner) { recordResult ->
                        var record: PDAMRecord? = null
                        if(recordResult!=null) {
                            record = PDAMRecord(
                                user?.no_pdam,
                                recordResult.time_end,
                                Timestamp.now(),
                                numberRead,
                                (numberRead!! - recordResult.number_read!!)
                            )
                        }else{
                            record = PDAMRecord(
                                user?.no_pdam,
                                Timestamp.now(),
                                Timestamp.now(),
                                numberRead,
                                numberRead
                            )
                        }
                        db.collection("records_pdam").add(record!!)
                    }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}