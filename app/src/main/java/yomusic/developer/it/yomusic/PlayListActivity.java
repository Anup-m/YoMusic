package yomusic.developer.it.yomusic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import yomusic.developer.it.yomusic.Utils.Utility;

public class PlayListActivity extends AppCompatActivity implements View.OnClickListener{

    Toolbar toolbar;
    TextView lastPlayed, recentlyAdded, topTracks, favourites;
    String name="", number="", listName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        toolbar = findViewById(R.id.toolbar);
        lastPlayed = findViewById(R.id.tx_lastPlayed);
        recentlyAdded = findViewById(R.id.tx_recentlyAdded);
        topTracks = findViewById(R.id.tx_topTracks);
        favourites = findViewById(R.id.tx_favourites);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lastPlayed.setOnClickListener(this);
        recentlyAdded.setOnClickListener(this);
        topTracks.setOnClickListener(this);
        favourites.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tx_lastPlayed:
                listName = "lastPlayedList";
                name = "Last Played Songs";
                number = String.valueOf(Utility.getLastPlayedListSize());
                break;
            case R.id.tx_recentlyAdded:
                listName = "recentlyAddedList";
                name = "Recently Added List";
                number = String.valueOf(Utility.getRecentlyAddedListSize());
                break;
            case R.id.tx_topTracks:
                listName = "topTracksList";
                name = "Most Played Songs";
                number = String.valueOf(Utility.getLastPlayedListSize()); //TODO To Be changed...
                break;
            case R.id.tx_favourites:
                listName = "favouriteList";
                name = "Favourites Songs";
                number = String.valueOf(Utility.getFavouriteListSize());
                break;
        }

        Intent intent = new Intent(this, DisplayTracksActivity.class);
        intent.putExtra("list", listName);
        intent.putExtra("name", name);
        intent.putExtra("noOfSongs",number);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
