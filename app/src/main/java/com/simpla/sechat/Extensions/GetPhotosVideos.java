package com.simpla.sechat.Extensions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.iceteck.silicompressorr.SiliCompressor;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.simpla.sechat.MessageActivity;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GetPhotosVideos {

    public static Boolean folderCheck(String folderName){
        File folder = new File(folderName);
        File[] files = folder.listFiles();
        return files != null && files.length > 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ArrayList<String> loadAllPhotos(Context context){
        ArrayList<String> galleryImageUrls = new ArrayList<>();
        String[] columns = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        @SuppressLint("Recycle") Cursor imageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ,columns,null,null,orderBy+" DESC");
        assert imageCursor != null;
        for(int i = 0; i<imageCursor.getCount(); i++){
            imageCursor.moveToPosition(i);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            galleryImageUrls.add(imageCursor.getString(dataColumnIndex));
        }
        return galleryImageUrls;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static ArrayList<String> loadAllVideos(Context context){
        ArrayList<String> galleryImageUrls = new ArrayList<>();
        String[] columns = new String[]{MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID};
        String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        @SuppressLint("Recycle") Cursor imageCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ,columns,null,null,orderBy+" DESC");
        assert imageCursor != null;
        for(int i = 0; i<imageCursor.getCount(); i++){
            imageCursor.moveToPosition(i);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Video.Media.DATA);
            galleryImageUrls.add(imageCursor.getString(dataColumnIndex));
        }
        return galleryImageUrls;
    }

    public static ArrayList<String> GetFiles(String folderName){
        ArrayList<String> allFiles = new ArrayList<>();
        File file = new File(folderName);
        File[] allFiles2;
        allFiles2 = file.listFiles();

        if(allFiles2 != null){

            if(allFiles2.length>1){
                Arrays.sort(allFiles2, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));
            }

            for (File value : allFiles2) {
                if (value.isFile()) {
                    String filePath = value.getAbsolutePath();
                    String fileType = filePath.substring(filePath.lastIndexOf("."));
                    if (fileType.equalsIgnoreCase(".jpg") || fileType.equalsIgnoreCase(".jpeg") ||
                            fileType.equalsIgnoreCase(".png") || fileType.equalsIgnoreCase(".mp4")
                            || fileType.equalsIgnoreCase(".mov") || fileType.equalsIgnoreCase(".gif") ||
                            fileType.equalsIgnoreCase(".heic")){

                        allFiles.add(filePath);
                    }
                }
            }
        }
        return allFiles;
    }

    public static void compressImageFile(Activity activity, Context mContext, String chosenFilePath){
        try{
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    String s = null;
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/SeChat");
                    if(file.isDirectory() || file.mkdirs()){
                       s = SiliCompressor.with(mContext).compress(chosenFilePath,file);
                    }
                    if(s != null){
                        MessageActivity messageActivity = (MessageActivity) activity;
                        messageActivity.uploadToStorage(s);
                    }
                }
            };
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void compressVideoFile(Activity activity, String chosenFilePath){
        try {
            Thread thread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    final String[] path = {null};
                    File mTranscodeOutputFile = null;
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/SeChat");
                    try {
                        mTranscodeOutputFile = File.createTempFile("transcode_test", ".mp4", file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    DefaultVideoStrategy strategy = new DefaultVideoStrategy.Builder()
                            //  .bitRate(100000)
                            .bitRate(DefaultVideoStrategy.BITRATE_UNKNOWN) // tries to estimate
                            .build();
                    DefaultAudioStrategy strategy2 = DefaultAudioStrategy.builder()
                            .channels(DefaultAudioStrategy.CHANNELS_AS_INPUT)
                            //.channels(1)
                            // .channels(2)
                            .sampleRate(DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT)
                            //.sampleRate(44100)
                            // .sampleRate(30000)
                            .bitRate(DefaultAudioStrategy.BITRATE_UNKNOWN)
                            // .bitRate(bitRate)
                            .build();
                    File finalMTranscodeOutputFile = mTranscodeOutputFile;
                    if(mTranscodeOutputFile != null){
                        Transcoder.into(mTranscodeOutputFile.getPath()).setAudioTrackStrategy(strategy2)
                                .addDataSource(chosenFilePath).setVideoTrackStrategy(strategy).setListener(new TranscoderListener() {
                            @Override
                            public void onTranscodeProgress(double progress) {

                            }

                            @Override
                            public void onTranscodeCompleted(int successCode) {
                                if(successCode == Transcoder.SUCCESS_TRANSCODED || successCode == Transcoder.SUCCESS_NOT_NEEDED ){
                                    path[0] = finalMTranscodeOutputFile.getAbsolutePath();
                                    if(!path[0].isEmpty()){
                                        MessageActivity messageActivity = (MessageActivity) activity;
                                        messageActivity.uploadToStorage(path[0]);
                                    }
                                }
                            }

                            @Override
                            public void onTranscodeCanceled() {

                            }

                            @Override
                            public void onTranscodeFailed(@NonNull Throwable exception) {
                                exception.printStackTrace();
                            }
                        }).transcode();
                    }
                }
            };
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
