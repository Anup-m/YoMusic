package yomusic.developer.it.yomusic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import yomusic.developer.it.yomusic.Infos.SongInfo;
import yomusic.developer.it.yomusic.Utils.Utility;

import static yomusic.developer.it.yomusic.Fragments.SongListFragment.currSongPosition;
import static yomusic.developer.it.yomusic.Utils.Utility.auth;

public class SplashScreenActivity extends AppCompatActivity implements PrepareRetrieverTask.MusicRetrieverPreparedListener {
    Animation play_in;
    ImageView imageView;
    TextView tx_name ,tx_desc;
    Retriever mRetriever;
    Context context;

    String name="",email="",imgUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        changeStatusBarColor();
        imageView = findViewById(R.id.splashscreen_img);
        tx_name = findViewById(R.id.name);
        tx_desc = findViewById(R.id.description);

        imageView.setVisibility(View.GONE);
        context = getApplicationContext();
        //Animations
        play_in = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.play_in);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            getUserDetails();
        }

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(play_in);
                tx_name.setVisibility(View.VISIBLE);
                tx_desc.setVisibility(View.VISIBLE);
            }
        }, 200);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                checkPermission();
            }
        }, 1500);
    }

    public  void getUserDetails() {
        try {
            name = auth.getCurrentUser().getDisplayName();
            email = auth.getCurrentUser().getEmail();
            imgUrl = auth.getCurrentUser().getPhotoUrl().toString();
        } catch (NullPointerException ignored) {  }
    }


    public void changeStatusBarColor(){
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.colorGrey));
    }

    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            } else{
                load();
                startMainActivity();
                //startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                finish();
            }
        }
        else{
            load();
            startMainActivity();
            //startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 123:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    load();
                    startMainActivity();
                    //startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void load(){
        if(Utility._songs.isEmpty()) {
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareMusicRetriever")).execute();
        }

        if(Utility._albums.isEmpty()) {
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareAlbumsRetriever")).execute();
        }

        if(Utility._artists.isEmpty()) {
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareArtistsRetriever")).execute();
        }

        if(Utility.folderList.isEmpty()) {
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareFolderRetriever")).execute();
        }

        if(Utility._lastPlayed.isEmpty()){
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareLastPlayedRetriever")).execute();
        }

        if(Utility._favourites.isEmpty()){
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareFavouritesRetriever")).execute();
        }

        if(Utility._recentlyAdded.isEmpty()){
            mRetriever = new Retriever(getApplicationContext(),getContentResolver());
            (new PrepareRetrieverTask(mRetriever, this,"prepareRecentlyAddedRetriever")).execute();
        }

//        Intent serviceIntent = new Intent(this, MusicService.class);
//        serviceIntent.putExtra("songId",-1);
//        startService(serviceIntent);
    }

    @Override
    public void onMusicRetrieverPrepared() {
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashScreenActivity.this,MainActivity.class);
        intent.putExtra("NAME",name);
        intent.putExtra("EMAIL",email);
        intent.putExtra("IMG_URL",imgUrl);
        startActivity(intent);
    }

    private boolean getSingInPreference(Context context) {
        //sharedPreference data for last played song...
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.log_out),false);
    }

}
