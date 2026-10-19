package com.purohitcommunity.portal.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {
  public byte[] png(String value){
    try{
      var matrix=new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 320, 320);
      var out=new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix,"PNG",out);
      return out.toByteArray();
    }catch(Exception e){ throw new IllegalStateException("Unable to generate QR code",e); }
  }
}
