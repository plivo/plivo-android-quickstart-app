package com.plivo.plivosimplequickstart;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmitFeedbackDialog extends DialogFragment {

    private static final String TAG = "SubmitFeedbackDialog";

    RatingBar callRatingView;
    EditText commentTextView;
    CheckBox sendLogsBox;
    Button submitFeedbackButton;
    Button skipFeedbackButton;
    ListView listView;
    TextView commentTextViewHead;


    public static Map<String, String> DEFAULT_COMMENTS = new HashMap<String, String>() {{
        put("AUDIO LAG", "audio_lag");
        put("BROKEN AUDIO", "broken_audio");
        put("CALL DROPPED", "call_dropped");
        put("CALLERID ISSUES", "callerid_issue");
        put("DIGITS NOT CAPTURED", "digits_not_captured");
        put("ECHO", "echo");
        put("HIGH CONNECT_TIME", "high_connect_time");
        put("LOW AUDIO LEVEL", "low_audio_level");
        put("ONE WAY AUDIO", "one_way_audio");
        put("OTHERS", "others");
        put("ROBOTIC AUDIO", "robotic_audio");
    }};



    ArrayList<String> selectedIssueList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.submit_feedback, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        callRatingView = view.findViewById(R.id.callrating_star);
        commentTextView = view.findViewById(R.id.comment_text);
        sendLogsBox = view.findViewById(R.id.sendlogs_toggle);
        submitFeedbackButton = view.findViewById(R.id.submit_feedback_bt);
        skipFeedbackButton = view.findViewById(R.id.skip_feedback_bt);
        listView = (ListView) view.findViewById(R.id.issue_list);
        commentTextViewHead = view.findViewById(R.id.comment_text_view);

        List<String> issueNameList = new ArrayList<String>(DEFAULT_COMMENTS.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), R.layout.issue_item, issueNameList);


        listView.setAdapter(adapter);
        listView.setOnItemClickListener((arg0, arg1, position, id) -> {

            if(!selectedIssueList.contains(issueNameList.get(position))){
                selectedIssueList.add(issueNameList.get(position));
                listView.getChildAt(position).setBackgroundColor(Color.RED);
            }else{
                selectedIssueList.remove(issueNameList.get(position));
                listView.getChildAt(position).setBackgroundColor(Color.WHITE);
            }
        });


        submitFeedbackButton.setOnClickListener(v -> {
            if(callRatingView.getRating() == 5.0) resetValues();
            ((App) v.getContext().getApplicationContext()).backend().submitFeedback(callRatingView.getRating(),selectedIssueList,commentTextView.getText().toString(),sendLogsBox.isChecked());
            dismiss();
        });

        skipFeedbackButton.setOnClickListener(v->dismiss());



        callRatingView.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Log.d(TAG, "onRatingChanged: "+rating);
            if(rating==5) {
                commentTextView.setVisibility(View.GONE);
                sendLogsBox.setVisibility(View.GONE);
                commentTextView.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                commentTextViewHead.setVisibility(View.GONE);
            }else{
                commentTextView.setVisibility(View.VISIBLE);
                sendLogsBox.setVisibility(View.VISIBLE);
                commentTextView.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
                commentTextViewHead.setVisibility(View.VISIBLE);
            }
        });


    }

    private void resetValues() {
        selectedIssueList.clear();
        commentTextView.setText("");
        sendLogsBox.setChecked(false);
    }


}
