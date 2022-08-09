package activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import adapter.MensagensAdapter;
import config.ConfiguracaoFirebase;
import de.hdodenhof.circleimageview.CircleImageView;
import helper.Base64Custom;
import helper.UsuarioFirebase;
import model.Conversa;
import model.Grupo;
import model.Mensagem;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNomechat;
    private CircleImageView circleImageFotoChat;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private EditText editMensagem;
    private ImageView imageCamera;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenermensagens;
    private StorageReference storage;
    private Grupo grupo;

    private static final int SELECAO_CAMERA = 100;

    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();

    private ChildEventListener childEventListenerMensagens;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(paulomedinna.app.whatsappclone.R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbarChat);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        textViewNomechat = findViewById(R.id.textViewNomeChat);
        circleImageFotoChat = findViewById(R.id.circleImageFotoChat);
        imageCamera = findViewById(R.id.imageCamera);

        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getUsuarioLogado();



        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("chatGrupo")) {

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNomechat.setText(grupo.getNome());

                String foto = grupo.getFoto();
                if (foto != null) {
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this).load(url).into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

            } else {
                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNomechat.setText(usuarioDestinatario.getNome());

                String foto = usuarioDestinatario.getFoto();
                if (foto != null) {
                    Uri url = Uri.parse(usuarioDestinatario.getFoto());
                    Glide.with(ChatActivity.this).load(url).into(circleImageFotoChat);
                } else {
                    circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());

            }


        }

        adapter = new MensagensAdapter(mensagens, this.getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens").child(idUsuarioRemetente).child(idUsuarioDestinatario);


        imageCamera.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imagem = null;

            try {
                switch (requestCode) {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }
                if (imagem != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    String nomeImagem = UUID.randomUUID().toString();

                    StorageReference imagemRef = storage.child("imagens").child("fotos").child(idUsuarioRemetente).child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.addOnFailureListener(e -> {
                        Log.d("Erro", "Erro ao fazer upload");
                        Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem!", Toast.LENGTH_SHORT).show();

                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> uriTask = imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                                @Override
                                public void onComplete(Task<Uri> task) {
                                    Uri url = task.getResult();
                                    String dowloadUrl = task.getResult().toString();

                                    if (usuarioDestinatario!= null){
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(dowloadUrl);

                                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                                    }else {

                                        for (Usuario membro : grupo.getMembros()) {

                                            String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setImagem(dowloadUrl);

                                            salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                            salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);


                                            Toast.makeText(ChatActivity.this, "Sucesso ao enviar imagem", Toast.LENGTH_SHORT).show();




                                        }

                                    }

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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, SELECAO_CAMERA);

    }


    private void setSupportActionBar(Toolbar toolbar) {
    }

    public void enviarMensagem(View view) {

        String textoMensagem = editMensagem.getText().toString();
        if (!textoMensagem.isEmpty()) {
            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario(idUsuarioRemetente);
            mensagem.setMensagem(textoMensagem);

            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

            salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);

            salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

            if (usuarioDestinatario != null) {

            } else {

                for (Usuario membro : grupo.getMembros()) {

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem msg = new Mensagem();
                    msg.setIdUsuario(idUsuarioLogadoGrupo);
                    msg.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                }


            }

        } else {
            Toast.makeText(this, "Digite uma mensagem para enviar!", Toast.LENGTH_SHORT).show();
        }

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem msg, boolean isGroup) {


        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());
        if (isGroup) {
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        } else {
            conversaRemetente.setIsGroup("false");
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);

        }
        conversaRemetente.salvar();
    }
    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg) {

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente).child(idDestinatario).push().setValue(msg);

        editMensagem.setText("");

    }


    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens() {

        mensagens.clear();
        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}