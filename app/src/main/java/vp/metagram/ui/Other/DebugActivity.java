package vp.metagram.ui.Other;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.tools.io.iDBMetagram;
import vp.tools.io.iFileSystemUtils;

import static vp.metagram.general.variables.DBVersion;
import static vp.metagram.general.variables.appContext;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.mainDatabaseName;
import static vp.metagram.general.variables.metagramAgent;

public class DebugActivity extends BaseActivity
{
    Button ExportDB;
    Button ImportDB;
    Button ClientStat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);

        ExportDB = findViewById(R.id.debug_ExportDB);
        ImportDB = findViewById(R.id.debug_ImportDB);
        ClientStat = findViewById(R.id.debug_ClientStatistics);

        ExportDB.setOnClickListener((View v)->
        {
            try
            {
                dbMetagram.close();
                dbMetagram.copyDatabase(iFileSystemUtils.GetReportExportDir(this)+"metagram.db");
                dbMetagram = new iDBMetagram(appContext, mainDatabaseName, DBVersion);
                Toast.makeText(this, "Done!",
                        Toast.LENGTH_LONG).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        ImportDB.setOnClickListener((View v)->
        {
            try
            {
                String destination = dbMetagram.getDatabasePath();
                String source = iFileSystemUtils.GetReportImportDir(this)+"metagram.db";
                dbMetagram.close();
                dbMetagram.removeDatabase();
                dbMetagram = null;
                iFileSystemUtils.copy(source, destination);

                dbMetagram = new iDBMetagram(appContext, mainDatabaseName, DBVersion);

                Toast.makeText(this, "Done!",
                        Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            catch (GeneralSecurityException e)
            {
                e.printStackTrace();
            }

        });

        ClientStat.setOnClickListener((View v)->
        {
            metagramAgent.activeAgent.agentStatus.delete(metagramAgent.activeAgent.userID);

            String a = null;
            if (a.equals(""))
            {
                a = "";
            }
        });
    }
}
