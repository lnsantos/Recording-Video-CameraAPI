package com.nepoapp.recordingvideo

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/** A basic Camera preview class */
class CameraPreview(
    context: Context,
    private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        // Instale um SurfaceHolder.Callback para ser notificado quando o
        // superfície subjacente é criada e destruída.
        addCallback(this@CameraPreview)
        // configuração obsoleta, mas necessária nas versões do Android anteriores à 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // A superfície foi criada, agora diga à câmera onde desenhar a visualização.
        mCamera.apply {
            try {
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d("TAG", "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
            mCamera.stopPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // Se sua visualização puder mudar ou girar, cuide desses eventos aqui.
        // Certifique-se de interromper a visualização antes de redimensioná-la ou reformatá-la.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // interrompa a visualização antes de fazer alterações
        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
            // ignorar: tentou parar uma visualização inexistente
        }

        // defina o tamanho da visualização e faça redimensionar, girar ou
        // reformatar as alterações aqui

        // iniciar a visualização com novas configurações
        mCamera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.d("TAG", "Error starting camera preview: ${e.message}")
            }
        }
    }
}