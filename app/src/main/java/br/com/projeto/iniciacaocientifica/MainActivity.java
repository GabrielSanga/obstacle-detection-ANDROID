package br.com.projeto.iniciacaocientifica;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{

//region Variables Global
    
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private FrameLayout mFrameLayout;

    private MediaPlayer mSoundClear = null;
    private MediaPlayer mSoundNotClear = null;

    private Gson mGson = new GsonBuilder().setPrettyPrinting().create();

    private Employee mRetorno = null;

    private OkHttpClient mClient = new OkHttpClient();

    private static final String URL = "http://192.168.0.102:5000/predict";

//endregion

//region Start Application

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mSoundClear = MediaPlayer.create(MainActivity.this, R.raw.clear);;
        mSoundNotClear = MediaPlayer.create(MainActivity.this, R.raw.notclear);

        setContentView(R.layout.activity_main);

        try {
            /**Verifica se o dispositivo tem camera*/
            if (!checkCameraHardware(this)){
                throw new Exception("Dispositvo não possúi câmera! Não é possível utilizar o APP.");
            }else{
                /** Caso tenha câmera, verifica se o aplicativo possui permissão para utiliza-la */
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, 0);
                }
            }

            /** Criar uma instância de Camera */
            mCamera = getCameraInstance();

            /** Crie visualização e defina-a como o conteúdo de nossa atividade.*/
            mCameraPreview = new CameraPreview(MainActivity.this, mCamera);

            mFrameLayout = findViewById(R.id.camera_preview);
            mFrameLayout.addView(mCameraPreview);

            findViewById(R.id.button_capture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    mCamera.takePicture(null, null, mPicture);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//endregion

//region Functions

    /** Verifica se este dispositivo tem uma câmera */
    private boolean checkCameraHardware(@NonNull Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

//endregion

//region Methods

    /** Obtendo uma instância do objeto Camera */
    public static Camera getCameraInstance(){
        Camera cam = null;
        try {
            cam = Camera.open();

            //cam.setDisplayOrientation(90);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return cam;
    }

    /** Capturando o frame */
    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            SendRequest(data);
        }
    };

    /** Método responsável pelo envio da requisição para a API */
    private void SendRequest(byte[] image){
        try {
            //Preparando Requisição para os servidor flask
            RequestBody postBodyImage = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), image)).build();

            Request request = new Request.Builder().url(URL).post(postBodyImage).build();

            //Enviando requisição e recuperando a resposta
            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSoundNotClear.start();

                            mCamera.takePicture(null, null, mPicture);
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    mRetorno = mGson.fromJson(response.body().string(), Employee.class);

                    if (Integer.parseInt(mRetorno.getResult()) == 1) {
                        mSoundClear.start();
                    } else {
                        mSoundNotClear.start();
                    }

                    mCamera.takePicture(null, null, mPicture);
                }
            });

            reloadCamera();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Limpando a visualização para o proximo frame */
    private void reloadCamera(){
        mFrameLayout.removeView(mCameraPreview);
        mFrameLayout.addView(mCameraPreview);
    }

//endregion

}
