package andrew.zach.soundrite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Zach on 06/11/2017.
 */

public class CurrentScore extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currentscore);
       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/



    }//outside onCreate
    @Override
    public void onBackPressed(){
        Intent intent=new Intent (CurrentScore.this,MainActivity.class);
        startActivity(intent);
    }
}
