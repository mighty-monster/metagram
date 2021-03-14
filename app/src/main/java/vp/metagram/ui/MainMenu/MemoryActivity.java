package vp.metagram.ui.MainMenu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.Dialogs.ConfirmationDialog;
import vp.metagram.ui.Dialogs.ConnectingDialog;

import static vp.metagram.general.functions.setTextViewFontForMenu;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.dbMetagram;
import static vp.metagram.general.variables.logger;
import static vp.metagram.general.variables.threadPoolExecutor;
import static vp.tools.io.iFileSystemUtils.GetDownloadDir;
import static vp.tools.io.iFileSystemUtils.deleteDir;
import static vp.tools.io.iFileSystemUtils.getDirectorySize;


public class MemoryActivity extends BaseActivity
{

    TextView title;

    TextView mainDBTitle;
    Button mainDBShrink;
    TextView mainDBBanner;

    TextView logDBTitle;
    Button logDBShrink;
    TextView logDBBanner;

    TextView downloadDirTitle;
    Button downloadDirClear;
    TextView downloadDirBanner;

    TextView cacheDirTitle;
    Button cacheDirClear;
    TextView cacheDirBanner;

    ConnectingDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        prepareUIElements();
        dialog = ConnectingDialog.newInstance(getString(R.string.addOrder_UpgradeDialog));
    }

    @Override
    public void onResume()
    {
        updateUIElements();
        super.onResume();
    }

    private void prepareUIElements()
    {
        title = findViewById(R.id.memory_title);
        setTextViewFontForMenu(this, title);

        mainDBTitle = findViewById(R.id.memory_mainDatabaseTitle);
        mainDBShrink = findViewById(R.id.memory_mainDatabaseShrink);
        mainDBBanner = findViewById(R.id.memory_mainDatabaseBanner);
        setTextViewFontForMessage(this, mainDBTitle);
        setTextViewFontForMessage(this, mainDBShrink);
        setTextViewFontForMessage(this, mainDBBanner);

        mainDBShrink.setOnClickListener((View v)->
        {
            dialog.show(getFragmentManager(), "");
            threadPoolExecutor.execute(()->
            {
                try
                {
                    dbMetagram.shrinkDatabase();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    runOnUiThread(()->
                    {
                        updateUIElements();
                        dialog.close();
                    });
                }
            });
        });

        logDBTitle = findViewById(R.id.memory_logDatabaseTitle);
        logDBShrink = findViewById(R.id.memory_logDatabaseShrink);
        logDBBanner = findViewById(R.id.memory_logDatabaseBanner);
        setTextViewFontForMessage(this, logDBTitle);
        setTextViewFontForMessage(this, logDBShrink);
        setTextViewFontForMessage(this, logDBBanner);

        logDBShrink.setOnClickListener((View v)->
        {
            dialog.show(getFragmentManager(), "");
            threadPoolExecutor.execute(()->
            {
                try
                {
                    logger.logDatabase.shrinkDatabase();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    runOnUiThread(()->
                    {
                        updateUIElements();
                        dialog.close();
                    });
                }
            });
        });

        downloadDirTitle = findViewById(R.id.memory_downloadDirTitle);
        downloadDirClear = findViewById(R.id.memory_downloadDirClear);
        downloadDirBanner = findViewById(R.id.memory_downloadDirBanner);
        setTextViewFontForMessage(this, downloadDirTitle);
        setTextViewFontForMessage(this, downloadDirClear);
        setTextViewFontForMessage(this, downloadDirBanner);

        downloadDirClear.setOnClickListener((View v)->
        {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog();
            confirmationDialog.showDialog(this,
                    getString(R.string.memoryActivity_clearingWarningTitle),
                    getString(R.string.memoryActivity_downloadDirWarning),
                    getString(R.string.button_confirmCaption), () ->
                    {
                        dialog.show(getFragmentManager(), "");
                        threadPoolExecutor.execute(() ->
                        {
                            try
                            {
                                deleteDir(GetDownloadDir(this));
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                runOnUiThread(()->
                                {
                                    updateUIElements();
                                    dialog.close();
                                });
                            }
                        });
                    });
        });

        cacheDirTitle = findViewById(R.id.memory_cacheDirTitle);
        cacheDirClear = findViewById(R.id.memory_cacheDirClear);
        cacheDirBanner = findViewById(R.id.memory_cacheDirBanner);
        setTextViewFontForMessage(this, cacheDirTitle);
        setTextViewFontForMessage(this, cacheDirClear);
        setTextViewFontForMessage(this, cacheDirBanner);

        cacheDirClear.setOnClickListener((View v)->
        {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog();
            confirmationDialog.showDialog(this,
                    getString(R.string.memoryActivity_clearingWarningTitle),
                    getString(R.string.memoryActivity_cacheDirWarning),
                    getString(R.string.button_confirmCaption), () ->
                    {
                        dialog.show(getFragmentManager(), "");
                        threadPoolExecutor.execute(() ->
                        {
                            try
                            {
                                deleteDir(getCacheDir().getAbsolutePath());
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                runOnUiThread(()->
                                {
                                    updateUIElements();
                                    dialog.close();
                                });
                            }
                        });
                    });
        });
    }

    private void updateUIElements()
    {

        mainDBTitle.setText(String.format(getResources().getString(R.string.memoryActivity_mainDatabase),
                android.text.format.Formatter.formatFileSize(this, dbMetagram.getDatabaseSize())));

        logDBTitle.setText(String.format(getResources().getString(R.string.memoryActivity_logDatabase),
                android.text.format.Formatter.formatFileSize(this, logger.logDatabase.getDatabaseSize())));

        downloadDirTitle.setText(String.format(getResources().getString(R.string.memoryActivity_downloadDir),
                android.text.format.Formatter.formatFileSize(this, getDirectorySize(new File(GetDownloadDir(this))))));

        cacheDirTitle.setText(String.format(getResources().getString(R.string.memoryActivity_cacheDir),
                android.text.format.Formatter.formatFileSize(this, getDirectorySize(getCacheDir()))));
    }
}
