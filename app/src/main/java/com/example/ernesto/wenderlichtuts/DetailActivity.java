package com.example.ernesto.wenderlichtuts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class DetailActivity extends ActionBarActivity {
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    private String title;

    String mImageURL;
    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = (ImageView)findViewById(R.id.img_cover);

        String coverID = this.getIntent().getExtras().getString("coverID");
        String firstSentence = this.getIntent().getExtras().getString("first_sentence");

        if(coverID.length() > 0 ){
            mImageURL = IMAGE_URL_BASE + coverID + "-L.jpg";
            Picasso.with(this).load(mImageURL).placeholder(R.drawable.img_books_loading).into(imageView);
        }

        TextView textView = (TextView)findViewById(R.id.first_sentence);
        textView.setText(firstSentence);

        title =  this.getIntent().getExtras().getString("title");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        if(shareItem != null){
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        setSharedIntent();

        return true;
    }

    public void setSharedIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Book Recommendation!");

        String hashtag = title.replaceAll("\\s","");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "#" + hashtag + " " +  mImageURL);

        mShareActionProvider.setShareIntent(shareIntent);
    }
}
























