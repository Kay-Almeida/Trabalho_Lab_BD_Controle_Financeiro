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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.DespesaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.ReceitaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Despesa;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Receita;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ValorInvalidoException;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.DespesaDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.ReceitaDao;

public class NovaEntradaFragment extends Fragment {
    private View view;
    private Button btnSalvarEntrada;
    private EditText etValorEntrada;
    private TextView textView, textView2;
    private RadioGroup radioGroup;
    private RadioButton rbDespesaHistorico, rbReceitaHistorico;

    private ReceitaController receitaController;
    private DespesaController despesaController;

    public NovaEntradaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nova_entrada, container, false);
        btnSalvarEntrada = view.findViewById(R.id.btnSalvarEntrada);
        etValorEntrada   = view.findViewById(R.id.etValorEntrada);
        textView         = view.findViewById(R.id.textView);
        textView2        = view.findViewById(R.id.textView2);
        radioGroup       = view.findViewById(R.id.radioGroup);
        rbDespesaHistorico = view.findViewById(R.id.rbDespesaHistorico);
        rbReceitaHistorico = view.findViewById(R.id.rbReceitaHistorico);

        receitaController = new ReceitaController(new ReceitaDao());
        despesaController = new DespesaController(new DespesaDao());

        btnSalvarEntrada.setOnClickListener(op -> salvar());
        return view;
    }

    private void salvar() {
        String valorEntrada = etValorEntrada.getText().toString();

        if (valorEntrada.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe um valor.", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor = Double.parseDouble(valorEntrada);
        boolean isReceita = rbReceitaHistorico.isChecked();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String mensagem;
                if (isReceita) {
                    Receita receita = new Receita(valor, new ArrayList<>());
                    receita.getData();
                    receitaController.inserir(receita);
                    mensagem = receita.toString();
                } else {
                    Despesa despesa = new Despesa(valor, new ArrayList<>());
                    despesa.getData();
                    despesaController.inserir(despesa);
                    mensagem = despesa.toString();
                }

                String finalMensagem = mensagem;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(view.getContext(), finalMensagem, Toast.LENGTH_LONG).show();
                    limpaCampos();
                });

            } catch (ValorInvalidoException | java.sql.SQLException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void limpaCampos() {
        etValorEntrada.setText("");
        radioGroup.clearCheck();
    }
}