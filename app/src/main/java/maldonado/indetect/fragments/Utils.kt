package maldonado.indetect.fragments

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SingletonNetwork(ctx: Context) {
    private var queue: RequestQueue = Volley.newRequestQueue(ctx)
    private var url: String = "http://192.168.196.213:8080/api/v1.0/"

    companion object {
        @Volatile private var INSTANCE: SingletonNetwork? = null

        fun getInstance(context: Context): SingletonNetwork =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildNetwork(context).also { INSTANCE = it }
            }

        private fun buildNetwork(context: Context) : SingletonNetwork {
            return SingletonNetwork(context)
        }

    }

    fun sendPostRequest(obj: JSONObject, endp: String){
        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, this.url + endp, obj,
            Response.Listener {
                Log.i("Json", it.toString())
            },
            Response.ErrorListener {
            })

        // Add Json Object Request to Queue
        queue.add(jsonRequest)
    }
}
