package zhenma.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

public class MainActivity extends Activity {

    private ImageView appIcon;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appIcon = (ImageView) findViewById(R.id.appIcon);
        mySwitch = (Switch) findViewById(R.id.StartButton);
        mySwitch.setChecked(false);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    appIcon.setImageResource(R.drawable.green_handshack);
                }else{
                    appIcon.setImageResource(R.drawable.blue_handshack);                }

            }
        });


//        ;Listener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                appIcon.setImageResource(R.drawable.green_handshack);
//            }
//        });
    }
}
