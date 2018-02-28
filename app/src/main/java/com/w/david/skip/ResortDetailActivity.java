package com.w.david.skip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.w.david.skip.objects.Resort;

/**
 * Created by whcda on 2/28/2018.
 */

public class ResortDetailActivity extends AppCompatActivity {
    private static final String LOGTAG = "RESORT_DETAIL_ACTIVITY";
    Resort mResort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_resort_detail);

        mResort = (Resort)getIntent().getSerializableExtra("MyResort");
        Log.d(LOGTAG,mResort.getUrl());
        TextView url = findViewById(R.id.textView);
        url.setText(mResort.getUrl());
    }

}
