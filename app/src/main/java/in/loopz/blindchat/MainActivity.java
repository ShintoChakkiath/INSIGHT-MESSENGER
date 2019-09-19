package in.loopz.blindchat;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String Msg="";
    int count=0;
    EditText etMsg;
    ImageView ivSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMsg=(EditText)findViewById(R.id.editText);
        ivSend=(ImageView)findViewById(R.id.ivSend);
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Msg=etMsg.getText().toString();
                count=0;
            }
        });
    }

    public void alphaClick(View v){
        Button button=(Button)v;
        String btnText=button.getText().toString();
        if(btnText.equals("SPACE"))
            btnText=" ";
        if(Msg.length()!=0) {
            if (btnText.equalsIgnoreCase("" + Msg.charAt(count))) {
                Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                count++;
                if (count == Msg.length()) {
                    count = 0;
                    Msg="";
                }
            }
        }
    }
}
