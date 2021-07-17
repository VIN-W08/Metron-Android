package com.example.vin.metron

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vin.metron.data.remote.network.ApiConfig
import com.example.vin.metron.data.remote.network.ApiService
import com.example.vin.metron.data.remote.network.ResponseIsFake
import com.example.vin.metron.entities.PLNRecord
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import okhttp3.MultipartBody
import retrofit2.Callback
import retrofit2.Response

class PlnViewModel: ViewModel() {

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

    fun getPLNRecords(noPln: String?): LiveData<ArrayList<PLNRecord>>{
        val recordsResult = MutableLiveData<ArrayList<PLNRecord>>()
        val db = FirebaseFirestore.getInstance()
        db.collection("records_pln")
            .whereEqualTo("no_pln", noPln)
            .orderBy("time_end", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { qs ->
                val records = ArrayList<PLNRecord>()
                if(qs.documents.size != 0){
                    for(document in qs.documents){
                        val record = PLNRecord(
                            document.get("no_pln").toString(),
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

    fun getPreviousRecord(noPln: String?): LiveData<PLNRecord?> {
        val plnResult = MutableLiveData<PLNRecord?>()
        val db = FirebaseFirestore.getInstance()
        db.collection("records_pln")
            .whereEqualTo("no_pln", noPln)
            .orderBy("time_end", Query.Direction.DESCENDING).limit(1)
            .get()
            .addOnSuccessListener {
                if(it.documents.size == 0){
                    plnResult.value = null
                } else{
                    plnResult.value = PLNRecord(
                        it.documents[0].get("no_pln").toString(),
                        it.documents[0].get("time_start") as Timestamp,
                        it.documents[0].get("time_end") as Timestamp,
                        it.documents[0].get("number_read") as Double,
                        it.documents[0].get("usage") as Double
                    )
                }
            }

        return plnResult
    }
}