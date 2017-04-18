package testprojs.animdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private boolean mStarted = false;
    private FrameView mFrameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrameView = (FrameView) findViewById(R.id.frame_view);
    }

    public void btn_clicked(View view) {
        if(mStarted) {
            mFrameView.stopAnim();
        } else {
            mFrameView.startAnim();
        }
        mStarted = !mStarted;
        ((Button)view).setText(mStarted ? "stop frame anim" : "start frame anim");
    }
}
