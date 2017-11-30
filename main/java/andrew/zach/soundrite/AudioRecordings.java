package andrew.zach.soundrite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Zach on 09/11/2017.
 */

public class AudioRecordings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recordings);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/


    }
    @Override
    public void onBackPressed(){
        Intent intent=new Intent (AudioRecordings.this,MainActivity.class);
        startActivity(intent);
    }
}
