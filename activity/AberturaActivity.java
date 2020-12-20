package com.example.imc.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.imc.R;

public class AberturaActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

    }
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abertura);

        new Handler().postDelayed(new Runnable() {
            /*
             * Exibindo splash com um timer.
             */
            @Override
            public void run() {
                // Esse método será executado sempre que o timer acabar
                // E inicia a activity principal
                Intent i = new Intent(AberturaActivity.this, AnunciosActivity.class);
                startActivity(i);

                // Fecha esta activity
                finish();


            }
        }, SPLASH_TIME_OUT);
    }
}