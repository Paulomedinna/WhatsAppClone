package activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import config.ConfiguracaoFirebase;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        campoEmail = findViewById(R.id.editEmail2);
        campoSenha = findViewById(R.id.editSenha2);
    }


    public void logarUsuario(Usuario usuario) {

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                abrirTelaPrincipal();
            } else {

                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                } catch (FirebaseAuthInvalidCredentialsException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(LoginActivity.this, "Erro ao autenticar usuário!", Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void validarAutenticacaoUsuario(View view) {

        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();


        if (!textoEmail.isEmpty()) {
            if (!textoSenha.isEmpty()) {

                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);

            } else {
                Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();

            }
        } else {
            Toast.makeText(LoginActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual != null) {
            abrirTelaPrincipal();

        }
    }

    public void abrirTelaCadastro(View view) {
        Intent intent = new Intent(LoginActivity.this, Activity_Cadastro.class);
        startActivity(intent);

    }

    public void abrirTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }
}