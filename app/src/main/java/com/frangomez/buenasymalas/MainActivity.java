package com.frangomez.buenasymalas;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Unica Activity de la app: todas las pantallas son destinos del nav graph. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
