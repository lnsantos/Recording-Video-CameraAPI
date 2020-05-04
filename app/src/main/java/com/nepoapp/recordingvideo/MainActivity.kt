package com.nepoapp.recordingvideo

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mediaRecord: MediaRecorder? = null
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    private var isRecording = false

    companion object{
        const val CAMERA_BACK = 0

        const val TYPE_PHOTO = 0
        const val TYPE_VIDEO = 1
    }

    private var typeCurrent = TYPE_VIDEO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCamera = getCameraInstance()

        mPreview = mCamera?.let {
            CameraPreview(this,it)
        }

        mPreview?.also {previewCamera ->
            camera_preview.addView(previewCamera)
        }

        button_capture.setOnClickListener {

            if (typeCurrent == TYPE_PHOTO){
                mCamera?.takePicture(null,null,mPicture)
            }else{
                startRecording()
            }
        }
        
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
    }

    private fun startRecording(){

        if (isRecording){
            mediaRecord?.stop()
            releaseMediaRecorder()
            mCamera?.lock()

            button_capture.background = resources.getDrawable(R.drawable.background_not_record)
            isRecording = false
        }else{

            if (prepareVideoRecorder()){

                mediaRecord?.start()
                button_capture.background = resources.getDrawable(R.drawable.background_record)
                isRecording = true
            }else{
                releaseMediaRecorder()
            }

        }

    }
    private fun getCameraInstance(): Camera? {
        return try {
            val camera = Camera.open() // attempt to get a Camera instance

            camera
        } catch (e: Exception) {
            Toast.makeText(this,"Não existe uma câmera",Toast.LENGTH_LONG).show()
            null
        }
    }

    // interface para recuperar uma imagem
    private val mPicture = Camera.PictureCallback{ data, _ ->

        val pictureFile : File = CameraUtils().getOutputMediaFile(typeCurrent) ?: run {
            Log.d("TAG", ("Error creating media file, check storage permissions"))
            return@PictureCallback
        }

        try {
            val fileOutput = FileOutputStream(pictureFile)
            fileOutput.write(data)
            fileOutput.close()

            Toast.makeText(this,pictureFile.path,Toast.LENGTH_LONG).show()
            mCamera?.release()
        }catch (e: FileNotFoundException) {
            Log.d("TAG", "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d("TAG", "Error accessing file: ${e.message}")
        }

    }

    private fun prepareVideoRecorder(): Boolean{
        mediaRecord = MediaRecorder()

        mCamera?.let { cam ->

            cam?.unlock()

            mediaRecord?.run {
                setCamera(cam)

                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)

                /*setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW))*/
                setOutputFile(CameraUtils().getOutputMediaFile(typeCurrent).toString())

                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.VideoEncoder.DEFAULT)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)

                setPreviewDisplay(mPreview?.holder?.surface)

                setVideoSize(640,480)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(1000)
                return try {
                    prepare()
                    true
                }catch (e: IllegalStateException) {
                    Log.d("TAG", "IllegalStateException preparing MediaRecorder: ${e.message}")
                    releaseMediaRecorder()
                    false
                } catch (e: IOException) {
                    Log.d("TAG", "IOException preparing MediaRecorder: ${e.message}")
                    releaseMediaRecorder()
                    false
                }
            }

        }
        return false
    }

    private fun releaseMediaRecorder() {
        mediaRecord?.reset()
        mediaRecord?.release()
        mediaRecord = null
        mCamera?.lock()
    }

    private fun releaseCamera() {
        mCamera?.release()
        mCamera = null
    }

}
