package com.plant;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kakao.usermgmt.response.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.Inflater;

public class ReservationMakeFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {
    Context mContext;

    RoomData roomData = new RoomData();
    View mainView;
    int year;
    int month;
    int day;
    int hour;
    int minute;

    /***************** UI ****************/
    LinearLayout parentLayout;
    AutoCompleteTextView destination_editText;
    EditText comment_editText;
    ReservationMakeFragmentSpinner withNumSpin;
    ImageView onetwoWay_btn[] = new ImageView[2];
    ImageView goal_btn[] = new ImageView[3];
    TextView reservation_make_month_textView;
    TextView reservation_make_day_textView;
    TextView reservation_make_hour_textView;
    TextView reservation_make_minute_textView;
    LinearLayout dayLaout;
    LinearLayout timeLayout;
    ImageView send_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_reservation_make, container, false);
        init();
        ((FrameActivity)mContext).setupUI(mainView);

        return mainView;
    }

    public void init() {
        mContext = getContext();

        AutoCompletePHP autoCompletePHP = new AutoCompletePHP();
        if(HttpRequest.isInternetConnected(mContext))
            autoCompletePHP.execute();

        roomData = new RoomData();
        parentLayout = (LinearLayout) mainView.findViewById(R.id.fragment_reservation_make_parent);
        destination_editText = (AutoCompleteTextView) mainView.findViewById(R.id.destination_editText);
        comment_editText = (EditText) mainView.findViewById(R.id.comment_editText);
        withNumSpin = (ReservationMakeFragmentSpinner) mainView.findViewById(R.id.UserNumSpin);
        reservation_make_month_textView = (TextView) mainView.findViewById(R.id.reservation_make_month_textView);
        reservation_make_day_textView = (TextView) mainView.findViewById(R.id.reservation_make_day_textView);
        reservation_make_hour_textView = (TextView) mainView.findViewById(R.id.reservation_make_hour_textView);
        reservation_make_minute_textView = (TextView) mainView.findViewById(R.id.reservation_make_minute_textView);
        dayLaout = (LinearLayout) mainView.findViewById(R.id.dayLayout);
        timeLayout = (LinearLayout) mainView.findViewById(R.id.timeLayout);
        (onetwoWay_btn[0] = (ImageView) mainView.findViewById(R.id.oneway_btn)).setOnClickListener(onetwoWayListener);
        (onetwoWay_btn[1] = (ImageView) mainView.findViewById(R.id.twoway_btn)).setOnClickListener(onetwoWayListener);
        (goal_btn[0] = (ImageView) mainView.findViewById(R.id.goal_btn0)).setOnClickListener(goalListener);
        (goal_btn[1] = (ImageView) mainView.findViewById(R.id.goal_btn1)).setOnClickListener(goalListener);
        (goal_btn[2] = (ImageView) mainView.findViewById(R.id.goal_btn2)).setOnClickListener(goalListener);
        send_btn = (ImageView) mainView.findViewById(R.id.send_btn);

        destination_editText.setOnEditorActionListener(this);

        //현재 시간을 입력한다.
        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        reservation_make_month_textView.setText("" + (month + 1));
        reservation_make_day_textView.setText("" + day);
        reservation_make_hour_textView.setText("" + hour);
        reservation_make_minute_textView.setText("" + minute);

        dayLaout.setOnClickListener(this);
        timeLayout.setOnClickListener(this);
        send_btn.setOnClickListener(this);

        withNumSpin.setAdapter(new WithNumSpinAdapter());
    }


    private class WithNumSpinAdapter implements SpinnerAdapter {
        ArrayList<String> spinnerItemList = new ArrayList<String>();
        TextView spinnerTextview;

        public WithNumSpinAdapter(){
            spinnerItemList.add("없음");
            spinnerItemList.add("1");
            spinnerItemList.add("2");
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.fragment_reservation_make_spinner_item, parent, false);

                spinnerTextview = (TextView) convertView.findViewById(R.id.fragment_reservation_make_spinner_textview);
            }
            spinnerTextview.setText(spinnerItemList.get(position));

            return convertView;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return spinnerItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return spinnerItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            withNumSpin.setBackgroundResource(R.drawable.reservation_withnumber_image_background);

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.fragment_reservation_make_spinner_item, parent, false);

            TextView tv = (TextView) convertView.findViewById(R.id.fragment_reservation_make_spinner_textview);
            tv.setText(spinnerItemList.get(position));

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return spinnerItemList.isEmpty();
        }
    }

    private class AutoCompletePHP extends AsyncTask<Void, Void, Void>{
        private final String autoCompleteURL = "http://plan-t.kr/autoComplete.php";
        private HttpRequest autoCompleteRequest;

        @Override
        protected Void doInBackground(Void... params) {
            autoCompleteRequest = new HttpRequest(autoCompleteURL);
            autoCompleteRequest.setContext(mContext);
            if(HttpRequest.isInternetConnected(mContext)){
                autoCompleteRequest.start();
                try {
                    autoCompleteRequest.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            String[] strings = autoCompleteRequest.requestResult.split(System.getProperty("line.separator"));
            ArrayAdapter<String> adWord = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, strings);
            destination_editText.setAdapter(adWord);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dayLayout:
                DatePickerDialog datePickerDialog = (DatePickerDialog) YearMonthDayPicker();
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.setTitle("날짜 설정");
                datePickerDialog.show();

                break;
            case R.id.timeLayout:
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, timePickerListener, hour, minute, false);
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.setTitle("시간 설정");
                timePickerDialog.show();
                break;
            case R.id.send_btn:
                if(withNumSpin.getSelectedItem() == null) {
                    Toast.makeText(getContext(), "동행 인원을 선택해주세요.", Toast.LENGTH_SHORT).show();
                } else if (destination_editText.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "목적지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "예약이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    //시간정보 format으로 변경하여 저장.
                    roomData.setRoomTimeData(year, month+1, day, hour, minute);
                    roomData.hostUserID = ((FrameActivity)getActivity()).userData.userID;
                    if(withNumSpin.getSelectedItem().equals("없음")){
                        roomData.userNum = 1;
                    }else {
                        roomData.userNum = Integer.parseInt((String)withNumSpin.getSelectedItem()) + 1;
                    }
                    roomData.roomType = RoomData.ROOM_TYPE_RESERVE;
                    roomData.setDestPoint(destination_editText.getText().toString());
                    roomData.comment = comment_editText.getText().toString();
                    roomData.maxUserNum = 4;

                    MakeRoomPHP makeRoomPHP = new MakeRoomPHP();
                    makeRoomPHP.execute(roomData);
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        v.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(((Activity)mContext).getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(((Activity)mContext).getCurrentFocus().getWindowToken(), 0);

        return false;
    }

    public class MakeRoomPHP extends AsyncTask<RoomData, Void, Void> {
        String makeRoomURL = "http://plan-t.kr/makeRoom.php";
        HttpRequest makeRoomRequest;

        @Override
        protected Void doInBackground(RoomData... params) {
            makeRoomRequest = new HttpRequest(makeRoomURL);
            makeRoomRequest.makeQuery(params[0].getRoomDataJson());
            makeRoomRequest.setContext(mContext);
            if(HttpRequest.isInternetConnected(mContext)){
                makeRoomRequest.start();
                try {
                    makeRoomRequest.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((FrameActivity) getActivity()).makeChange(3);
            ((FrameActivity) getActivity()).makeRoomCount++;
        }
    }

    public Dialog YearMonthDayPicker(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), android.R.style.Theme_Holo_Light_Dialog, datePickerListener, year, month, day);
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);

        return datePickerDialog;
    }

    //왕복, 편도 버튼 리스너
    View.OnClickListener onetwoWayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.oneway_btn:
                    if(roomData.round == false) break;
                    roomData.round = false;
                    onetwoWay_btn[0].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_oneway_clicked_btn));
                    onetwoWay_btn[1].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_twoway_btn));
                    break;
                case R.id.twoway_btn:
                    if(roomData.round == true)  break;
                    roomData.round = true;
                    onetwoWay_btn[0].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_oneway_btn));
                    onetwoWay_btn[1].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_twoway_clicked_btn));
                    break;
                default:
                    Log.d("ReservationMakeFragment", "error at onetwoWayListener");
            }
        }
    };

    //목적(자격증, 영어, 기타) 버튼 리스너
    //자격증 = 0, 영어 = 1, 기타 = 2;
    View.OnClickListener goalListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goal_btn[0].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option1));
            goal_btn[1].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option2));
            goal_btn[2].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option3));
            switch (v.getId()){
                case R.id.goal_btn0:
                    goal_btn[0].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option1_clicked));
                    roomData.roomObject = 0;
                    break;
                case R.id.goal_btn1:
                    goal_btn[1].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option2_clicked));
                    roomData.roomObject = 1;
                    break;
                case R.id.goal_btn2:
                    goal_btn[2].setBackground(ContextCompat.getDrawable(getContext(), R.drawable.reserve_box2_option3_clicked));
                    roomData.roomObject = 2;
                    break;
            }
        }
    };

    //DatePicker 리스너
    DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year_here, int monthOfYear, int dayOfMonth) {
            year = year_here;
            month = monthOfYear;
            day = dayOfMonth;
            reservation_make_month_textView.setText("" + (monthOfYear + 1));
            reservation_make_day_textView.setText("" + dayOfMonth);
        }
    };

    //TimePicker 리스너
    TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute_here) {
            hour = hourOfDay;
            minute = minute_here;
            reservation_make_hour_textView.setText("" + hourOfDay);
            reservation_make_minute_textView.setText("" + minute_here);
        }
    };
}
