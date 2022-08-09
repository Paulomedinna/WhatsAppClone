package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import activity.ChatActivity;
import adapter.ConversasAdapter;
import config.ConfiguracaoFirebase;
import helper.RecyclerItemClickListener;
import helper.UsuarioFirebase;
import model.Conversa;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas;
    private List<Conversa> listaConversa = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;


    public ConversasFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas);

        adapter = new ConversasAdapter(listaConversa, getActivity());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager(layoutManager);
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter(adapter);

        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(), recyclerViewConversas, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        List<Conversa> listaConversasAtualizada = adapter.getConversas();
                        Conversa conversaSelecionada = listaConversasAtualizada.get(position);


                        if (conversaSelecionada.getIsGroup().equals("true")){

                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                            startActivity(i);

                        }else {
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                            startActivity(i);
                        }

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                })
        );

        String identificadorUsuaio = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas").child(identificadorUsuaio);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    public void pesquisarConversas(String texto) {
        // Log.d("pesquisa", texto);

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa : listaConversa) {

            if (conversa.getUsuarioExibicao() != null){
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase(Locale.ROOT);
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase(Locale.ROOT);

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }

            }else {
                String nome = conversa.getGrupo().getNome().toLowerCase(Locale.ROOT);
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase(Locale.ROOT);

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }

            }


        }

        adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        recyclerViewConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recuperarConversas() {
        listaConversa.clear();
        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot datasnapshot, String previousChildName) {

                Conversa conversa = datasnapshot.getValue(Conversa.class);
                listaConversa.add(conversa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }
}