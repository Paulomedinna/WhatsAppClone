package activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import adapter.ContatosAdapter;
import adapter.GrupoSelecionadoAdapter;
import config.ConfiguracaoFirebase;
import helper.RecyclerItemClickListener;
import helper.UsuarioFirebase;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class GrupoActivity extends AppCompatActivity {


    private RecyclerView recyclerMembrosSelecionados;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private final ArrayList<Usuario> listaMembros = new ArrayList<>();
    private final ArrayList<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuariosRef;
    private FirebaseUser usuarioAtual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;

    private void setSupportActionBar(Toolbar toolbar) {
    }

    public void atualizarMembrosToolbar() {


        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;
        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados ");



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbarGrupo);
        setSupportActionBar(toolbar);


        RecyclerView recyclerMembros = findViewById(R.id.recyclerMembros);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        fabAvancarCadastro = findViewById(R.id.fabAvancarCadastro);

        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioatual();

        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contatosAdapter);


        recyclerMembros.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembros,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembros.get(position);
                                listaMembros.remove(usuarioSelecionado);
                                listaMembrosSelecionados.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )

        );

        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros", (Serializable) listaMembrosSelecionados);
                startActivity(i);

            }
        });

        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros", (Serializable) listaMembrosSelecionados);
                startActivity(i);
            }
        });

    }

    public void recuperarContatos() {


        valueEventListenerMembros = usuariosRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for (DataSnapshot dados : datasnapshot.getChildren()) {

                    Usuario usuario = dados.getValue(Usuario.class);
                    String emailUsuarioAtual = usuarioAtual.getEmail();

                    assert emailUsuarioAtual != null;
                    assert usuario != null;
                    if (!emailUsuarioAtual.equals(usuario.getEmail())) {
                        listaMembros.add(usuario);

                    }
                }

                contatosAdapter.notifyDataSetChanged();
                atualizarMembrosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);


        recyclerMembrosSelecionados.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerMembrosSelecionados,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onItemClick(View view, int position) {

                                Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);
                                listaMembrosSelecionados.remove(usuarioSelecionado);
                                grupoSelecionadoAdapter.notifyDataSetChanged();

                                listaMembros.add(usuarioSelecionado);
                                contatosAdapter.notifyDataSetChanged();

                                atualizarMembrosToolbar();

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );


    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerMembros);
    }


}

