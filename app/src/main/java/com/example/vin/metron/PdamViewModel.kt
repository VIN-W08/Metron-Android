package com.example.vin.metron

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vin.metron.data.remote.network.ApiConfig
import com.example.vin.metron.data.remote.network.ApiService
import com.example.vin.metron.data.remote.network.ResponseIsFake
import com.example.vin.metron.entities.PDAMRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import okhttp3.MultipartBody
import retrofit2.Callback
import retrofit2.Response

class PdamViewModel: ViewModel() {
    private val apiService: ApiService = ApiConfig.provideApiService()

    fun checkIsFakeFromURI(image: MultipartBody.Part, context: Context): LiveData<ResponseIsFake> {
        val result: MutableLiveData<ResponseIsFake> = MutableLiveData<ResponseIsFake>()
        val client = apiService.checkIsFakeFromURI(image)
        client.enqueue(object : Callback<ResponseIsFake> {
            override fun onResponse(
                call: retrofit2.Call<ResponseIsFake>,
                response: Response<ResponseIsFake>
            ) {
                result.value = response.body()
            }

            override fun onFailure(call: retrofit2.Call<ResponseIsFake>, t: Throwable) {
                Toast.makeText(context,"Error Api", Toast.LENGTH_SHORT).show()
            }
        })
        return result
    }

    fun getPDAMRecords(noPdam: String?): LiveData<ArrayList<PDAMRecord>>{
        val recordsResult = MutableLiveData<ArrayList<PDAMRecord>>()
        val db = FirebaseFirestore.getInstance()
        db.collection("records_pdam")
            .whereEqualTo("no_pdam", noPdam)
            .orderBy("time_end", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { qs ->
                val records = ArrayList<PDAMRecord>()
                if(qs.documents.size != 0){
                    for(document in qs.documents){
                        val record = PDAMRecord(
                            document.get("no_pdam").toString(),
                            document.get("time_start") as Timestamp,
                            document.get("time_end") as Timestamp,
                            document.get("number_read").toString().toDouble(),
                            document.get("usage").toString().toDouble()
                        )
                        records.add(record)
                    }
                }
                recordsResult.value = records
            }

        return recordsResult
    }

    fun getPreviousRecord(noPdam: String?): LiveData<PDAMRecord?> {
        val pdamResult = MutableLiveData<PDAMRecord?>()
        val db = FirebaseFirestore.getInstance()
        db.collection("records_pdam")
            .whereEqualTo("no_pdam", noPdam)
            .orderBy("time_end", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnSuccessListener {
                if(it.documents.size == 0){
                    pdamResult.value = null
                } else{
                    pdamResult.value = PDAMRecord(
                        it.documents[0].get("no_pdam").toString(),
                        it.documents[0].get("time_start") as Timestamp,
                        it.documents[0].get("time_end") as Timestamp,
                        it.documents[0].get("number_read") as Double,
                        it.documents[0].get("usage") as Double
                    )
                }
            }

        return pdamResult
    }
}