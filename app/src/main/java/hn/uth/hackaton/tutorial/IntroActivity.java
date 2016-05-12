package hn.uth.hackaton.tutorial;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import hn.uth.hackaton.R;
import me.relex.circleindicator.CircleIndicator;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_layout);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);

        // Set an Adapter on the ViewPager
        assert mViewPager != null;
        mViewPager.setAdapter(new IntroAdapter(getSupportFragmentManager()));

        // Set a PageTransformer
        mViewPager.setPageTransformer(false, new IntroPageTransformer());

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        assert indicator != null;
        indicator.setViewPager(mViewPager);
    }

}