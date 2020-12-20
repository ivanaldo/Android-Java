package com.example.imc.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.imc.R;

public class SobreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
    }

    public void visualizarTelefoneEmpresa(View view){
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "75998185594", null ));
        startActivity( i );
    }

    public void whatsappEmpresa(View view) {

        String contact = +55 + "75998185594"; // use country code with your phone number
        String url = "https://api.whatsapp.com/send?phone=" + contact;
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Esse número não está no Whatsapp!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }


    }
}
