package andrew.zach.soundrite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Zach on 04/11/2017.
 */

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

       Button enter=(Button) findViewById(R.id.button_enter);

        ImageView myTitleImage=(ImageView) findViewById(R.id.titleText);

        myTitleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent (WelcomeActivity.this,MainActivity.class);
                startActivity(intent); //onclick move to main page
            }

        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent (WelcomeActivity.this, MainActivity.class);
                startActivity(intent); // when enter is clicked, take user to 'WelcomeActivity.java' page
            }
        });


    }
}
