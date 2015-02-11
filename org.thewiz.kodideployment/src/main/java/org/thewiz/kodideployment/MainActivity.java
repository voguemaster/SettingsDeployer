package org.thewiz.kodideployment;

import android.app.Activity;
import android.graphics.Color;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends Activity {

    /// our kodi specific stuff
    KodiEnvironment m_kodiEnv;

    // label state
    int m_progressType=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // detect XBMC/Kodi
        m_kodiEnv = new KodiEnvironment(this);
        m_kodiEnv.DetectEnvironment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // close the log and cleanup
        m_kodiEnv.Cleanup(false);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // progress setup
        ProgressBar pb = (ProgressBar)findViewById(R.id.work_prog);
        pb.setMax(100);
        pb.setIndeterminate(false);
        pb.setVisibility(View.INVISIBLE);

        // set the text based on whats detected
        TextView foundTextView =  (TextView)findViewById(R.id.found_text);
        if (m_kodiEnv.isInstalled())
        {
            if (m_kodiEnv.getSetupType() == KodiEnvironment.KodiSetupType.XBMC) {
                foundTextView.setText(R.string.found_xbmc);
            } else if (m_kodiEnv.getSetupType() == KodiEnvironment.KodiSetupType.KODI) {
                foundTextView.setText(R.string.found_kodi);
            }

            // start deployment
            startDeployment();
        }
        else
        {
            foundTextView.setText(R.string.found_nothing);
        }
    }

    /**
     * cancel our downloading.
     */
    public void onBackPressed()
    {
        // will tell the async task to exit.
        m_kodiEnv.CancelDeployment();

        // exit now
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start the async task that will download and extract the settings zip. tie the operations to the progress bar
     */
    private void startDeployment()
    {
        // show the progress bar
        ProgressBar progress = (ProgressBar)findViewById(R.id.work_prog);
        progress.setMax(100);
        progress.setProgress(0);
        progress.setVisibility(View.VISIBLE);
        m_kodiEnv.DeploySettingsFile();
    }

    /**
     * First int is the type (download/extract). second is the progress itself.
     * @param progress A tuple of ints
     */
    public void onProgressUpdate(Integer... progress)
    {
        // based on the type of work, set the label text (only when changes otherwise we have lots of layout calcs.)
        int progressType = progress[0];
        if (progressType != m_progressType)
        {
            TextView workTypeTV = (TextView)findViewById(R.id.work_type);
            if (progressType == SettingsDownloader.PROGRESS_DOWNLOAD) {
                workTypeTV.setText(R.string.work_type_downloading);
            } else if (progressType == SettingsDownloader.PROGRESS_EXTRACT) {
                workTypeTV.setText(R.string.work_type_extracting);
            }
            m_progressType = progressType;
        }

        // update progress
        ProgressBar pb = (ProgressBar)findViewById(R.id.work_prog);
        pb.setProgress(progress[1]);
    }

    /**
     * Called on the UI thread when everything finishes
     * @param bSuccess True if deployment succeeded
     */
    public void DeploymentFinished(boolean bSuccess)
    {
        // set result label based on if we succeeded
        TextView resultTextView = (TextView)findViewById(R.id.result_text);
        if (bSuccess)
        {
            resultTextView.setText(R.string.deploy_success);
            resultTextView.setTextColor(Color.parseColor("#ff77ff79"));
        }
        else
        {
            resultTextView.setText(R.string.deploy_fail);
            resultTextView.setTextColor(Color.RED);
        }

        // clear status of progress bar
        ProgressBar progress = (ProgressBar)findViewById(R.id.work_prog);
        progress.setProgress(0);
        progress.setVisibility(View.INVISIBLE);
        m_progressType = 0;
        TextView workTypeTV = (TextView)findViewById(R.id.work_type);
        workTypeTV.setText("");

        // cleanup log, etc.
        m_kodiEnv.Cleanup(bSuccess);
    }
}
