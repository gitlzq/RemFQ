package ki.zq.remfq.ocr

import com.google.gson.Gson
import ki.zq.remfq.bean.GeneralFPBean
import ki.zq.remfq.util.BaseUtil
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

/**
 * http 工具类
 */
object HttpUtil {
    private val okHttpClient = OkHttpClient().newBuilder().build()

    @Throws(Exception::class)
    fun post(requestUrl: String, accessToken: String, params: String): String? {
        val contentType = "application/x-www-form-urlencoded"
        return post(requestUrl, accessToken, contentType, params)
    }

    @Throws(Exception::class)
    fun post(
        requestUrl: String,
        accessToken: String,
        contentType: String,
        params: String
    ): String? {
        var encoding = "UTF-8"
        if (requestUrl.contains("nlp")) {
            encoding = "GBK"
        }
        return post(requestUrl, accessToken, contentType, params, encoding)
    }

    @Throws(Exception::class)
    fun post(
        requestUrl: String,
        accessToken: String,
        contentType: String,
        params: String,
        encoding: String
    ): String? {
        val url = "$requestUrl?access_token=$accessToken"
        return postGeneralUrl(url, contentType, params)
    }

    @Throws(Exception::class)
    fun postGeneralUrl(
        generalUrl: String,
        contentType: String,
        params: String
    ): String? {
        val mediaType: MediaType? = "application/json".toMediaTypeOrNull()
        val body: RequestBody = params.toRequestBody(mediaType)
        val request: Request = Request.Builder()
            .url(generalUrl)
            .method("POST", body)
            .addHeader("Content-Type", contentType)
            .addHeader("Accept", "application/json")
            .addHeader("Connection", "Keep-Alive")
            .build()
        val response = okHttpClient.newCall(request).execute()
        return response.body?.string()
    }

    fun multipleInvoice(imgStr:String): String? {
        // 请求url
        val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice"
        try {
            // 本地文件路径
            val contentType = "application/x-www-form-urlencoded"
            //val filePath = "[本地文件路径]"
            //val imgData: ByteArray = FileUtil.readFileByBytes(filePath)
            //val imgStr: String = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")
            println("imgParam -> "+imgParam)
            val param = "image=$imgParam"
            BaseUtil.getToken()?.also { token ->
                val result: String? = post(url, token, contentType, param)
                println(result)
                return result
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(Exception::class)
    fun getAccessToken(): Boolean {
        var getAccessTokenFlag = false
        val result = postGeneralUrl(
            "https://aip.baidubce.com/oauth/2.0/token?client_id=Sa9ROXIGhGzayDzWX9KjAw1x&client_secret=Fg5izt2hMm1iffeSFmdjgZwLFxzu9GMi&grant_type=client_credentials",
            "application/json",
            ""
        )
        result?.also {
            getAccessTokenFlag = if (it.contains("error")) {
                false
            } else {
                val tokenInfo = Gson().fromJson(result, GeneralFPBean::class.java)
                BaseUtil.saveToken(tokenInfo.access_token)
                true
            }
        }
        return getAccessTokenFlag
    }
}