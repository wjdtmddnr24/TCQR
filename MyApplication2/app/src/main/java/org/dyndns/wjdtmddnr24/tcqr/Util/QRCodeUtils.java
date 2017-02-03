package org.dyndns.wjdtmddnr24.tcqr.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import org.dyndns.wjdtmddnr24.tcqr.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by 정 on 2017-01-26.
 */

public class QRCodeUtils {
    public static Bitmap EncodeToQRCode(int width, int height, String value) throws WriterException, UnsupportedEncodingException, FormatException, ChecksumException, NotFoundException {
        MultiFormatWriter writer = new MultiFormatWriter();
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        value = new String(value.getBytes("UTF-8"), "ISO-8859-1");
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
        BitMatrix result = writer.encode(value, BarcodeFormat.QR_CODE, width, height, hints);
        int h = result.getHeight(), w = result.getWidth();
        int[] pixels = new int[h * w];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }

    public static String DecodeToString(Bitmap bitmap) throws FormatException, ChecksumException, NotFoundException, UnsupportedEncodingException {
        HashMap<DecodeHintType, String> hint = new HashMap<DecodeHintType, String>();
        hint.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hint.put(DecodeHintType.TRY_HARDER, String.valueOf(Boolean.TRUE));
        hint.put(DecodeHintType.PURE_BARCODE, String.valueOf(Boolean.FALSE));

        int width = bitmap.getWidth(), height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
        QRCodeReader reader = new QRCodeReader();
        Result result = reader.decode(binaryBitmap, hint);
        return new String(result.getText().getBytes("ISO-8859-1"), "UTF-8");
    }

    public static Result DecodeToResult(Bitmap bitmap) throws FormatException, ChecksumException, NotFoundException, UnsupportedEncodingException {
        HashMap<DecodeHintType, String> hint = new HashMap<DecodeHintType, String>();
//        hint.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hint.put(DecodeHintType.TRY_HARDER, String.valueOf(Boolean.TRUE));
        hint.put(DecodeHintType.PURE_BARCODE, String.valueOf(Boolean.FALSE));

        int width = bitmap.getWidth(), height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
        QRCodeReader reader = new QRCodeReader();
        return reader.decode(binaryBitmap, hint);
    }


    public static void saveQRCode(Context context, Bitmap bitmap, String path) throws FileNotFoundException {
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        Date date = new Date();
        String file_name = dateFormat.format(date) + ".png";
        String fullpath = ex_storage + path + file_name;
        File file = new File(ex_storage + path, file_name);
        File dir = new File(ex_storage + path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        Toast.makeText(context, context.getString(R.string.saved_into) + fullpath, Toast.LENGTH_SHORT).show();
        MediaScannerConnection.scanFile(context, new String[]{fullpath}, null, null);
    }
}

