package com.example.szakdoga;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TicketUtil {

    public static File getTicketFile(Context context, String purchaseId) {
        return new File(context.getFilesDir(), purchaseId + ".pdf");
    }

    public static void downloadTicket(Context context, String purchaseId) {
        // Ez csak egy minta. Itt jöhetne SMB kapcsolat, vagy Retrofit letöltés stb.

        try {
            // Példa: PDF másolása assets-ből (teszteléshez)
            InputStream inputStream = context.getAssets().open("placeholder.pdf"); // legyen egy PDF az assets-ben
            File outFile = getTicketFile(context, purchaseId);
            FileOutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
