package com.simpla.sechat.Extensions;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.simpla.sechat.R;

public class UniversalImageLoader {

    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
    }

    public ImageLoaderConfiguration getConfig() {

        int defaultImage = R.drawable.loading_picture;
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(400)).build();

        return new ImageLoaderConfiguration.Builder(mContext).defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache()).diskCacheSize(100*1024*1024).build();
    }

    public static void setImage(String imgURL, ImageView imageView, final ProgressBar progressBar, String firstPart){

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(firstPart+imgURL,imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(progressBar !=null){
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(progressBar!=null){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(progressBar!=null){
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(progressBar!=null){
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


}

