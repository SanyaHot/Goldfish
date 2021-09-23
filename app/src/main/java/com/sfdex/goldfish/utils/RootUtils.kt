package com.sfdex.goldfish.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import kotlin.concurrent.thread

private const val TAG = "RootUtils"

//Root工具类
object RootUtils {
    //屏幕截图
    fun screenshot(block: () -> Unit) {
        thread {
            "ScreenShot" log "截图"
            val path = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                "/sdcard/Pictures/Screenshot_${System.currentTimeMillis()}.png"
            else
                gContext.cacheDir.absolutePath + "/Screenshot_${System.currentTimeMillis()}.png"
            try {
                val ret = execCmd("screencap -p $path")
                if (ret != 0) {
                    "截图失败".toast()
                } else {
                    //通知相册更新
                    notifyGallery(File(path))
                }
                block.invoke()
            } catch (e: Exception) {
                "ScreenShot" log "截图功能异常：${e.message}"
                e.printStackTrace()
//                "截图功能异常：${e.message}".toast()
            }
        }
    }

    fun shutdown(context: Context) {
        //方式一
        execCmd("reboot -p")

        //方式二(系统应用)
        /*val intent = Intent(Intent.ACTION_SHUTDOWN)
        context.sendBroadcast(intent)*/

        //方式三(系统应用)
        /*val intent = Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        //其中false换成true,会弹出是否关机的确认窗口
        context.startActivity(intent);*/

    }

    fun reboot(context: Context) {
        execCmd("reboot")
    }

    //执行命令
    private fun execCmd(cmd: String): Int {
        var result = -1
        var dataOutputStream: DataOutputStream? = null
        try {
            val process = Runtime.getRuntime().exec("su")
            dataOutputStream = DataOutputStream(process.outputStream).apply {
                writeBytes(
                    """
                $cmd
                
                """.trimIndent()
                )
                flush()
                writeBytes("exit\n")
                flush()
            }

            process.waitFor()
            result = process.exitValue()
        } catch (e: Exception) {
            "执行命令错误${e.message}".toast()
            e.printStackTrace()
        } finally {
            try {
                dataOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }

    //更新相册
    fun notifyGallery(file: File) {
        val resolver = gContext.contentResolver
        val values = getImagesContentValues(file, System.currentTimeMillis())
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            //Android11以下直接通知相册刷新
            MediaStore.Images.Media.insertImage(
                gContext.contentResolver,
                BitmapFactory.decodeFile(file.absolutePath),
                file.name,
                null
            )
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val uri = Uri.fromFile(file)

            intent.data = uri
            gContext.sendBroadcast(intent)
        } else {
            //Android11手动写入相册,然后更新
            val mediaUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val localUri = resolver.insert(mediaUri, values)
            localUri?.let {
                resolver.openOutputStream(localUri)?.use { outputStream ->
//                    val sink = outputStream.sink().buffer()
//                    sink.writeAll(videoFile.source().buffer())
//                    sink.flush()

                    val byteArray = file.readBytes()
                    println("Hello ByteArray")
                    println(byteArray)
                    outputStream.write(byteArray, 0, byteArray.size)
                    outputStream.flush()
                    outputStream.close()
                }

                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(localUri, values, null, null)
                values.clear()
            }
        }

    }

    fun getImagesContentValues(paramFile: File, currentTime: Long): ContentValues {
        val values = ContentValues()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, currentTime)
            values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                //content://media/external_primary/images/media; allowed directories are [DCIM, Pictures]
                Environment.DIRECTORY_PICTURES
            )
            values.put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            values.put(MediaStore.Images.Media.DATA, paramFile.absolutePath)
        }
        values.put(MediaStore.Images.Media.TITLE, paramFile.name)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, paramFile.name)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.SIZE, paramFile.length())
        values.put(MediaStore.Images.Media.DATE_ADDED, currentTime)
        values.put(MediaStore.Images.Media.DATE_MODIFIED, currentTime)
        return values
    }

}