package com.example.quizy;

import android.content.Intent;
import android.hardware.DataSpace;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Exam extends AppCompatActivity {

    private Question[] data;
    private String quizID;
    private String uid;
    private int oldTotalPoints = 0;
    private int oldTotalQuestions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exam);

        quizID = getIntent().getStringExtra("Quiz ID");

        ListView listView = findViewById(R.id.listview);
        Button submit = findViewById(R.id.submit);
        TextView title = findViewById(R.id.title);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if( snapshot.child("Quizzes").hasChild(quizID) ) {
                    DataSnapshot ref = snapshot.child("Quizzes").child(quizID);
                    title.setText(ref.child("Title").getValue().toString());
                    int num = Integer.parseInt(ref.child("Total Questions").getValue().toString());
                    data = new Question[num];
                    for(int i=0; i<num; i++){
                        DataSnapshot qRef = ref.child("Questions").child(String.valueOf(i));
                        Question question = new Question();
                        question.setQuestion(qRef.child("Question").getValue().toString());
                        question.setOption1(qRef.child("Option 1").getValue().toString());
                        question.setOption2(qRef.child("Option 2").getValue().toString());
                        question.setOption3(qRef.child("Option 3").getValue().toString());
                        question.setOption4(qRef.child("Option 4").getValue().toString());
                        int ans = Integer.parseInt(qRef.child("Ans").getValue().toString());
                        question.setCorrectAnswer(ans);
                        data[i] = question;
                    }
                    ListAdapter listAdapter = new ListAdapter(data);
                    listView.setAdapter(listAdapter);
                    DataSnapshot ref2 = snapshot.child("Users").child(uid);
                    if(ref2.hasChild("Quizzes")){
                        oldTotalPoints = Integer.parseInt(ref2.child("Total Points").getValue().toString());
                    }
                    if(ref2.hasChild("Total Questions")){
                        oldTotalQuestions = Integer.parseInt(ref2.child("Total Questions").getValue().toString());
                    }
                }else{
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Exam.this, "Can't Connect", Toast.LENGTH_SHORT).show();
            }
        };

        database.addValueEventListener(listener);

        submit.setOnClickListener(view -> {
            DatabaseReference ref = database.child("Quizzes").child(quizID).child("Answers").child(uid);
            int totalPoints = oldTotalPoints;
            int points = 0;
            for(int i=0; i<data.length; i++){
                ref.child(String.valueOf((i+1))).setValue(data[i].getSeletedAnswer());
                if(data[i].getSeletedAnswer() == data[i].getCorrectAnswer()){
                    points++;
                    totalPoints++;
                }
            }
            ref.child("Points").setValue(points);
            int totalquestions = oldTotalQuestions + data.length;
            database.child("Users").child(uid).child("Total Points").setValue(totalPoints);
            database.child("Users").child(uid).child("Total Questions").setValue(totalquestions);
            database.child("Users").child(uid).child("Quizzes Solved").child(quizID).setValue("");

            Intent i = new Intent(Exam.this, Result.class);
            i.putExtra("Quiz ID", quizID);
            startActivity(i);
            finish();
        });
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

            question.setText(data[position].getQuestion());
            option1.setText(data[position].getOption1());
            option2.setText(data[position].getOption2());
            option3.setText(data[position].getOption3());
            option4.setText(data[position].getOption4());

            option1.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) data[position].setSeletedAnswer(1);
            });

            option2.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) data[position].setSeletedAnswer(2);
            });

            option3.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) data[position].setSeletedAnswer(3);
            });

            option4.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked) data[position].setSeletedAnswer(4);
            });

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


            return view;
        }
    }
}