package vp.metagram.ui.MainMenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import vp.metagram.R;
import vp.metagram.base.BaseActivity;

import static vp.metagram.general.functions.configShimmer;
import static vp.metagram.general.functions.setTextViewFontForMessage;
import static vp.metagram.general.variables.appVersion;

public class LegalActivity extends BaseActivity
{

    ShimmerFrameLayout menuExternalShimmer;
    TextView menuExternalShimmerTextView;
    ShimmerFrameLayout menuInternalShimmer;
    TextView menuInternalShimmerTextView;

    TextView terms;
    TextView privacy;
    TextView licences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal);

        prepareUIElements();
    }

    private void prepareUIElements()
    {
        menuExternalShimmer = findViewById(R.id.shimmer_view_container_external);
        menuInternalShimmer = findViewById(R.id.shimmer_view_container_internal);
        menuExternalShimmerTextView = findViewById(R.id.shimmer_text_exteral);
        menuInternalShimmerTextView = findViewById(R.id.shimmer_text_internal);

        String shimmerText = "";
        if (appVersion.languagePartNo == 1)
            shimmerText = appVersion.get_appName_en();
        else if (appVersion.languagePartNo == 2)
            shimmerText = appVersion.get_appName_fa();

        configShimmer(this,
                shimmerText,
                menuExternalShimmer,
                menuInternalShimmer,
                menuExternalShimmerTextView,
                menuInternalShimmerTextView,
                2000);


        terms = findViewById(R.id.legalActivity_TermsButton);
        privacy = findViewById(R.id.legalActivity_PrivacyButton);
        licences = findViewById(R.id.legalActivity_LicencesButton);

        setTextViewFontForMessage(this, terms);
        setTextViewFontForMessage(this, privacy);
        setTextViewFontForMessage(this, licences);

        licences.setOnClickListener((View v)->
        {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.LegalActivity_Licences));
        });
    }
}
