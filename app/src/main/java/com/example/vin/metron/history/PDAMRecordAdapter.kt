package com.example.vin.metron.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vin.metron.R
import com.example.vin.metron.databinding.ItemRecordBinding
import com.example.vin.metron.entities.PDAMRecord

class PDAMRecordAdapter(private val records: ArrayList<PDAMRecord>): RecyclerView.Adapter<PDAMRecordAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount() = records.size

    class ViewHolder(private val binding: ItemRecordBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(record: PDAMRecord){
            binding.apply {
                noPlnTV.text = "No.${record.no_pdam}"
                usageTV.text = "${record.usage}m3"
                timeStartTV.text =
                    "Awal pengunaan: ${record.time_start?.toDate()}"
                timeEndTV.text =
                    "Akhir pengunaan: ${record.time_end?.toDate()}"
                Glide.with(root.context)
                    .load(R.drawable.logo_pdam)
                    .apply(RequestOptions().override(85, 85))
                    .into(logoIV)
            }
        }
    }
}