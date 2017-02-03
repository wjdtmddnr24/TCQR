package org.dyndns.wjdtmddnr24.tcqr.model;

import android.net.Uri;

import com.google.zxing.Result;

import org.dyndns.wjdtmddnr24.tcqr.Util.CompressUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import lombok.Data;

/**
 * Created by jack on 2017-01-30.
 */

@Data
public class QRCode implements Serializable {
    private Result result;
    private String text;
    private String compressedText;
    private boolean isCompressed = false;
    private File image;
    public String filename = "";

    public QRCode() {
    }

    public QRCode(Result result) throws IOException {
        this.result = result;
        if (CompressUtils.isMarkerAdded(result.getText())) {
            compressedText = result.getText();
            isCompressed = true;
            String rm = CompressUtils.removeMarker(compressedText);
            text = CompressUtils.decompress(rm);
        } else {
            text = result.getText();
            isCompressed = false;
        }
    }

}
