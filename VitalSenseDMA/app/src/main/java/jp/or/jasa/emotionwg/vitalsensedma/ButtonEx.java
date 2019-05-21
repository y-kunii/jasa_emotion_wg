package jp.or.jasa.emotionwg.vitalsensedma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//ボタンとダイアログ
public class ButtonEx extends Activity implements
        View.OnClickListener {
    private final static int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

    private final static String TAG_BEDOUT = "0";               // RESET
    private final static String TAG_BEDIN = "1";                // 入床
    private final static String TAG_SLEEPING   = "11";          // 入眠
    private final static String TAG_SLEEPING_DEEP = "1x";       // 深い眠りボタン
    private final static String TAG_SLEEPING_DEEP2 = "12";
    private final static String TAG_SLEEPING_DEEP3 = "13";
    private final static String TAG_SLEEPING_DEEP4 = "14";
    private final static String TAG_MORNING    = "21";          // 朝眠りが浅くなった
    private final static String TAG_MORNING_DEEP2 = "22";
    private final static String TAG_MORNING_DEEP3 = "23";
    private final static String TAG_MORNING_DEEP4 = "24";
    private final static String TAG_WAKEUP   = "31";            // 覚醒
    //    private UploadTask task;
    private Handler handler = new Handler();
    private String text;
    private PostEvent task;

    private int sleepDepth = -1;
    private final static String sleepDepthTag[] = {
            TAG_SLEEPING_DEEP2,
            TAG_SLEEPING_DEEP3,
            TAG_SLEEPING_DEEP4,
            TAG_SLEEPING_DEEP3,
            ""
    };

    //アクティビティ起動時に呼ばれる
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //レイアウトの生成
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.WHITE);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        //ボタンの生成(1)
        layout.addView(makeButton(res2bmp(this, R.drawable.bed_bedin), TAG_BEDIN));
        layout.addView(makeButton(res2bmp(this, R.drawable.bed_sleeping), TAG_SLEEPING));
        layout.addView(makeButton("深い眠り", TAG_SLEEPING_DEEP));
        layout.addView(makeButton(res2bmp(this, R.drawable.bed_sleepy_morning), TAG_MORNING));
        layout.addView(makeButton(res2bmp(this, R.drawable.bed_wakeup), TAG_WAKEUP));
        layout.addView(makeButton("離床（RESET）", TAG_BEDOUT));
    }

    //ボタンの生成(1)
    private Button makeButton(String text, String tag) {
        Button button = new Button(this);
        button.setText(text);
        button.setTag(tag);
        button.setOnClickListener(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
        return button;
    }

    //イメージボタンの生成(3)
    private ImageButton makeButton(Bitmap bmp, String tag) {
        ImageButton button = new ImageButton(this);
        button.setTag(tag);
        button.setOnClickListener(this);
        button.setImageBitmap(bmp);
        button.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
        return button;
    }

    //リソース→ビットマップ
    public Bitmap res2bmp(Context context, int resID) {
        return BitmapFactory.decodeResource(
                context.getResources(), resID);
    }

    //ボタン押下時に呼ばれる(2)
    public void onClick(View view) {
        String tagWork = (String)view.getTag();

        // 1 つのボタンで眠りの深さを順番に変える。
        if (tagWork.equals(TAG_SLEEPING_DEEP)) {
            sleepDepth++;
            if (sleepDepthTag[sleepDepth].equals("")) {
                sleepDepth = 0;
            }
            tagWork = sleepDepthTag[sleepDepth];
        }
        else {
            sleepDepth = -1;
        }

        final String tag = tagWork;

        Thread thread = new Thread(new Runnable() { public void run() {
            // HTTP 通信
            try {
                task = new PostEvent();
                task.setListener(createListener());
                task.execute(tag);
            } catch (Exception e) {
                text = null;
            }
        }});
        thread.start();

//        MessageDialog.show(this, "メッセージダイアログ", "ボタンを押した");
    }


    private PostEvent.Listener createListener() {
        return new PostEvent.Listener() {
            @Override
            public void onSuccess(String result) {
                // TODO: 送信結果を何か表示させたいが、後回し。
//                textView.setText(result);
                Log.d("***** debug *****","sendRequest : PostEvent.Listener onSuccess");
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //メッセージダイアログの定義(4)
    public static class MessageDialog extends DialogFragment {
        //ダイアログの表示(5)
        public static void show(
                Activity activity, String title, String text) {
            MessageDialog f = new MessageDialog();
            Bundle args = new Bundle();
            args.putString("title", title);
            args.putString("text", text);
            f.setArguments(args);
            f.show(activity.getFragmentManager(), "messageDialog");
        }
    }
}
