package br.com.projeto.iniciacaocientifica;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.Method;

/** classe básica de visualização da câmera */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    /** CONSTRUTOR */

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        /** Instale um SurfaceHolder.Callback para ser notificado quando a superfície subjacente for criada e destruída. */
        mHolder = getHolder();
        mHolder.addCallback(this);

        /** Configuração obsoleta, mas necessária em versões do Android anteriores a 3.0 */
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /** OVERRIDES */

    public void surfaceCreated(SurfaceHolder holder) {
        /** A superfície foi criada, agora diga à câmera onde desenhar a visualização. */
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        /** Cuidado ao liberar a visualização da câmera . */
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        /** Se sua visualização puder mudar ou girar, cuide desses eventos aqui. */
        /** Certifique-se de parar a visualização antes de redimensioná-la ou reformatá-la. */

        if (mHolder.getSurface() == null){
            return;
        }
    }

}