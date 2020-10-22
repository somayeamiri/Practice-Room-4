package com.example.roomjava4;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Button btn_download;
    String DATABASE_NAME = "endb.db";
    String FILE = "/data/data/com.example.roomjava4/databases/";


    EditText word;
    Button btn;
    TextView show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_download=findViewById(R.id.btn_downlaod);
        word=findViewById(R.id.edt_word);
        btn=findViewById(R.id.btn);
        show=findViewById(R.id.edt_show);

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(FILE + DATABASE_NAME);
                if (!file.exists()) {

                    Intent intentService=new Intent(MainActivity.this,DownloadServices.class);
                    intentService.putExtra("URL","http://195.248.242.73/androidteam/p1/database.zip");
                    startService(intentService);


                } else {
                    Toast.makeText(MainActivity.this, "Database downloaded before", Toast.LENGTH_SHORT).show();
                }


            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //query to database
                readData(word.getText().toString());


            }
        });



    }
    private void readData(String sample){

        WordRoomDatabase.getDatabase(MainActivity.this, DATABASE_NAME).wordDao().GetWord(sample) // observable
                .subscribeOn(Schedulers.io()) // observable where done
                .observeOn(AndroidSchedulers.mainThread()) // result where show
                .subscribe(new DisposableSingleObserver<Dic>() {
                    @Override
                    public void onSuccess(Dic dic) {

                        show.setText(dic.translate);
//                        Toast.makeText(MainActivity.this, dic.translate, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}