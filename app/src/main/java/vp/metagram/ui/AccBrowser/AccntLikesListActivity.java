package vp.metagram.ui.AccBrowser;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;


import vp.metagram.R;
import vp.metagram.base.BaseActivity;
import vp.metagram.ui.AccBrowser.ListSource.ListSource;
import vp.metagram.ui.AccBrowser.ListSource.ListSource_Likes_Online;
import vp.metagram.utils.instagram.types.User;

import static vp.metagram.general.functions.setTextViewFontForMessage;


public class AccntLikesListActivity extends BaseActivity
{
    Map<Long, User> userList = new LinkedHashMap<>(10, 0.75F, true);

    ListSource likerListSource;

    ImageButton backButton;
    TextView titleTextView;

    Fragment accntListFragment;

    String short_code;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accnt_like_list);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {finish();}

        short_code = bundle.getString("short_code");


        prepareUIElements();
    }

    public void prepareUIElements()
    {
        backButton = findViewById(R.id.ReportSelector_backButton);
        backButton.setOnClickListener((View v)->{onBackPressed();});

        titleTextView = findViewById(R.id.ReportSelector_titleTextView);
        setTextViewFontForMessage(this,titleTextView);

        likerListSource = new ListSource_Likes_Online(short_code, userList);

        accntListFragment = AccntListFragment.newInstance(likerListSource);

        getSupportFragmentManager().beginTransaction().add(R.id.AccntList_frameLayout, accntListFragment).commitAllowingStateLoss();
    }
}
