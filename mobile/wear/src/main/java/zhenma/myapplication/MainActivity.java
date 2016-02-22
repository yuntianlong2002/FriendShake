package zhenma.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {

    private ImageView appIcon;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appIcon = (ImageView) findViewById(R.id.appIcon);
        button = (Button) findViewById(R.id.StartButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appIcon.setImageResource(R.drawable.green_handshack);
            }
        });
    }
}
