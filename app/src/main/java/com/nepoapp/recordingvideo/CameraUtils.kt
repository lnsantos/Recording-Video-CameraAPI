package com.nepoapp.recordingvideo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraUtils{

      fun checkCameraHardware(context: Context): Boolean =
         context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)


      fun getOutputMediaFile(type: Int) : File?{
         val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
         val folderName = "solvianteste"
         val mediaStorage = File(path,folderName)

         if (!mediaStorage.exists()){
             if (!mediaStorage.mkdirs()){
                 return null
             }
         }

         val time = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
         val pathFile = mediaStorage.path + File.separator + time
         return if (type == 0){
             File("$pathFile.jpg")
         } else File("$pathFile.mp4")
     }
}