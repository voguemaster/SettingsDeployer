package org.thewiz.kodideployment;

import android.content.Context;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * The downloader file. Takes a String as URL, updates with integer (0-100) and the result is the path of the downloaded file
 */
public class SettingsDownloader extends AsyncTask<String, Integer, Boolean>
{
    /// our progress types, passed in the progress update functions
    public final static int PROGRESS_DOWNLOAD = 1;
    public final static int PROGRESS_EXTRACT = 2;

    /// our context
    Context m_ctx;

    /// log writer
    FileWriter m_logWriter;

    public SettingsDownloader(Context ctx, FileWriter logWriter)
    {
        m_ctx = ctx;
        m_logWriter = logWriter;
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected Boolean doInBackground(String... strParams)
    {
        Boolean result = Boolean.FALSE;

        // download the file
        String filename = downloadFile(strParams[0]);
        if (filename != null)
        {
            File fSettingsZip = new File(filename);
            if (fSettingsZip.exists() && !isCancelled())
            {
                try
                {
                    m_logWriter.write("Successfully downloaded to: "+filename+"\n");

                    // extract to wanted directory
                    String destDir = strParams[1];
                    boolean bSuccess = extractSettingsZip(fSettingsZip, destDir);
                    result = (bSuccess ? Boolean.TRUE : Boolean.FALSE);

                    // delete settings zip
                    if (fSettingsZip.exists()) {
                        fSettingsZip.delete();
                    }
                }
                catch(Exception e)
                {
                    Log.e("SettingsDownloader", "Error: "+e.toString());
                }
            }
        }

        return result;
    }

    /**
     * Updating the UI
     * @param progress
     */
    protected void onProgressUpdate(Integer... progress)
    {
        // lovely cast we don't like. update the activity
        MainActivity activity = (MainActivity)m_ctx;
        activity.onProgressUpdate(progress);
    }

    /**
     * Called in the UI thread after the task finished
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result)
    {
        // @todo: make an interface instead of a cast
        MainActivity activity = (MainActivity)m_ctx;
        activity.DeploymentFinished(result);
    }

    /**
     * called on the UI thread after cancellation and background work has stopped
     */
    protected void onCancelled()
    {
        MainActivity activity = (MainActivity)m_ctx;
        activity.DeploymentFinished(false);
    }

    /**
     * Download the file and save it to our files dir
     * @param strURL URL of file to DL
     * @return Full path tyo the settings ZIP file
     */
    private String downloadFile(String strURL)
    {
        publishProgress(PROGRESS_DOWNLOAD, 0);
        int npos = strURL.lastIndexOf('/');
        if (npos == strURL.length()-1)
        {
            return null;
        }
        String filename = strURL.substring(npos+1);
        try
        {
            m_logWriter.write("Trying URL: "+strURL+", filename: "+filename+"\n");
            m_logWriter.flush();
            URL url = new URL(strURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.connect();

            // setup streams to download and write to file
            String outputFilePath = m_ctx.getFilesDir().getAbsolutePath()+"/"+filename;
            File fSettingsZip = new File(outputFilePath);
            if (fSettingsZip.exists()) {    // todo: do we want this configurable?
                m_logWriter.write("Deleting old file: "+outputFilePath+"\n");
                fSettingsZip.delete();
            }

            int contentLength = conn.getContentLength();

            InputStream is = new BufferedInputStream(url.openStream(), 8192);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFilePath));
            byte data[] = new byte[1024];
            long totalRead = 0;
            int read=0;
            while ((read = is.read(data)) != -1)
            {
                if (isCancelled())
                {
                    outputFilePath = null;
                    break;
                }
                totalRead += read;

                // progress if we know the content length
                if (contentLength > 0) {
                    publishProgress(PROGRESS_DOWNLOAD, (int)(totalRead*100/contentLength));
                }

                // write to file
                os.write(data, 0, read);
            }

            // close and get ready to bail
            os.flush();
            os.close();
            is.close();
            filename = outputFilePath;
        }
        catch(Exception e)
        {
            Log.e("SettingsDownloader", "Error: "+e.toString());
            return null;
        }

        return filename;
    }

    /**
     * Extract the settings zip to the wanted location
     * @param fSettingsZip The zip file
     * @return True if extraction was successful
     */
    private boolean extractSettingsZip(File fSettingsZip, String destDir)
    {
        publishProgress(PROGRESS_EXTRACT, 0);
        boolean result = false;
        try
        {
            m_logWriter.write("Unzipping to destination: "+destDir+"\n");
            m_logWriter.flush();

            // open the zip
            ZipFile zip = new ZipFile(fSettingsZip);
            int count=0;
            int zipSize = zip.size();
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while(entries.hasMoreElements())
            {
                if (isCancelled()) {
                    break;
                }

                // todo: update progress
                ZipEntry ze = (ZipEntry)entries.nextElement();
                count++;
                String entryName = ze.getName();
                String destFullpath = destDir+"/"+entryName;
                m_logWriter.write("Extracting: "+destFullpath+"\n");
                File fDestPath = new File(destFullpath);
                if (ze.isDirectory())
                {
                    fDestPath.mkdirs();
                    publishProgress(PROGRESS_EXTRACT, (count*100/zipSize));
                    continue;
                }
                fDestPath.getParentFile().mkdirs();

                // write file
                try {
                    InputStream is = zip.getInputStream(ze);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFullpath));
                    int n=0;
                    byte buf[] = new byte[4096];
                    while((n = is.read(buf, 0, 4096)) > -1)
                    {
                        bos.write(buf, 0, n);
                    }
                    // close
                    is.close();
                    bos.close();
                } catch(IOException ioe) {
                    m_logWriter.write("Could not write, error: "+ioe.toString());
                }

                // update progress
                publishProgress(PROGRESS_EXTRACT, (count*100/zipSize));
            }

            // close zip and bail
            zip.close();
            m_logWriter.write("Successfully extracted: "+fSettingsZip.getName()+"\n");
            m_logWriter.flush();
            result = !isCancelled();
        }
        catch(Exception e)
        {
            Log.e("SettingsDownloader", "Error: "+e.toString());
            result = false;
        }

        return result;
    }

}


