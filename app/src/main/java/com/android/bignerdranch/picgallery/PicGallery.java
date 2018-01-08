package com.android.bignerdranch.picgallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.bignerdranch.picgallery.Fragments.PicGalleryFragment;

public class PicGallery extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context,PicGallery.class);
    }

    @Override
    public Fragment createFragment() {
        return PicGalleryFragment.newInstance();
    }
}
