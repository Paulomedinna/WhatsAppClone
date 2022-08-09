package activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.Locale;

import config.ConfiguracaoFirebase;
import fragment.ContatosFragment;
import fragment.ConversasFragment;
import paulomedinna.app.whatsappclone.R;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private FragmentPagerItemAdapter adapter;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();


        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("WhatsApp");
        setSupportActionBar(toolbar);


        adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );


        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                switch (viewPager.getCurrentItem()) {
                    case 0:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        conversasFragment.pesquisarConversas(newText.toLowerCase(Locale.ROOT));
                        break;
                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        contatosFragment.pesquisarContatos(newText.toLowerCase(Locale.ROOT));
                        break;

                }
                return true;

            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menusair:
                deslogarUsuario();
                finish();
                break;
            case R.id.menuConfiguracoes:
                abrirConfigurações();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario() {

        try {
            autenticacao.signOut();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void abrirConfigurações() {
        Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);
        startActivity(intent);

    }


}