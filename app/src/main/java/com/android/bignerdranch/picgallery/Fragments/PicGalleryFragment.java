package com.android.bignerdranch.picgallery.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.bignerdranch.picgallery.Model.PhotoModel;
import com.android.bignerdranch.picgallery.Model.PhotoRequestResult;
import com.android.bignerdranch.picgallery.Network.FlickrFetcher;
import com.android.bignerdranch.picgallery.Network.Thumbnaildownloader;
import com.android.bignerdranch.picgallery.PicPageActivity;
import com.android.bignerdranch.picgallery.Preferance.QueryPrefarance;
import com.android.bignerdranch.picgallery.R;
import com.android.bignerdranch.picgallery.Services.PollService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.android.bignerdranch.picgallery.Network.FlickrFetcher.TAG;

public class PicGalleryFragment extends VisibleFragment {

    List<PhotoModel> mPhotoModels = new ArrayList<>();
    RecyclerView recyclerView;
    TextView currentPageView;
    FetcherTask mFetcherTask;
    int maxPage = 0;
    int mItemsPerPage = 0;
    int mTotalItemCount = 0;
    int mCurrentPage = 1;
    Boolean amLoading = false;
    GridLayoutManager mGridLayoutManager;
    Thumbnaildownloader<PhotoHolder> mThumbnaildownloader;
    ProgressBar mProgressBar;
    public Boolean GPS_OK = true;
    GoogleApiClient apiClient;


    public PicGalleryFragment() {
        // Required empty public constructor
    }

    public static PicGalleryFragment newInstance() {
        PicGalleryFragment fragment = new PicGalleryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        Handler handler = new Handler();
        mThumbnaildownloader = new Thumbnaildownloader<>("myImageDownloader", handler);
        mThumbnaildownloader.start();
        mThumbnaildownloader.getLooper();
        mThumbnaildownloader.setThumbnailDownloadListener(new Thumbnaildownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                // mLruCache.put(target.getAdapterPosition(),drawable);
                Log.d("cacheadd", "adding in position - " + target.getAdapterPosition() + "layout position - " + target.getPosition());
                target.bindPic(drawable);
            }
        });
        apiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_items, menu);

        MenuItem item = menu.findItem(R.id.geosearch);
        if (!GPS_OK)
            item.setEnabled(false);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        MenuItem switchPolling = menu.findItem(R.id.app_bar_switch);
        View view = switchPolling.getActionView();
        final Switch aSwitch = (Switch) view.findViewById(R.id.my_polling_switch);
        if (QueryPrefarance.getAlarmStatus(getActivity()) != null)
            aSwitch.setChecked(Boolean.valueOf(QueryPrefarance.getAlarmStatus(getActivity())));
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                QueryPrefarance.setAlarmStatus(isChecked, getActivity());
                PollService.setServiceAlarm(getActivity(), isChecked);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recyclerView.removeAllViewsInLayout();
                clearPagingParameters();
                mProgressBar.setVisibility(View.VISIBLE);
                QueryPrefarance.setQueryInPrefarance(query, getActivity());
                //updateItems();
                updateItemsRetro();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPrefarance.getQueryFromPreference(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem switchPolling = menu.findItem(R.id.app_bar_switch);
        View view = switchPolling.getActionView();
        final Switch aSwitch = (Switch) view.findViewById(R.id.my_polling_switch);
        if (QueryPrefarance.getAlarmStatus(getActivity()) != null)
            aSwitch.setChecked(Boolean.valueOf(QueryPrefarance.getAlarmStatus(getActivity())));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geosearch:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                   searchGeoPhotos();
                }
                break;
        }

        return true;
    }

    private void searchGeoPhotos(){
        final LocationRequest request = new LocationRequest();
        request.setNumUpdates(1);
        request.setInterval(0);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},103);
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    try {
                        Call<PhotoRequestResult> call = new FlickrFetcher().getGeoSearchItemsFromRetrofit(mCurrentPage,null,location);
                        call.enqueue(new Callback<PhotoRequestResult>() {
                            @Override
                            public void onResponse(Call<PhotoRequestResult> call, Response<PhotoRequestResult> response) {
                                postExecuteCall(response.body());
                            }

                            @Override
                            public void onFailure(Call<PhotoRequestResult> call, Throwable t) {

                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 103){
            searchGeoPhotos();
        }
    }

    private void clearPagingParameters() {
        maxPage = 0;
        mTotalItemCount = 0;
        mItemsPerPage = 0;
        mCurrentPage = 1;
        mPhotoModels.clear();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pic_gallery, container, false);
        //currentPageView = (TextView) v.findViewById(R.id.currentPage);
        recyclerView = v.findViewById(R.id.piclist);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mGridLayoutManager);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        initializeScrollListener();
        setAdapter();
        //updateItems();
        updateItemsRetro();
        return v;
    }

    private void updateItems() {
        String query = QueryPrefarance.getQueryFromPreference(getActivity());
        mFetcherTask = new FetcherTask(query);
        mFetcherTask.execute();
    }

    private void updateItemsRetro() {
        String query = QueryPrefarance.getQueryFromPreference(getActivity());
        Call<PhotoRequestResult> call;
        try {
            if (query != null)
                call = new FlickrFetcher().getSearchItemsFromRetrofit(mCurrentPage, query);
            else
                call = new FlickrFetcher().getRecentItemsFromRetrofit(mCurrentPage);

            call.enqueue(new Callback<PhotoRequestResult>() {
                @Override
                public void onResponse(Call<PhotoRequestResult> call, Response<PhotoRequestResult> response) {
                    postExecuteCall(response.body());
                }

                @Override
                public void onFailure(Call<PhotoRequestResult> call, Throwable t) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postExecuteCall(PhotoRequestResult photoModelsResult) {
        mProgressBar.setVisibility(View.GONE);
        if (photoModelsResult == null) {
            mPhotoModels = new ArrayList<>();
            setAdapter();
        } else {
            QueryPrefarance.setLastResultId(photoModelsResult.getResults().get(0).getId(), getActivity());
            if (mPhotoModels.size() == 0) {
                mPhotoModels.addAll(photoModelsResult.getResults());
                maxPage = photoModelsResult.getPageCount();
                mItemsPerPage = photoModelsResult.getItemsPerPage();
                mTotalItemCount = photoModelsResult.getItemCount();
                setAdapter();
                setCurrentPageView();
            } else {
                final int oldSize = mPhotoModels.size();
                mPhotoModels.addAll(photoModelsResult.getResults());
                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        // Scroll to first row of newly added set
                        recyclerView.smoothScrollToPosition(oldSize);
                        setCurrentPageView();
                        amLoading = false;
                    }
                });
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void initializeScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0) {
                    // Scrolling up or down
                    if (!(amLoading) &&             // Not already processing a page fetch
                            (dy > 0) &&                 // Scrolling down
                            (mCurrentPage < maxPage) &&   // Haven't hit the bottom yet
                            mGridLayoutManager.findLastVisibleItemPosition() >= (mPhotoModels.size() - 1)) {
                        // We scrolled to the last row of the previously fetched set
                        //
                        // Go fetch more.
                        Log.d(TAG, "Fetching more items");
                        amLoading = true;
                        mCurrentPage++;
                        //updateItems(); // Also updates current page view
                        updateItemsRetro();
                    } else {
                        // Make sure our page value is correct
                        int firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
                        int calcPage = 0;
                        if (firstVisibleItem < mItemsPerPage) {
                            calcPage = 1;
                        } else {
                            calcPage = (firstVisibleItem / mItemsPerPage) +
                                    (firstVisibleItem % mItemsPerPage == 0 ? 0 : 1);
                        }
                        if (calcPage != mCurrentPage) {
                            mCurrentPage = calcPage;
                        }
                        setCurrentPageView(firstVisibleItem);
                    }
                }
            }
        });
    }

    private void setCurrentPageView() {
        setCurrentPageView(-1);
    }

    @SuppressLint("SetTextI18n")
    private void setCurrentPageView(int firstVisibleItem) {
        if (firstVisibleItem == -1) {
            firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();
        }
        /*currentPageView.setText("Current Fetched Page: " + mCurrentPage +
                " of " + ((maxPage==0) ? "<unknown>": maxPage) +
                ", " + ((mItemsPerPage==0)?"<unknown>":mItemsPerPage) + " items per page" +
                ", " + ((mTotalItemCount==0)?"<unknown>":mTotalItemCount) + " total items" +
                ", you've scrolled past: " + (firstVisibleItem <= 0 ? 0: firstVisibleItem) +
                " items.");*/
    }

    public void setAdapter() {
        if (isAdded()) {
            recyclerView.setAdapter(new PhotoAdapter(mPhotoModels));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        apiClient.connect();
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        if(!(api.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS)){
            api.getErrorDialog(getActivity(), api.isGooglePlayServicesAvailable(getActivity()), 100, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    GPS_OK = false;
                }
            }).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
            apiClient.disconnect();
    }

    private void preventAsyncMemoryLeak() {
        if (mFetcherTask.getStatus() != AsyncTask.Status.FINISHED)
            mFetcherTask.cancel(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnaildownloader.quit();
        //preventAsyncMemoryLeak();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnaildownloader.clearQueue();
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageView;
        PhotoModel mPhotoModel;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView;
            mImageView.setOnClickListener(this);
        }

        public void bindPhotoModel(PhotoModel photoModel) {
            mPhotoModel = photoModel;
            Picasso.with(getActivity())
                    .load(mPhotoModel.getUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(mImageView);
        }

        public void bindPic(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            startActivity(PicPageActivity.newInstance(getActivity(), mPhotoModel.getPicPageUri()));
        }
    }

    class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<PhotoModel> mPhotoModels;

        public PhotoAdapter(List<PhotoModel> photoModels) {
            mPhotoModels = photoModels;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.pic_list_item, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            /*if(mLruCache.get(mPhotoModels.get(position).getUrl()) == null) {
                Log.d("cachenull","pic is null at position - "+position);
                mThumbnaildownloader.queueThumbnail(holder, mPhotoModels.get(position).getUrl());
            }
            else {
                Log.d("cachefound","pic found at position - "+position);
                Drawable drawable = new BitmapDrawable(getResources(),mPhotoModels.get(position).getUrl());
                holder.bindPic(drawable);
            }*/
            holder.bindPhotoModel(mPhotoModels.get(position));
        }

        @Override
        public int getItemCount() {
            return mPhotoModels.size();
        }
    }

    private class FetcherTask extends AsyncTask<Void, Void, PhotoRequestResult> {

        String mQuery;

        public FetcherTask(String query) {
            mQuery = query;
        }

        @Override
        protected PhotoRequestResult doInBackground(Void... voids) {
            PhotoRequestResult photoRequestResult = null;
            try {
                if (mQuery != null)
                    photoRequestResult = new FlickrFetcher().getSearchItems(mCurrentPage, mQuery);
                else
                    photoRequestResult = new FlickrFetcher().getRecentItems(mCurrentPage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return photoRequestResult;
        }

        @Override
        protected void onPostExecute(PhotoRequestResult photoModelsResult) {
            postExecuteCall(photoModelsResult);
        }

        private void postExecuteCall(PhotoRequestResult photoModelsResult) {
            mProgressBar.setVisibility(View.GONE);
            if (photoModelsResult == null) {
                mPhotoModels = new ArrayList<>();
                setAdapter();
            } else {
                QueryPrefarance.setLastResultId(photoModelsResult.getResults().get(0).getId(), getActivity());
                if (mPhotoModels.size() == 0) {
                    mPhotoModels.addAll(photoModelsResult.getResults());
                    maxPage = photoModelsResult.getPageCount();
                    mItemsPerPage = photoModelsResult.getItemsPerPage();
                    mTotalItemCount = photoModelsResult.getItemCount();
                    setAdapter();
                    setCurrentPageView();
                } else {
                    final int oldSize = mPhotoModels.size();
                    mPhotoModels.addAll(photoModelsResult.getResults());
                    recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            // Scroll to first row of newly added set
                            recyclerView.smoothScrollToPosition(oldSize);
                            setCurrentPageView();
                            amLoading = false;
                        }
                    });
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        }
    }

}
