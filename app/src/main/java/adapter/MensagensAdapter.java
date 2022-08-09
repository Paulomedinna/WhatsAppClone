package adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import helper.UsuarioFirebase;
import model.Mensagem;
import paulomedinna.app.whatsappclone.R;

public class MensagensAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Mensagem> mensagens;
    private final Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;


    public MensagensAdapter(List<Mensagem> mensagens, Context context) {
        this.mensagens = mensagens;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getViewByType(parent, viewType);
    }

    private RecyclerView.ViewHolder getViewByType(ViewGroup parent, int viewType) {
        if (viewType == TIPO_REMETENTE)
            return new MyViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.adapter_mensagem_remetente
                            , parent, false)
            );
        else
            return new MyViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.adapter_mensagem_destinatario, parent, false)
            );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);
        MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.bindViewHolder(viewHolder, mensagem, context);
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {
        Mensagem mensagem = mensagens.get(position);
        String idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        if (idUsuario.equals(mensagem.getIdUsuario())) return TIPO_REMETENTE;
        else return TIPO_DESTINATARIO;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mensagem;
        TextView nome;
        ImageView imagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            imagem = itemView.findViewById(R.id.imageMensagemFoto);
            nome = itemView.findViewById(R.id.textNomeExibicao);
        }

        public void bindViewHolder(MyViewHolder holder, Mensagem mensagem, Context context) {
            String msg = mensagem.getMensagem();
            String imagem = mensagem.getImagem();

            if (imagem != null) {
                Uri url = Uri.parse(imagem);
                Glide.with(context).load(url).into(holder.imagem);

                String nome = mensagem.getNome();
                if (!nome.isEmpty()) {
                    holder.nome.setText(nome);
                } else {
                    holder.nome.setVisibility(View.GONE);
                }

                holder.imagem.setVisibility(View.VISIBLE);
                holder.mensagem.setVisibility(View.GONE);
            } else {
                String nome = mensagem.getNome();
                if (!nome.isEmpty()) {
                    holder.nome.setText(nome);
                } else {
                    holder.nome.setVisibility(View.GONE);
                }
                holder.imagem.setVisibility(View.GONE);
                holder.mensagem.setText(msg);
                holder.mensagem.setVisibility(View.VISIBLE);

            }
        }
    }
}
