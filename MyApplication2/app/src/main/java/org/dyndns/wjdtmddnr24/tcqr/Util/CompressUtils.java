package org.dyndns.wjdtmddnr24.tcqr.Util;

import android.util.Base64;

import org.apache.http.util.ByteArrayBuffer;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by 정 on 2017-01-26.
 */

public class CompressUtils {
    public static String compress(String text) throws IOException {
        byte[] original = text.getBytes("UTF-8");
        LZMA2Options options = new LZMA2Options();
        ByteArrayInputStream input = new ByteArrayInputStream(original);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        options.setPreset(6);
        XZOutputStream out = new XZOutputStream(output, options);
        byte[] buffer = new byte[10];
        int size, progress = 0;
        while ((size = input.read(buffer)) != -1) {
            out.write(buffer, 0, size);
        }
        out.finish();

        String encoded = new String(Base64.encode(output.toByteArray(), 0), "ISO-8859-1");
        return encoded;
    }

    public static String decompress(String text) throws IOException ,IllegalArgumentException{
        ByteArrayInputStream input = new ByteArrayInputStream(Base64.decode(text.getBytes("ISO-8859-1"), 0));
        XZInputStream xzInput = new XZInputStream(input);
        int read;
        ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(6);
        byte[] buffer = new byte[3000];
        while ((read = xzInput.read(buffer)) != -1) {
            byteArrayBuffer.append(buffer, 0, read);
        }
        return new String(byteArrayBuffer.toByteArray(), "UTF-8");

    }

    public static String UTF8TOISO(String text) throws UnsupportedEncodingException {
        return new String(text.getBytes(), "ISO-8859-1");
    }

    public static String ISOTOUTF8(String text) throws UnsupportedEncodingException {
        return new String(text.getBytes("ISO-8859-1"), "UTF-8");
    }

    public static String addMarker(String compressed) throws UnsupportedEncodingException {
        return "TCQREncoded:" + (char) 0x04 + new String(compressed.getBytes(), "ISO-8859-1");
    }

    public static String removeMarker(String content) {
        return content.substring(("TCQREncoded:" + (char) 0x04).length());
    }

    public static boolean isMarkerAdded(String text) {
        return text.length() > ("TCQREncoded:" + (char) 0x04).length() && text.substring(0, ("TCQREncoded:" + (char) 0x04).length()).equals(("TCQREncoded:" + (char) 0x04));
    }
}
