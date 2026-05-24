package kailaine.mobile.trabalho_semestral_android_controle_financeiro;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.DespesaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.ReceitaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Despesa;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Receita;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.DespesaDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.ReceitaDao;

public class InicioFragment extends Fragment {
    private View view;
    private TextView tvTotalDespesaInicio, tvReceitaInicio, tvDespesaInicio, tvTotalReceitaInicio, tvTitulo, tvTitulo2;

    private ReceitaController receitaController;
    private DespesaController despesaController;

    public InicioFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inicio, container, false);
        tvTotalDespesaInicio = view.findViewById(R.id.tvTotalDespesaInicio);
        tvReceitaInicio      = view.findViewById(R.id.tvReceitaInicio);
        tvDespesaInicio      = view.findViewById(R.id.tvDespesaInicio);
        tvTotalReceitaInicio = view.findViewById(R.id.tvTotalReceitaInicio);
        tvTitulo             = view.findViewById(R.id.tvTitulo);
        tvTitulo2            = view.findViewById(R.id.tvTitulo2);

        receitaController = new ReceitaController(new ReceitaDao());
        despesaController = new DespesaController(new DespesaDao());

        // Roda o acesso ao banco em thread separada
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                atualizarTotais();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return view;
    }

    @SuppressLint("DefaultLocale")
    private void atualizarTotais() throws Exception {
        List<Receita> receitas = receitaController.listar();
        List<Despesa> despesas = despesaController.listar();

        Receita receita = new Receita(receitas);
        Despesa despesa = new Despesa(despesas);

        double totalreceitas = receita.calcularSaldo();
        double totaldespesas = despesa.calcularSaldo();

        // Atualiza a UI de volta na thread principal
        requireActivity().runOnUiThread(() -> {
            tvTotalReceitaInicio.setText(String.format("R$ %.2f", totalreceitas));
            tvTotalDespesaInicio.setText(String.format("R$ %.2f", totaldespesas));
        });
    }
}