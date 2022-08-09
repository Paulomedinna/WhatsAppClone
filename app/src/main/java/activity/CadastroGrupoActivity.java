package activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import adapter.GrupoSelecionadoAdapter;
import config.ConfiguracaoFirebase;
import de.hdodenhof.circleimageview.CircleImageView;
import helper.UsuarioFirebase;
import model.Grupo;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class CadastroGrupoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private final ArrayList<Usuario> listaMembrosSelecionados2 = new ArrayList<>();
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter2;
    private RecyclerView recyclerMembrosSelecionados2;
    private CircleImageView imagemGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(paulomedinna.app.whatsappclone.R.layout.activity_cadastro_grupo);
        toolbar = findViewById(R.id.toolbarCadastroGrupo);
        toolbar.setTitle("Novo Grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembrosSelecionados2 = findViewById(R.id.recyclerMembrosGrupo);
        fabSalvarGrupo = findViewById(R.id.fabSalvarGrupo);
        imagemGrupo = findViewById(R.id.imagemGrupo);
        editNomeGrupo = findViewById(R.id.editNomeGrupo);
        grupo = new Grupo();

        imagemGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) ;
                startActivityForResult(takePictureIntent, SELECAO_GALERIA);

            }
        });


        if (getIntent().getExtras() != null) {
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados2.addAll(membros);
            textTotalParticipantes.setText("Participantes : " + listaMembrosSelecionados2.size());
        }


        grupoSelecionadoAdapter2 = new GrupoSelecionadoAdapter(listaMembrosSelecionados2, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerMembrosSelecionados2.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados2.setHasFixedSize(true);
        recyclerMembrosSelecionados2.setAdapter(grupoSelecionadoAdapter2);


        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeGrupo = editNomeGrupo.getText().toString();
                listaMembrosSelecionados2.add(UsuarioFirebase.getUsuarioLogado());
                grupo.setMembros(listaMembrosSelecionados2);

                grupo.setNome(nomeGrupo);
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo);
                startActivity(i);
            }
        });


    }

    private void setSupportActionBar(Toolbar toolbar) {

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;


            try {
                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if (imagem != null){
                    imagemGrupo.setImageBitmap(imagem);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + "jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CadastroGrupoActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                   String url = task.getResult().toString();
                                   grupo.setFoto(url);

                                }
                            });

                        }
                    });
                }


            } catch (Exception e) {
                e.printStackTrace();

            }


        }
    }
}