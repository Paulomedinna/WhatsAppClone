package activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Base64;

import config.ConfiguracaoFirebase;
import helper.Base64Custom;
import helper.UsuarioFirebase;
import model.Usuario;
import paulomedinna.app.whatsappclone.R;

public class Activity_Cadastro extends AppCompatActivity {

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editnome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void cadastroUsuario(Usuario usuario) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(Activity_Cadastro.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    finish();

                    try {

                        String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                        usuario.setUid(identificadorUsuario);
                        usuario.salvar();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        excecao = "Digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Digite um e-mail válido!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excecao = "Essa conta já foi cadastrada!";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar usuário!" + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(Activity_Cadastro.this, excecao, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    public void validarCadastrarUsuario(View view) {
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoNome.isEmpty()) {
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {

                    Usuario usuario = new Usuario();
                    usuario.setNome(textoNome);
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    cadastroUsuario(usuario);


                } else {
                    Toast.makeText(Activity_Cadastro.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();

                }


            } else {
                Toast.makeText(Activity_Cadastro.this, "Preencha o email!", Toast.LENGTH_SHORT).show();

            }


        } else {
            Toast.makeText(Activity_Cadastro.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();

        }
    }


}