package com.example.chadrick.datalabeling;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by chadrick on 17. 10. 10.
 */

public class Unzip {

    private static final String TAG="unzip";



    public static boolean unpackZip(File zipfile)
    {
        InputStream filereadstream;
        ZipInputStream zipinputstream;
        try
        {
            String filename;
            String path = zipfile.getParent();
            String zipname = zipfile.getName();

            filereadstream = new FileInputStream(zipfile);
            zipinputstream = new ZipInputStream(new BufferedInputStream(filereadstream));
            ZipEntry zipentry;
            byte[] buffer = new byte[1024];
            int count;

            while ((zipentry = zipinputstream.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = zipentry.getName();


                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (zipentry.isDirectory()) {

                    File dir = new File(path + "/" + filename);
                    Log.d(TAG,"making dir: "+ dir.toString());
                    dir.mkdirs();
                    continue;
                }

                File filetocreate = new File(path +"/"+ filename);
                Log.d(TAG,"creating file: "+ filetocreate.toString());
                FileOutputStream fout = new FileOutputStream(filetocreate);


                // cteni zipu a zapis
                while ((count = zipinputstream.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zipinputstream.closeEntry();
            }

            zipinputstream.close();

            // remove zip file
            zipfile.delete();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
