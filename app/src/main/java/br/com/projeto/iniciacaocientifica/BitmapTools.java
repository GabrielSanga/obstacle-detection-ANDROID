package br.com.projeto.iniciacaocientifica;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

public class BitmapTools {

    public static Bitmap toBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data , 0, data.length);
    }

    public static byte[] toBytes(Bitmap in) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }

    public static  Bitmap redimension(Bitmap in, int width, int height){
        return Bitmap.createScaledBitmap(in, width, height, true);
    }

}
