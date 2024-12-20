package com.example.quizy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Result extends AppCompatActivity {
    private Question[] data;
    private String uid;
    private String quizID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);

        quizID = getIntent().getStringExtra("Quiz ID");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getIntent().hasExtra("User UID")) uid = getIntent().getStringExtra("User UID");

        TextView title = findViewById(R.id.title);
        ListView listView = findViewById(R.id.listview);
        TextView total = findViewById(R.id.total);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Quizzes").hasChild(quizID)){
                    DataSnapshot ansRef = snapshot.child("Quizzes").child(quizID).child("Answers").child(uid);
                    DataSnapshot qRef = snapshot.child("Quizzes").child(quizID);
                    title.setText(qRef.child("Title").getValue().toString());
                    int num = Integer.parseInt(qRef.child("Total Questions").getValue().toString());
                    data = new Question[num];
                    int correctAns = 0;
                    for (int i=0; i<num; i++){
                        DataSnapshot q = qRef.child("Questions").child(String.valueOf(i));
                        Question question = new Question();
                        question.setQuestion(q.child("Question").getValue().toString());
                        question.setOption1(q.child("Option 1").getValue().toString());
                        question.setOption2(q.child("Option 2").getValue().toString());
                        question.setOption3(q.child("Option 3").getValue().toString());
                        question.setOption4(q.child("Option 4").getValue().toString());
                        question.setSeletedAnswer(Integer.parseInt(ansRef.child(String.valueOf((i+1))).getValue().toString()));
                        int ans = Integer.parseInt(q.child("Ans").getValue().toString());
                        if( ans == question.getSeletedAnswer() ) correctAns++;
                        question.setCorrectAnswer(ans);
                        data[i] = question;
                    }
                    total.setText("Total: " + correctAns + "/" + data.length);
                    ListAdapter adapter = new ListAdapter(data);
                    listView.setAdapter(adapter);
                }else{
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Result.this, "Can't Connect", Toast.LENGTH_SHORT).show();
            }
        };
        database.addListenerForSingleValueEvent(listener);
    }

    public class ListAdapter extends BaseAdapter {
        Question[] arr;
        ListAdapter(Question[] arr2){
            arr = arr2;
        }

        @Override
        public int getCount() {
            return arr.length;
        }

        @Override
        public Object getItem(int position) {
            return arr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.question, null);
            TextView question = view.findViewById(R.id.question);
            RadioButton option1 = view.findViewById(R.id.option1);
            RadioButton option2 = view.findViewById(R.id.option2);
            RadioButton option3 = view.findViewById(R.id.option3);
            RadioButton option4 = view.findViewById(R.id.option4);

            TextView result = view.findViewById(R.id.result);

            question.setText(data[position].getQuestion());
            option1.setText(data[position].getOption1());
            option2.setText(data[position].getOption2());
            option3.setText(data[position].getOption3());
            option4.setText(data[position].getOption4());

            switch (data[position].getSeletedAnswer()){
                case 1:
                    option1.setChecked(true);
                    break;
                case 2:
                    option2.setChecked(true);
                    break;
                case 3:
                    option3.setChecked(true);
                    break;
                case 4:
                    option4.setChecked(true);
                    break;
            }

            option1.setEnabled(false);
            option2.setEnabled(false);
            option3.setEnabled(false);
            option4.setEnabled(false);

            result.setVisibility(View.VISIBLE);

            if(data[position].getSeletedAnswer() == data[position].getCorrectAnswer()) {
                result.setBackgroundResource(R.drawable.green_background);
                result.setTextColor(ContextCompat.getColor(Result.this, R.color.green_dark));
                result.setText("Correct Answer");
            }else{
                result.setBackgroundResource(R.drawable.red_background);
                result.setTextColor(ContextCompat.getColor(Result.this, R.color.red_dark));
                result.setText("Wrong Answer");

                switch (data[position].getCorrectAnswer()){
                    case 1:
                        option1.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 2:
                        option2.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 3:
                        option3.setBackgroundResource(R.drawable.green_background);
                        break;
                    case 4:
                        option4.setBackgroundResource(R.drawable.green_background);
                        break;
                }
            }

            return view;
        }
    }
}