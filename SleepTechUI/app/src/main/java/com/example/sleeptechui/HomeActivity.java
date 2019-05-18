package com.example.sleeptechui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {
    final private String TAG = "HomeActivity";
    final private boolean DEBUG_ON = true;

    //-------------------- Declaration member Block --------------------
    private TextView mHomeTextView = null;;
    private Button mGoodButton = null;;
    private Button mBadButton = null;;
    private RadioGroup mRadioGroup = null;;
    private RadioButton mReasonRadioButton1 = null;;
    private RadioButton mReasonRadioButton2 = null;;
    private RadioButton mReasonRadioButton3 = null;;
    private RadioButton mReasonRadioButton4 = null;;
    private GridView mSleepDaysGridView = null;;
    private SleepDaysGridAdapter mSleepDaysGridViewAdapter = null;;
    private BottomNavigationView mBottomNavigationView = null;;
    private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
    private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private SleepDataManager mSleepDataManager = null;
    private String[] mSleepDaysList = null;
    private RapiroManager mRapiroManager = null;
    private String mBadCommandWithReason = null;

    //-------------------- Declaration Listener Block --------------------
    View.OnClickListener mGoodButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.UI_ITEM_ID_GOOD_BUTTON:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mRapiroManager.sendCommand(SleepTechUIConstants.COMMAND_ID_FEEL_GOOD);
                        }
                    }).start();
                    break;
                default:
            }
        }
    };

    View.OnClickListener mBadButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.UI_ITEM_ID_BAD_BUTTON:
                    if(mBadCommandWithReason == null){
                        Toast.makeText(HomeActivity.this,
                                "寝れなかった理由を選択して下さい。",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mRapiroManager.sendCommand(mBadCommandWithReason);
                            }
                        }).start();
                    }
                    break;
                default:
            }
        }
    };

    RadioGroup.OnCheckedChangeListener mRadioGroupClickedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (-1 == checkedId) {
                Toast.makeText(HomeActivity.this,
                        "クリアされました",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this,
                        ((RadioButton)findViewById(checkedId)).getText()
                                + "が選択されました",
                        Toast.LENGTH_SHORT).show();
                this.onClickRadioButton(checkedId);
            }
        }

        private void onClickRadioButton(int checkedId) {
            switch (checkedId){
                case R.id.UI_ITEM_ID_BAD_REASON_1_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_HEAT;
                    break;
                case R.id.UI_ITEM_ID_BAD_REASON_2_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_COLDNESS;
                    break;
                case R.id.UI_ITEM_ID_BAD_REASON_3_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_BRIGHTNESS;
                    break;
                case R.id.UI_ITEM_ID_BAD_REASON_4_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_DARKNESS;
                    break;
                case R.id.UI_ITEM_ID_BAD_REASON_5_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_MOISTURE;
                    break;
                case R.id.UI_ITEM_ID_BAD_REASON_6_RADIO_BUTTON:
                    mBadCommandWithReason = SleepTechUIConstants.COMMAND_ID_FEEL_BAD_BECAUSE_OF_DRY;
                    break;
                default:
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    private AdapterView.OnItemClickListener mSleepDaysGridViewListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String touchedDay = null;
            if (mSleepDaysGridViewAdapter != null) {
                touchedDay = mSleepDaysGridViewAdapter.getPositionWord(position);
                if(touchedDay == null){
                    Log.d(TAG, "日付の取得に失敗しました。");
                    return;
                }
            }
            if (mSleepDataManager != null) {
                SleepDayData data = mSleepDataManager.selectData(touchedDay);
                if (data != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("SLEEP_DAY_DATA", data);
                    SleepDayDialog viewDialog = new SleepDayDialog();
                    viewDialog.setArguments(bundle);
                    viewDialog.show(getSupportFragmentManager(), TAG);
                } else {
                    Log.d(TAG, "表示できるデータはありません");
                }
                Log.d(TAG, "ダイアログ表示!");
            }
        }

    };

    //-------------------- Declaration Function Block --------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSleepDataManager = new SleepDataManager(getApplicationContext());
        mSleepDataManager.deleteAllData();
        if(DEBUG_ON){
            Log.d(TAG,"ダミーデータ作成");
            createDummyData();
        }
        initView();
        BottomNavigationView mBottomNavigationView = (BottomNavigationView) findViewById(R.id.UI_ITEM_ID_UNDER_NAVIGATION);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mRapiroManager = new RapiroManager();
    }

    private void initView(){
        mHomeTextView = (TextView)findViewById(R.id.UI_ITEM_ID_GO_HOME);
        mGoodButton = (Button)findViewById(R.id.UI_ITEM_ID_GOOD_BUTTON);
        mBadButton = (Button)findViewById(R.id.UI_ITEM_ID_BAD_BUTTON);
        mRadioGroup = (RadioGroup)findViewById(R.id.UI_ITEM_ID_BAD_REASON_RADIO_GROUP);
        mReasonRadioButton1 = (RadioButton)findViewById(R.id.UI_ITEM_ID_BAD_REASON_1_RADIO_BUTTON);
        mReasonRadioButton2 = (RadioButton)findViewById(R.id.UI_ITEM_ID_BAD_REASON_2_RADIO_BUTTON);
        mReasonRadioButton2 = (RadioButton)findViewById(R.id.UI_ITEM_ID_BAD_REASON_3_RADIO_BUTTON);
        mReasonRadioButton4 = (RadioButton)findViewById(R.id.UI_ITEM_ID_BAD_REASON_4_RADIO_BUTTON);
        mSleepDaysGridView = (GridView)findViewById(R.id.UI_ITEM_ID_SLEEP_DAYS_NAVIGATION_VIEW);
        mBottomNavigationView = (BottomNavigationView)findViewById(R.id.UI_ITEM_ID_UNDER_NAVIGATION);
        mGoodButton.setOnClickListener(mGoodButtonClickListener);
        mBadButton.setOnClickListener(mBadButtonClickListener);
        mRadioGroup.setOnCheckedChangeListener(mRadioGroupClickedChangeListener);
        mSleepDaysList = mSleepDataManager.getSleepDaysListFromDB();
        mSleepDaysGridViewAdapter = new SleepDaysGridAdapter(getApplicationContext(), R.layout.sleep_days_table, mSleepDaysList.length,mSleepDaysList);
        mSleepDaysGridView.setAdapter(mSleepDaysGridViewAdapter);
        mSleepDaysGridView.setOnItemClickListener(mSleepDaysGridViewListener);
    }

    private void createDummyData(){
        SleepDayData dummyData1 = new SleepDayData("4月21日",23,30,14,"GOOD");
        SleepDayData dummyData2 = new SleepDayData("4月22日",24,35,9,"BAD");
        SleepDayData dummyData3 = new SleepDayData("4月23日",22,30,5,"BAD");
        SleepDayData dummyData4 = new SleepDayData("4月24日",21,35,20,"BAD");
        SleepDayData dummyData5 = new SleepDayData("4月25日",25,30,11,"GOOD");
        SleepDayData dummyData6 = new SleepDayData("5月31日",20,25,10,"(未設定)");
        if(mSleepDataManager != null){
            mSleepDataManager.insertData(dummyData1);
            mSleepDataManager.insertData(dummyData2);
            mSleepDataManager.insertData(dummyData3);
            mSleepDataManager.insertData(dummyData4);
            mSleepDataManager.insertData(dummyData5);
            mSleepDataManager.insertData(dummyData6);
        }
    }



}
