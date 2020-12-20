package com.example.imc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.imc.adapter.AdapterAnuncios;
import com.example.imc.helper.ConfiguracaoFirebase;
import com.example.imc.helper.RecyclerItemClickListener;
import com.example.imc.model.Anuncio;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.imc.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeusAnunciosActivity extends AppCompatActivity {

    private RecyclerView recyclerAnuncios;
    private List<Anuncio> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anuncioUsuarioRef;
    private AlertDialog dialog;
    private AlertDialog alerta;
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        //Configurações iniciais
        anuncioUsuarioRef = ConfiguracaoFirebase.getFirebase()
                .child("meus_anuncios")
                .child( ConfiguracaoFirebase.getIdUsuario() );

        inicializarComponentes();

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CadastrarAnunciosActivity.class));
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurar RecyclerView
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerAnuncios.setAdapter( adapterAnuncios );

        progressbar = findViewById(R.id.progressBarMeusAnuncios);
        progressbar.setVisibility(View.GONE);

        //Recupera anúncios para o usuário
        recuperarAnuncios();
        new recuperarAnuncio().execute();

        //Adiciona evento de clique no recyclerview
        recyclerAnuncios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, final int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, final int position) {
                                // alerta
                                AlertDialog.Builder builder = new AlertDialog.Builder(MeusAnunciosActivity.this);//Cria o gerador do AlertDialog
                                builder.setTitle("Atenção");//define o titulo
                                builder.setMessage("Deseja apagar esse anúncio?");//define a mensagem
                                //define um botão como positivo
                                builder.setPositiveButton("SIM", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Anuncio anuncioSelecionado = anuncios.get(position);
                                        anuncioSelecionado.remover();
                                        adapterAnuncios.notifyDataSetChanged();

                                        Toast.makeText(MeusAnunciosActivity.this, "Anúncio apagado com sucesso", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //define um botão como negativo.
                                builder.setNegativeButton("NÃO", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Toast.makeText(MeusAnunciosActivity.this, "Você clicou em não apagar o anúncio", Toast.LENGTH_SHORT).show();

                                    }
                                });
                                alerta = builder.create();//cria o AlertDialog
                                alerta.show();//Exibe

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    public class recuperarAnuncio extends AsyncTask<String,Void,Boolean > {

        @Override
        protected void onPreExecute() {

            progressbar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(String... params) {

            int i = 0;
            while (i < 10 ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
                    int mExitValue = mIpAddrProcess.waitFor();

                    if (mExitValue == 0) {

                        return true;

                    }

                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                    System.out.println(" Exception:" + ignore);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(" Exception:" + e);
                }

            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean dados) {
            // O resultado da execução em background é passado para este passo como um parâmetro.
            if (dados == true) {

                progressbar.setVisibility(View.GONE);
            } else {

                progressbar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Problema com sua internet, verifique e tente novamente!", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void recuperarAnuncios(){

        anuncioUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                anuncios.clear();
                for ( DataSnapshot ds : dataSnapshot.getChildren() ){
                    anuncios.add( ds.getValue(Anuncio.class) );
                }

                Collections.reverse( anuncios );
                adapterAnuncios.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void inicializarComponentes(){

        recyclerAnuncios = findViewById(R.id.recyclerMeusAnuncios);

    }
    @Override
    public void onBackPressed() {
        Intent i = new Intent(MeusAnunciosActivity.this, AnunciosActivity.class);
        startActivity(i);
    }

}


