package br.com.projeto.iniciacaocientifica;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


public class MainActivity extends AppCompatActivity {
    ImageView imageViewFoto;

    private MediaPlayer soundClear = null;
    private MediaPlayer soundNotClear = null;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Employee retorno = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundClear = MediaPlayer.create(MainActivity.this, R.raw.clear);
        soundNotClear = MediaPlayer.create(MainActivity.this, R.raw.notclear);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA }, 0);
        }

        imageViewFoto = findViewById(R.id.imageView);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                tirarFoto();
            }
        });
    }

    public void tirarFoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        someActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();

                //Preparando imagem para ser enviada
                Bundle extras =  data.getExtras();
                Bitmap imagem = (Bitmap) extras.get("data");
                imageViewFoto.setImageBitmap(imagem);

                //Preparando Requisição para os servidor flask
                OkHttpClient client = new OkHttpClient();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                try {
                    imagem.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                }catch(Exception e){
                    return;
                }
                byte[] byteArray = stream.toByteArray();

                RequestBody postBodyImage = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("files[]", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray)).build();

                Request request = new Request.Builder().url("http://192.168.0.105:5000/upload").post(postBodyImage).build();

                //Enviando requisição e recuperando a resposta
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                soundNotClear.start();
                                System.out.println(e);
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        retorno = gson.fromJson(response.body().string(), Employee.class);

                        if (Integer.parseInt(retorno.getResult()) == 1){
                            soundClear.start();
                        } else {
                            soundNotClear.start();
                        }
                    }
                });
            }
        }
    });
}
