package kailaine.mobile.trabalho_semestral_android_controle_financeiro;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.MetaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.MetaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.MetaFinanceiraDao;

public class NovaMetaFragment extends Fragment {
    private View view;
    private Button btnSalvarMeta;
    private EditText etNomeMeta, etValorMeta;
    private TextView tvTituloMeta;

    private MetaController metaController;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NovaMetaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nova_meta, container, false);
        btnSalvarMeta = view.findViewById(R.id.btnSalvarMeta);
        etNomeMeta    = view.findViewById(R.id.etNomeMeta);
        etValorMeta   = view.findViewById(R.id.etValorMeta);
        tvTituloMeta  = view.findViewById(R.id.tvTituloMeta);

        metaController = new MetaController(new MetaFinanceiraDao());

        btnSalvarMeta.setOnClickListener(op -> salvarMeta());

        return view;
    }

    private void salvarMeta() {
        String nomeMeta  = etNomeMeta.getText().toString();
        String valorMeta = etValorMeta.getText().toString();

        if (nomeMeta.isEmpty()) {
            Toast.makeText(view.getContext(), "O nome da meta não pode ser vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (valorMeta.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe o valor da meta.", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor = Double.parseDouble(valorMeta);
        MetaFinanceira metaFinanceira = new MetaFinanceira(nomeMeta, valor);

        executor.execute(() -> {
            try {
                metaController.inserir(metaFinanceira);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(view.getContext(), metaFinanceira.toString(), Toast.LENGTH_LONG).show();
                    limpaCampos();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao salvar meta: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void limpaCampos() {
        etNomeMeta.setText("");
        etValorMeta.setText("");
    }
}