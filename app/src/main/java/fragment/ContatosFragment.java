package fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import activity.ChatActivity;
import activity.GrupoActivity;
import adapter.ContatosAdapter;
import config.ConfiguracaoFirebase;
import helper.RecyclerItemClickListener;
import helper.UsuarioFirebase;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;


public class ContatosFragment extends Fragment {

    public RecyclerView recyclerViewListaContatos;
    public ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;


    public ContatosFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contato, container, false);


        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioatual();

        adapter = new ContatosAdapter(listaContatos, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContatos.setLayoutManager(layoutManager);
        recyclerViewListaContatos.setHasFixedSize(true);
        recyclerViewListaContatos.setAdapter(adapter);

        recyclerViewListaContatos.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerViewListaContatos, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        List<Usuario> listaUsuarioAtualizada = adapter.getContatos();

                        Usuario usuarioSelecionado = listaUsuarioAtualizada.get(position);
                        boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                        if (cabecalho) {
                            Intent i = new Intent(getActivity(), GrupoActivity.class);
                            startActivity(i);


                        } else {
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatContato", usuarioSelecionado);
                            startActivity(i);
                        }

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


        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setEmail("");

        listaContatos.add(itemGrupo);


        return view;


    }


    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();

    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContatos);
    }


    public void recuperarContatos() {
        listaContatos.clear();


        valueEventListenerContatos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot datasnapshot) {

                Usuario itemGrupo = new Usuario();
                itemGrupo.setNome("Novo Grupo");
                itemGrupo.setEmail("");
                listaContatos.add(itemGrupo);


                for (DataSnapshot dados : datasnapshot.getChildren()) {

                    //TODO erro aqui
                    Usuario usuario = dados.getValue(Usuario.class);
                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if (!emailUsuarioAtual.equals(usuario.getEmail())) {

                        listaContatos.add(usuario);

                    }

                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


    public void pesquisarContatos(String texto) {

        // Log.d("pesquisa", texto);

        List<Usuario> listaContatosBusca = new ArrayList<>();

        for (Usuario usuario : listaContatos) {

            String nome = usuario.getNome().toLowerCase(Locale.ROOT);
            if (nome.contains(texto)) {
                listaContatosBusca.add(usuario);
        }
    }
    adapter =new ContatosAdapter(listaContatosBusca, getActivity());
        recyclerViewListaContatos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
}
}