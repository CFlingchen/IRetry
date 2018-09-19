package com.lingchen.iretrydemo;

import android.view.View;

public class MainActivity extends BaseActivity {


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }


    public void tokenTest(View view) {
        goActivity(TestTokenActivity.class);
    }

    public void netTest(View view) {
        goActivity(TestNetActivity.class);
    }

}
