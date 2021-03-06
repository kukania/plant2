package com.plant;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kakao.usermgmt.response.model.User;
import com.plant.Kakao.GlobalApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class ChatingActivity extends Activity implements View.OnClickListener {
    Context mContext;
    ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    TextView chatingTimer;
    ListView textBody;
    EditText chatingContent;
    long time;
    ImageView sendBtn;
    ImageView backBtn;

    RoomData myRoomData;
    UserData myUserData;
    ArrayList<UserData> participatedUser;
    ArrayList<Integer> withNumber;
    ChatingListViewAdapter adapter;

    getData myChatingThread;
    boolean end = false;
    int id = 0;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref=getSharedPreferences("chating",MODE_PRIVATE);
        mContext = this;
        setContentView(R.layout.activity_chating);
        Intent intent = getIntent();
        myRoomData = (RoomData) intent.getSerializableExtra("roomData");
        myUserData = ((GlobalApplication)getApplication()).userData;
        participatedUser = (ArrayList<UserData>) intent.getSerializableExtra("participated");
        withNumber = (ArrayList<Integer>) intent.getSerializableExtra("withNumber");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.parseColor("#77361a"));
        }
        init();
        initData();
        threadStart();
        textBody.setAdapter(adapter);
    }

    void init() {
        if (myRoomData.roomID == -1) {
            Log.d("ERROR", "No Room Data Input");
            finish();
        } else {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss + dd일");
            Date d = new Date(myRoomData.startTime - Calendar.getInstance().getTimeInMillis());
            String timeStr = format.format(d);
            chatingTimer = (TextView) findViewById(R.id.chatingTimer);
            chatingTimer.setText(timeStr);
            textBody = (ListView) findViewById(R.id.textBody);
            (sendBtn = (ImageView) findViewById(R.id.sendBtn)).setOnClickListener(this);
            (backBtn = (ImageView) findViewById(R.id.backBtn)).setOnClickListener(this);
        }
        chatingContent = (EditText) findViewById(R.id.chatingContents);
        chatingContent.setOnFocusChangeListener(new MyFocusChangeListener());
    }

    void initData() {
        adapter = new ChatingListViewAdapter(this, myUserData);
        try {
            String getData = "";
            if (HttpRequest.isInternetConnected(mContext))
                getData = new getFirstData().execute("http://www.plan-t.kr/chating/getFirstChating.php?roomID=" + myRoomData.roomID).get();
            else {
                //인터넷 연결이 안되어 있을 떄의 처리.
            }
            JSONObject myJsonObject;
            try {
                myJsonObject = new JSONObject(getData);
                for (int i = 0; i < myJsonObject.length(); i++) {
                    JSONObject obj = new JSONObject(myJsonObject.getString((id + 1) + ""));
                    int num = getUserDataFromParticipates(obj.getString("userID"));
                    if(num!=-1) {
                        UserData temp = participatedUser.get(num);
                        String content = URLDecoder.decode(obj.getString("content"), "euc-kr");
                        obj.remove("content");
                        obj.put("content", content);
                        obj.put("profile", temp.thumbnailPath);
                        obj.put("userNum", withNumber.get(num));
                        obj.put("name", temp.name);
                        adapter.myJsonObjectList.add(obj);
                    }
                    else{
                        String content = URLDecoder.decode(obj.getString("content"), "euc-kr");
                        obj.remove("content");
                        obj.put("content", content);
                        obj.put("profile", "");
                        obj.put("userNum",1);
                        obj.put("name", "없음");
                        adapter.myJsonObjectList.add(obj);
                    }
                    id++;
                    SharedPreferences.Editor editor=pref.edit();
                    editor.putInt(""+myRoomData.roomID,id);
                    editor.commit();
                    // Log.d("obj",obj.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    void threadStart() {
        myChatingThread = new getData();
        if (HttpRequest.isInternetConnected(mContext))
            myChatingThread.execute();
        else {
            //인터넷 연결이 안되어 있을 떄의 처리.
        }
        timeStart();
    }

    int getUserDataFromParticipates(String ID) {
        for (int i = 0; i < participatedUser.size(); i++) {
            if (participatedUser.get(i).userID.equals(ID))
                return i;
        }
        return -1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn:
                String input = chatingContent.getText().toString();
                chatingContent.setText("");

                if(input.trim().equals(""))
                    break;
                HttpRequest myRequest = new HttpRequest("http://plan-t.kr/chating/insertChating.php");
                JSONObject json = new JSONObject();
                try {
                    json.put("userID", myUserData.userID);
                    json.put("roomID", myRoomData.roomID);
                    json.put("content", input);
                } catch (Exception e) {
                    e.getStackTrace();
                }
                myRequest.makeQuery(json);
                if (HttpRequest.isInternetConnected(mContext)) {
                    myRequest.run();

                    String sendPushAlarmUrl = "http://plan-t.kr/sendPushAlarm.php";
                    HttpRequest sendPushAlarmRequest = new HttpRequest(sendPushAlarmUrl);
                    sendPushAlarmRequest.makeQuery(myRoomData.getRoomDataJson());
                    sendPushAlarmRequest.run();
                }
                try {
                    myRequest.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.backBtn:
                finish();
                break;
        }
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.chatingContents && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    class getFirstData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String returnV = "";
            HttpRequest httpRequest = new HttpRequest(params[0]);
            if(HttpRequest.isInternetConnected(mContext)) {
                httpRequest.start();
                returnV = httpRequest.requestResult;
                return returnV;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String params) {
            Log.d("getFromChating", params);
        }
    }

    class getData extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void a[]) {
            String returnV = "";
            while (!end) {
                HttpRequest httpRequest = new HttpRequest("http://www.plan-t.kr/chating/getChating.php?roomID=" + myRoomData.roomID + "&ID=" + id);
                if(HttpRequest.isInternetConnected(mContext)) {
                    httpRequest.start();
                    try {
                        httpRequest.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    publishProgress(httpRequest.requestResult);
                    returnV = httpRequest.requestResult;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return returnV;
        }

        @Override
        protected void onProgressUpdate(String... params) {
            JSONObject myJsonObject;
            try {
                    myJsonObject = new JSONObject(params[0]);
                    for (int i = 0; i < myJsonObject.length(); i++) {
                        JSONObject obj = new JSONObject(myJsonObject.getString((id + 1) + ""));
                        int num = getUserDataFromParticipates(obj.getString("userID"));
                        UserData temp = participatedUser.get(num);
                        String content = URLDecoder.decode(obj.getString("content"), "euc-kr");
                        obj.remove("content");
                        obj.put("content", content);
                        obj.put("profile", temp.profilePath);
                        obj.put("userNum", withNumber.get(num));
                        obj.put("name", temp.name);
                        adapter.myJsonObjectList.add(obj);
                        adapter.notifyDataSetChanged();
                        textBody.setSelection(adapter.getCount() - 1);
                        id++;
                        SharedPreferences.Editor editor=pref.edit();
                        editor.putInt(""+myRoomData.roomID,id);
                        editor.commit();
                        // Log.d("obj",obj.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private TimerTask second;
    private final Handler handler = new Handler();
    long timer_sec;
    Timer timer = new Timer();
    String dayCnt = "";
    int hourTemp;
    public void timeStart() {
        second = new TimerTask() {
            @Override
            public void run() {
                Update();
                Calendar tempC = Calendar.getInstance();
                Calendar nowC = Calendar.getInstance();
                timer_sec = myRoomData.startTime - nowC.getTimeInMillis();
                tempC.setTimeInMillis(myRoomData.startTime);
                hourTemp=tempC.get(Calendar.HOUR_OF_DAY)-nowC.get(Calendar.HOUR_OF_DAY);
                if(hourTemp<0)hourTemp+=24;
                if (tempC.get(Calendar.MONTH) - nowC.get(Calendar.MONTH) > 0)
                    dayCnt = "7일+";
                else {
                    int c = tempC.get(Calendar.DAY_OF_MONTH) - nowC.get(Calendar.DAY_OF_MONTH);
                    if ((c) > 7)
                        dayCnt = "7일+";
                    else
                        dayCnt = c + "일";
                }
            }
        };
        timer.schedule(second, 0, 1000);
    }

    protected void Update() {
        Runnable updater = new Runnable() {
            public void run() {
                if (timer_sec <= 0) {
                    HttpRequest request = new HttpRequest("http://plan-t.kr/chating/timeOutPrint?roomID=" + myRoomData.roomID);
                    if(HttpRequest.isInternetConnected(mContext)) {
                        Thread t = new Thread(request);
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                        request.run(mContext);
//                        try {
//                            request.join();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        if (request.requestResult.equals("success")) {
                            myUserData.point++;
                        }
                        finish();
                    }
                }
                Calendar temp=Calendar.getInstance();
                temp.setTimeInMillis(timer_sec);

                chatingTimer.setText(hourTemp+":"+temp.get(Calendar.MINUTE)+":"+temp.get(Calendar.SECOND)+"/" + dayCnt);
            }
        };
        handler.post(updater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        end = true;
        timer.cancel();
    }
}
