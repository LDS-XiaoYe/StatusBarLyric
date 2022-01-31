package statusbar.lyric.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import statusbar.lyric.BuildConfig
import statusbar.lyric.R
import statusbar.lyric.config.Config
import statusbar.lyric.utils.HttpUtils.Get
import statusbar.lyric.view.miuiview.MIUIDialog
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess


object ActivityUtils {
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    // 弹出toast
    @JvmStatic
    fun showToastOnLooper(context: Context?, message: String?) {
        try {
            handler.post { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    //清除配置
    @JvmStatic
    fun cleanConfig(activity: Activity) {
        for (name in arrayOf("Lyric_Config", "AppList_Config", "Icon_Config")) {
            Utils.getSP(activity, name)?.let { Config(it) }?.clear()
        }
        showToastOnLooper(activity, activity.getString(R.string.ResetSuccess))
        exitProcess(0)
    }

    //检查更新
    fun checkUpdate(activity: Activity) {
        val handler = Handler(Looper.getMainLooper()) { message: Message ->
            val data: String = message.data.getString("value") as String
            try {
                val jsonObject = JSONObject(data)
                if (jsonObject.getString("tag_name").split("v").toTypedArray()[1].toInt() > BuildConfig.VERSION_CODE) {
                    MIUIDialog(activity).apply {
                        setTitle(
                            String.format(
                                "%s [%s]",
                                activity.getString(R.string.NewVer),
                                jsonObject.getString("name")
                            )
                        )
                        setMessage(jsonObject.getString("body").replace("#", ""))
                        setButton(R.string.Update) {
                            try {
                                val uri: Uri = Uri.parse(
                                    jsonObject.getJSONArray("assets").getJSONObject(0)
                                        .getString("browser_download_url")
                                )
                                val intent = Intent(
                                    Intent.ACTION_VIEW, uri
                                )
                                activity.startActivity(intent)
                            } catch (e: JSONException) {
                                showToastOnLooper(activity, activity.getString(R.string.GetNewVerError) + e)
                            }
                            dismiss()
                        }
                        setCancelButton(R.string.Cancel) { dismiss() }
                        show()
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.NoVerUpdate), Toast.LENGTH_LONG).show()
                }
            } catch (ignored: JSONException) {
                showToastOnLooper(activity, activity.getString(R.string.CheckUpdateError))
            }

            true
        }
        Thread {
            val value: String =
                HttpUtils.Get("https://api.github.com/repos/577fkj/StatusBarLyric/releases/latest")
            if (value != "") {
                handler.obtainMessage().let {
                    it.data = Bundle().apply {
                        putString("value", value)
                    }
                    handler.sendMessage(it)
                }
            } else {
                showToastOnLooper(activity, activity.getString(R.string.CheckUpdateFailed))
            }
        }.start()
    }

    fun isApi(packageManager: PackageManager, packName: String?): Boolean {
        return try {
            val appInfo = packageManager.getApplicationInfo(packName!!, PackageManager.GET_META_DATA)
            if (appInfo.metaData != null) {
                appInfo.metaData.getBoolean("XStatusBarLyric", false)
            } else {
                false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun dp2px(dpValue: Float): Int {
        return (0.5f + dpValue * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun openUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun getNotice(activity: Activity) {
        val handler = Handler(Looper.getMainLooper()) { message: Message ->
            try {
                val jsonObject = JSONObject(message.data.getString("value")!!)
                if (jsonObject.getString("versionCode") == BuildConfig.VERSION_CODE.toString()) {
                    if (java.lang.Boolean.parseBoolean(jsonObject.getString("forcibly"))) {
                        MIUIDialog(activity).apply {
                            setTitle(activity.getString(R.string.NewNotice))
                            setMessage(jsonObject.getString("data"))
                            setButton(activity.getString(R.string.Done)) { dismiss() }
                            show()
                        }
                    }
                }
                return@Handler true
            } catch (ignored: JSONException) {
            }
            showToastOnLooper(activity, activity.getString(R.string.GetNewNoticeError))
            false
        }
        Thread {
            val value = Get("https://app.xiaowine.cc/app/notice.json")
            if (value != "") {
                val message = handler.obtainMessage()
                val bundle = Bundle()
                bundle.putString("value", value)
                message.data = bundle
                handler.sendMessage(message)
            }
        }.start()
    }
}