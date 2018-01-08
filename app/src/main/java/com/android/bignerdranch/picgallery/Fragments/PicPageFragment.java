package com.android.bignerdranch.picgallery.Fragments;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.android.bignerdranch.picgallery.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicPageFragment extends Fragment {

    private static final String URL = "pic_url";
    private Uri mUrl;

    public PicPageFragment() {
        // Required empty public constructor
    }

    public static PicPageFragment newInstance(Uri url) {
        PicPageFragment fragment = new PicPageFragment();
        Bundle args = new Bundle();
        args.putParcelable(URL,url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = (Uri) getArguments().get(URL);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pic_page, container, false);
        final ProgressBar progressBar = v.findViewById(R.id.webProgress);
        progressBar.setMax(100);
        WebView webView = v.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView webView,int newProgress){
                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });
        webView.loadUrl(mUrl.toString());

        return v;
    }

}
