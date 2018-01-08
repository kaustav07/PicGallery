package com.android.bignerdranch.picgallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.bignerdranch.picgallery.Fragments.PicPageFragment;

public class PicPageActivity extends SingleFragmentActivity {

    public static Intent newInstance(Context context,Uri url){
        Intent intent = new Intent(context,PicPageActivity.class);
        intent.setData(url);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return PicPageFragment.newInstance(getIntent().getData());
    }
}
