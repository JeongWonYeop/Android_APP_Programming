package kr.ac.kumoh.prof.myrecyclerview
import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class GundamViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val QUEUE_TAG = "VolleyRequest"

        // NOTE: 서버 주소는 본인의 서버 IP 사용할 것
        val SERVER_URL = "http://192.168.0.8:8080"
    }

    private var mQueue: RequestQueue

    data class Gundam (var id: Int, var name: String, var model: String,var image: String)

    val list = MutableLiveData<ArrayList<Gundam>>()
    private val gundam = ArrayList<Gundam>()

    val imageLoader: ImageLoader
    init {
        list.value = gundam
        mQueue = Volley.newRequestQueue(application)

        imageLoader = ImageLoader(mQueue,
            object : ImageLoader.ImageCache {
                private val cache = LruCache<String, Bitmap>(100)
                override fun getBitmap(url: String): Bitmap? {
                    return cache.get(url)
                }
                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            })
    }

    fun getImageUrl(i: Int): String = "$SERVER_URL/image/" + URLEncoder.encode(gundam[i].image, "utf-8")

    fun requestGundam() {
        val request = JsonArrayRequest(
            Request.Method.GET,
            SERVER_URL,
            null,
            {
                //Toast.makeText(getApplication(), it.toString(), Toast.LENGTH_LONG).show()
                gundam.clear()
                parseJson(it)
                list.value = gundam
            },
            {
                Toast.makeText(getApplication(), it.toString(), Toast.LENGTH_LONG).show()
            }
        )

        request.tag = QUEUE_TAG
        mQueue.add(request)
    }

    fun getGundam(i: Int) = gundam[i]

    fun getSize() = gundam.size

    override fun onCleared() {
        super.onCleared()
        mQueue.cancelAll(QUEUE_TAG)
    }

    private fun parseJson(items: JSONArray) {
        for (i in 0 until items.length()) {
            val item: JSONObject = items[i] as JSONObject
            val id = item.getInt("id")
            val name = item.getString("name")
            val model = item.getString("model")
            val image = item.getString("image")

            gundam.add(Gundam(id, name, model, image))
        }
    }
}