package com.example.imc.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.imc.R;
import com.example.imc.helper.ConfiguracaoFirebase;
import com.example.imc.helper.Permissoes;
import com.example.imc.model.Anuncio;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnunciosActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText campoTitulo, campoDescricao, campoNome;
    private ImageView imagem1, imagem2, imagem3,imagem4, imagem5, imagem6,imagem7;
    private Spinner campoEstado, campoCategoria;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private Anuncio anuncio;
    private StorageReference storage;
    private AlertDialog dialog;
    private AlertDialog alerta;
    private int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0;
    private int soma = 0;
    private int listaConferi = 0;
    private int p = 0;
    private int n = 0;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private String[] listaFotosRecuperadas = new String[7];
    private List<String> listaURLFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncios);

        //Configurações iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();

    }

    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Salvando Anúncio")
                .setCancelable( false )
                .build();
        dialog.show();


        //Salvar imagem no Storage

        for (int i=0; i < listaFotosRecuperadas.length; i++){
            if(listaFotosRecuperadas[i]!= null){
                String urlImagem = listaFotosRecuperadas[i];
                soma++;
                int tamanhoLista = soma;
                salvarFotoStorage(urlImagem, tamanhoLista, i );
            }
        }

    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){

        //Criar nó no storage
        final StorageReference imagemAnuncio = storage
                .child("imagens")
                .child("anuncios")
                .child( anuncio.getIdAnuncio() )
                .child("imagem"+contador);

        //Fazer upload do arquivo
        final UploadTask uploadTask = imagemAnuncio.putFile( Uri.parse(urlString) );
       uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUrli) {

                        String urlConvertida = downloadUrli.toString();

                        listaURLFotos.add( urlConvertida );
                        if( totalFotos == listaURLFotos.size() ){
                            anuncio.setFotos( listaURLFotos );
                            anuncio.salvar();
                            dialog.dismiss();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        exibirMensagemErro("Falha ao fazer upload");
                        Log.i("INFO", "Falha ao fazer upload: " + e.getMessage());
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {

                    }
                });
            }
        });

    }

    private Anuncio configurarAnuncio(){

        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String nome = campoNome.getText().toString();
        String descricao = campoDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado( estado );
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone( telefone );
        anuncio.setNome(nome);
        anuncio.setDescricao(descricao);

        return anuncio;

    }

    public void validarDadosAnuncio(View view){

        anuncio = configurarAnuncio();
        String valor = String.valueOf(campoValor.getRawValue());

        if( listaConferi != 0 ){
            if( !anuncio.getEstado().isEmpty() ){
                if( !anuncio.getCategoria().isEmpty() ){
                    if( !anuncio.getTitulo().isEmpty() ){
                        if( !valor.isEmpty() && !valor.equals("0") ){
                            if( !anuncio.getDescricao().isEmpty() ){
                                if (!anuncio.getNome().isEmpty()){
                                    if( !anuncio.getTelefone().isEmpty()  ) {

                                        salvarAnuncio();
                                        conexaoRede();

                                    }else {
                                        exibirMensagemErro("Preencha o campo telefone");
                                    }
                                }else{
                                    exibirMensagemErro("Preencha o campo nome");
                                }

                            }else {
                                exibirMensagemErro("Preencha o campo descrição");
                            }

                        }else {
                            exibirMensagemErro("Preencha o campo valor");
                        }
                    }else {
                        exibirMensagemErro("Preencha o campo título");
                    }
                }else {
                    exibirMensagemErro("Preencha o campo categoria");
                }
            }else {
                exibirMensagemErro("Preencha o campo estado");
            }
        }else {
            exibirMensagemErro("Adicione no mínimo uma foto!");
        }

    }

    private void exibirMensagemErro(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        Log.d("onClick", "onClick: " + v.getId() );
        switch ( v.getId() ) {

            case R.id.imageCadastro1:

                if (a == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder.setTitle("Atenção");//define o titulo
                    builder.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {



                        }
                    });
                    //define um botão como negativo.
                    builder.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(1);
                        }
                    });
                    alerta = builder.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder.setTitle("Atenção");//define o titulo
                    builder.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem1.setImageDrawable(drawable);
                            listaFotosRecuperadas[0] = null;
                            listaConferi--;
                            a = 0;
                        }
                    });
                    //define um botão como negativo.
                    builder.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(1);

                        }
                    });
                    alerta = builder.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }
            case R.id.imageCadastro2:

                if (b == 0) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder2.setTitle("Atenção");//define o titulo
                    builder2.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder2.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg2, int arg3) {

                        }
                    });
                    //define um botão como negativo.
                    builder2.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg2, int arg3) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(2);

                        }
                    });
                    alerta = builder2.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else{
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder2.setTitle("Atenção");//define o titulo
                    builder2.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder2.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg2, int arg3) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem2.setImageDrawable(drawable);
                            listaFotosRecuperadas[1] = null;
                            listaConferi--;
                            b = 0;

                        }
                    });
                    //define um botão como negativo.
                    builder2.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg2, int arg3) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(2);

                        }
                    });
                    alerta = builder2.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }
            case R.id.imageCadastro3:

                if (c == 0) {
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder3.setTitle("Atenção");//define o titulo
                    builder3.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder3.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg4, int arg5) {

                        }
                    });
                    //define um botão como negativo.
                    builder3.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg4, int arg5) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(3);
                        }
                    });
                    alerta = builder3.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else{
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder3.setTitle("Atenção");//define o titulo
                    builder3.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder3.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg4, int arg5) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem3.setImageDrawable(drawable);
                            listaFotosRecuperadas[2] = null;
                            listaConferi--;
                            c = 0;

                        }
                    });
                    //define um botão como negativo.
                    builder3.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg4, int arg5) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(3);

                        }
                    });
                    alerta = builder3.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }

            case R.id.imageCadastro4:

                if (d == 0){
                    AlertDialog.Builder builder4 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder4.setTitle("Atenção");//define o titulo
                    builder4.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder4.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg6, int arg7) {

                        }
                    });
                    //define um botão como negativo.
                    builder4.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg6, int arg7) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(4);
                        }
                    });
                    alerta = builder4.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else {
                    AlertDialog.Builder builder4 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder4.setTitle("Atenção");//define o titulo
                    builder4.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder4.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg6, int arg7) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem4.setImageDrawable(drawable);
                            listaFotosRecuperadas[3] = null;
                            listaConferi--;
                            d = 0;
                        }
                    });
                    //define um botão como negativo.
                    builder4.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg6, int arg7) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(4);
                        }
                    });
                    alerta = builder4.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }
            case R.id.imageCadastro5:

                if (e == 0){
                    AlertDialog.Builder builder5 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder5.setTitle("Atenção");//define o titulo
                    builder5.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder5.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg8, int arg9) {

                        }
                    });
                    //define um botão como negativo.
                    builder5.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg8, int arg9) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(5);
                        }
                    });
                    alerta = builder5.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else {
                    AlertDialog.Builder builder5 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder5.setTitle("Atenção");//define o titulo
                    builder5.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder5.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg8, int arg9) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem5.setImageDrawable(drawable);
                            listaFotosRecuperadas[4] = null;
                            listaConferi--;
                            e = 0;
                        }
                    });
                    //define um botão como negativo.
                    builder5.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg8, int arg9) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(5);

                        }
                    });
                    alerta = builder5.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }
            case R.id.imageCadastro6:

                if (f == 0){
                    AlertDialog.Builder builder6 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder6.setTitle("Atenção");//define o titulo
                    builder6.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder6.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg10, int arg11) {

                        }
                    });
                    //define um botão como negativo.
                    builder6.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg10, int arg11) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(6);
                        }
                    });
                    alerta = builder6.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }else {
                    AlertDialog.Builder builder6 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder6.setTitle("Atenção");//define o titulo
                    builder6.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder6.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg10, int arg11) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem6.setImageDrawable(drawable);
                            listaFotosRecuperadas[5] = null;
                            listaConferi--;
                            f = 0;

                        }
                    });
                    //define um botão como negativo.
                    builder6.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg10, int arg11) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(6);

                        }
                    });
                    alerta = builder6.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }
            case R.id.imageCadastro7:

                if (g == 0) {
                    AlertDialog.Builder builder7 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder7.setTitle("Atenção");//define o titulo
                    builder7.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder7.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg12, int arg13) {

                        }
                    });
                    //define um botão como negativo.
                    builder7.setNegativeButton("Escolher imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg12, int arg13) {
                            Log.d("onClick", "onClick: ");
                            escolherImagem(7);
                        }
                    });
                    alerta = builder7.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                } else {
                    AlertDialog.Builder builder7 = new AlertDialog.Builder(CadastrarAnunciosActivity.this);//Cria o gerador do AlertDialog
                    builder7.setTitle("Atenção");//define o titulo
                    builder7.setMessage("O que você deseja fazer?");//define a mensagem
                    //define um botão como positivo
                    builder7.setPositiveButton("Remover", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg12, int arg13) {
                            Drawable drawable= getResources().getDrawable(R.drawable.padrao);
                            imagem7.setImageDrawable(drawable);
                            listaFotosRecuperadas[6] = null;
                            listaConferi--;
                            g = 0;
                        }
                    });
                    //define um botão como negativo.
                    builder7.setNegativeButton("Trocar a imagem", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg12, int arg13) {

                            Log.d("onClick", "onClick: ");
                            escolherImagem(7);
                        }
                    });
                    alerta = builder7.create();//cria o AlertDialog
                    alerta.show();//Exibe

                    break;
                }

        }
    }

    public void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK ) {

            //Recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //Configura imagem no ImageView
            if (requestCode == 1) {
                if (a == 0) {
                    imagem1.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[0] = caminhoImagem;
                    a = 1;
                    listaConferi += 1;
                } else {
                    imagem1.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[0] = caminhoImagem;

                }
            } else if (requestCode == 2) {
                if (b == 0) {
                    imagem2.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[1] = caminhoImagem;
                    b = 1;
                    listaConferi += 1;
                } else {
                    imagem2.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[1] = caminhoImagem;

                }
            } else if (requestCode == 3) {
                if (c == 0) {
                    imagem3.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[2] = caminhoImagem;
                    c = 1;
                    listaConferi += 1;
                } else {
                    imagem3.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[2] = caminhoImagem;

                }
            } else if (requestCode == 4) {
                if (d == 0) {
                    imagem4.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[3] = caminhoImagem;
                    d = 1;
                    listaConferi += 1;
                } else {
                    imagem4.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[3] = caminhoImagem;

                }
            } else if (requestCode == 5) {
                if (e == 0) {
                    imagem5.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[4] = caminhoImagem;
                    e = 1;
                    listaConferi += 1;
                } else {
                    imagem5.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[4] = caminhoImagem;

                }
            } else if (requestCode == 6) {
                if (f == 0) {
                    imagem6.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[5] = caminhoImagem;
                    f = 1;
                    listaConferi += 1;
                } else {
                    imagem6.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[5] = caminhoImagem;

                }
            } else if (requestCode == 7) {
                if (g == 0) {
                    imagem7.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[6] = caminhoImagem;
                    g = 1;
                    listaConferi += 1;
                } else {
                    imagem7.setImageURI(imagemSelecionada);
                    listaFotosRecuperadas[6] = caminhoImagem;

                }
            }
        }

    }

    private void carregarDadosSpinner(){

        //Configura spinner de estados
        String[] estados = getResources().getStringArray(R.array.Cursos);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoEstado.setAdapter( adapter );

        //Configura spinner de categorias
        String[] categorias = getResources().getStringArray(R.array.Categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoCategoria.setAdapter( adapterCategoria );


    }
    public void conexaoRede(){

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new DownloadDado().execute();
            }
        }).start();
    }

    public class DownloadDado extends AsyncTask<String,Void,Boolean > {

        @Override
        protected Boolean doInBackground(String... params) {

            Runtime runtime = Runtime.getRuntime();
            try {
                Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 www.google.com");
                int mExitValue = mIpAddrProcess.waitFor();

                if (mExitValue == 0) {

                    return true;

                }else{

                    return false;
                }

            } catch (InterruptedException ignore) {
                ignore.printStackTrace();
                System.out.println(" Exception:" + ignore);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(" Exception:" + e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean dados) {
            // O resultado da execução em background é passado para este passo como um parâmetro.
            if (dados == true) {

                if (p != 5) {
                    conexaoRede();
                }
                p++;
            }else {

                if (n != 5) {
                    conexaoRede();
                }else{
                    Toast.makeText(getApplicationContext(), "Problema com sua internet!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                n++;
            }

        }
    }

    private void inicializarComponentes(){

        campoTitulo = findViewById(R.id.editTitulo);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoNome = findViewById(R.id.editNome);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem4 = findViewById(R.id.imageCadastro4);
        imagem5 = findViewById(R.id.imageCadastro5);
        imagem6 = findViewById(R.id.imageCadastro6);
        imagem7 = findViewById(R.id.imageCadastro7);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
        imagem4.setOnClickListener(this);
        imagem5.setOnClickListener(this);
        imagem6.setOnClickListener(this);
        imagem7.setOnClickListener(this);

        //Configura localidade para pt -> portugues BR -> Brasil
        Locale locale = new Locale("pt", "BR");
        campoValor.setLocale( locale );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults ){
            if( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}

