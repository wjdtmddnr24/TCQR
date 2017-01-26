package org.dyndns.wjdtmddnr24.tcqr.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by 정 on 2017-01-26.
 */

public class QRCodeUtils {
    public static Bitmap EncodeToQRCode(int width, int height, String value) throws WriterException, UnsupportedEncodingException, FormatException, ChecksumException, NotFoundException {
        QRCodeWriter writer = new QRCodeWriter();
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        value = new String(value.getBytes("UTF-8"), "ISO-8859-1");
        hints.put(EncodeHintType.CHARACTER_SET, "ISO-8859-1");
        BitMatrix result = writer.encode(value, BarcodeFormat.QR_CODE, width, height, hints);
        Bitmap bitmap = Bitmap.createBitmap(result.getWidth(), result.getHeight(), Bitmap.Config.ARGB_8888);
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                bitmap.setPixel(i, j, result.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
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
}
