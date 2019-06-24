package ksy.yuhancommunity.com

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import ksy.yuhancommunity.com.model.PushDTO
import okhttp3.*
import java.io.IOException

class FcmPush() {
    var JSON = MediaType.parse("application/json; charset=utf-8")
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverkey = "AAAAzVFIXRA:APA91bEmtFTQuWqpS4VNbN8baIyWGBywOvMYL8kTGMRdFwXdg0Y8pc3xhoS53ClrYjZXPV5nlt1Iz3VqgXJpifsh1sn71EYAPyiltYutjCa-Pggy7VhGJ2K0-WiJMyxX6Ul8qRZp6iWt"
    var okHttpClient: OkHttpClient? = null
    var gson: Gson? = null

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

     fun sendMessage(destinationUid:String, title:String?, message: String?){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid)
            .get().addOnCompleteListener {
                task ->
                if(task.isSuccessful){
                    var token = task.result!!["pushToken"].toString()

                    var pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification?.title = title
                    pushDTO.notification?.body = message

                    var body = RequestBody.create(JSON,gson?.toJson(pushDTO)!!)
                    var request = Request.Builder()
                        .addHeader("Content-Type","application/json")
                        .addHeader("Authorization","key="+serverkey)
                        .url(url)
                        .post(body)
                        .build()
                    okHttpClient?.newCall(request)?.enqueue(object : Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            println(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val res = response.body
                            println(res?.string())
                        }
                    })
                }
            }
    }
}