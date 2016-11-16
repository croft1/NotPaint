package devicroft.notpaint;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class InternetImageFetch extends AppCompatActivity {

    Button confirm;
    EditText url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_image_fetch);

        confirm = (Button) findViewById(R.id.internet_image_button);
        url = (EditText) findViewById(R.id.internet_url_input);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(url.getText().toString().length() < 5){
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.internet_url_error), Toast.LENGTH_SHORT);
                }else{
                    Intent urlIntent = new Intent();
                    urlIntent.putExtra(MainActivity.FETCH_IMAGE_INTERNET, url.getText().toString());
                    setResult(RESULT_OK, urlIntent);
                    finish();
                    overridePendingTransition(0,0);
                }
            }
        });


        if(getIntent().hasExtra(MainActivity.FETCH_COLOR_BRUSH)){
            Intent i = getIntent();
            int colorInt = i.getIntExtra(MainActivity.FETCH_COLOR_BRUSH, 0);

            Log.d("Color intent", Integer.toString(colorInt));
            String colorHex = "#" + Integer.toHexString(colorInt);

            if(colorHex.length() > 2 || colorHex  != null) {
                colorHex.substring(2);
            }else{
                colorHex = "#111111";
            }
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorHex)));


        }
    }


}
