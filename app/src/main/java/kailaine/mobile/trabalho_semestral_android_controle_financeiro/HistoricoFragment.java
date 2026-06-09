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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.DespesaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.ReceitaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.ReservaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Despesa;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.Receita;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ReservaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.DespesaDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.ReceitaDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.RelatorioDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.ReservaFinanceiraDao;

public class HistoricoFragment extends Fragment {
    private View view;
    private Button btnExcluirHistorico, btnModificarHistorico, btnPesquisarHistorico;
    private EditText etIDEntradaHistorico, etValorHistorico;
    private TextView tvEditorEntrada, tvResultadoListarHistorico, tvHistorico;
    private RadioGroup radioGroup3;
    private RadioButton rbDespesaHistorico, rbReceitaHistorico, rbReservaHistorico;

    // -- Novos elementos para a Stored Procedure --
    private EditText etMesProcedure, etAnoProcedure;
    private Button btnResumoMes;
    private TextView tvResultadoProcedure;

    private DespesaController despesaController;
    private ReceitaController receitaController;
    private ReservaController reservaController;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistoricoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_historico, container, false);

        btnExcluirHistorico   = view.findViewById(R.id.btnExcluirHistorico);
        btnModificarHistorico = view.findViewById(R.id.btnModificarHistorico);
        btnPesquisarHistorico = view.findViewById(R.id.btnPesquisarHistorico);

        etIDEntradaHistorico = view.findViewById(R.id.etIDEntradaHistorico);
        etValorHistorico     = view.findViewById(R.id.etValorHistorico);

        tvEditorEntrada            = view.findViewById(R.id.tvEditorEntrada);
        tvResultadoListarHistorico = view.findViewById(R.id.tvResultadoListarHistorico);
        tvHistorico                = view.findViewById(R.id.tvHistorico);
        tvResultadoListarHistorico.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        radioGroup3        = view.findViewById(R.id.radioGroup3);
        rbDespesaHistorico = view.findViewById(R.id.rbDespesaHistorico);
        rbReceitaHistorico = view.findViewById(R.id.rbReceitaHistorico);
        rbReservaHistorico = view.findViewById(R.id.rbReservaHistorico);

        // Novos elementos - Stored Procedure
        etMesProcedure      = view.findViewById(R.id.etMesProcedure);
        etAnoProcedure      = view.findViewById(R.id.etAnoProcedure);
        btnResumoMes        = view.findViewById(R.id.btnResumoMes);
        tvResultadoProcedure = view.findViewById(R.id.tvResultadoProcedure);

        despesaController = new DespesaController(new DespesaDao());
        receitaController = new ReceitaController(new ReceitaDao());
        reservaController = new ReservaController(new ReservaFinanceiraDao());

        btnExcluirHistorico.setOnClickListener(op -> excluirEntrada());
        btnModificarHistorico.setOnClickListener(op -> editarEntrada());
        btnPesquisarHistorico.setOnClickListener(op -> pesquisarEntrada());
        btnResumoMes.setOnClickListener(op -> chamarProcedure());

        listarTodasEntradas();
        return view;
    }

    // ------------------------------------------------------------------
    // Stored Procedure — Resumo do Mês (NOVO)
    // ------------------------------------------------------------------

    @SuppressLint("DefaultLocale")
    private void chamarProcedure() {
        String mesStr = etMesProcedure.getText().toString();
        String anoStr = etAnoProcedure.getText().toString();

        if (mesStr.isEmpty() || anoStr.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe o mês e o ano.", Toast.LENGTH_SHORT).show();
            return;
        }

        int mes = Integer.parseInt(mesStr);
        int ano = Integer.parseInt(anoStr);

        if (mes < 1 || mes > 12) {
            Toast.makeText(view.getContext(), "Mês inválido. Use valores de 1 a 12.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                RelatorioDao relatorioDao = new RelatorioDao();
                relatorioDao.open();
                double[] resultado = relatorioDao.chamarProcedureResumoFinanceiro(mes, ano);
                relatorioDao.close();

                double totalReceita = resultado[0];
                double totalDespesa = resultado[1];
                double saldo        = resultado[2];

                String texto = String.format(
                        "── Resumo %02d/%d ──\n" +
                        "Receitas:  R$ %.2f\n" +
                        "Despesas:  R$ %.2f\n" +
                        "Saldo:     R$ %.2f",
                        mes, ano, totalReceita, totalDespesa, saldo
                );

                requireActivity().runOnUiThread(() ->
                        tvResultadoProcedure.setText(texto)
                );

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao chamar procedure: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------
    // Listar
    // ------------------------------------------------------------------

    @SuppressLint("DefaultLocale")
    private void listarTodasEntradas() {
        executor.execute(() -> {
            try {
                StringBuilder resultado = new StringBuilder();

                List<Despesa> despesas = despesaController.listar();
                for (Despesa despesa : despesas) {
                    resultado.append("Despesa - ").append(despesa.getData())
                            .append(String.format(" ID: %d, Valor: %.2f\n", despesa.getId(), despesa.getValor()));
                }

                List<Receita> receitas = receitaController.listar();
                for (Receita receita : receitas) {
                    resultado.append("Receita - ").append(receita.getData())
                            .append(String.format(" ID: %d, Valor: %.2f\n", receita.getId(), receita.getValor()));
                }

                List<ReservaFinanceira> reservas = reservaController.listar();
                for (ReservaFinanceira reserva : reservas) {
                    resultado.append("Reserva - ").append(reserva.getMeta().getNome())
                            .append(String.format(" ID: %d, Valor: %.2f\n", reserva.getId(), reserva.getValor()));
                }

                String texto = resultado.toString();
                requireActivity().runOnUiThread(() ->
                        tvResultadoListarHistorico.setText(texto)
                );

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao listar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    // ------------------------------------------------------------------
    // Pesquisar
    // ------------------------------------------------------------------

    private void pesquisarEntrada() {
        String idStr = etIDEntradaHistorico.getText().toString();
        if (idStr.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe o ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        int id = Integer.parseInt(idStr);
        boolean isDespesa = rbDespesaHistorico.isChecked();
        boolean isReceita = rbReceitaHistorico.isChecked();
        boolean isReserva = rbReservaHistorico.isChecked();

        if (!isDespesa && !isReceita && !isReserva) {
            Toast.makeText(view.getContext(), "Escolha um tipo de entrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                if (isDespesa) {
                    Despesa despesa = despesaController.buscarPorId(id);
                    requireActivity().runOnUiThread(() -> {
                        etIDEntradaHistorico.setText(String.valueOf(despesa.getId()));
                        etValorHistorico.setText(String.valueOf(despesa.getValor()));
                        Toast.makeText(view.getContext(), "Despesa encontrada com sucesso", Toast.LENGTH_LONG).show();
                    });
                } else if (isReceita) {
                    Receita receita = receitaController.buscarPorId(id);
                    requireActivity().runOnUiThread(() -> {
                        etIDEntradaHistorico.setText(String.valueOf(receita.getId()));
                        etValorHistorico.setText(String.valueOf(receita.getValor()));
                        Toast.makeText(view.getContext(), "Receita encontrada com sucesso", Toast.LENGTH_LONG).show();
                    });
                } else {
                    ReservaFinanceira reserva = reservaController.buscarPorId(id);
                    requireActivity().runOnUiThread(() -> {
                        etIDEntradaHistorico.setText(String.valueOf(reserva.getId()));
                        etValorHistorico.setText(String.valueOf(reserva.getValor()));
                        Toast.makeText(view.getContext(), "Reserva encontrada com sucesso", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao pesquisar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    // ------------------------------------------------------------------
    // Editar
    // ------------------------------------------------------------------

    private void editarEntrada() {
        String idStr    = etIDEntradaHistorico.getText().toString();
        String valorStr = etValorHistorico.getText().toString();
        if (idStr.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe ID e Valor.", Toast.LENGTH_SHORT).show();
            return;
        }
        int id       = Integer.parseInt(idStr);
        double valor = Double.parseDouble(valorStr);
        boolean isDespesa = rbDespesaHistorico.isChecked();
        boolean isReceita = rbReceitaHistorico.isChecked();
        boolean isReserva = rbReservaHistorico.isChecked();

        if (!isDespesa && !isReceita && !isReserva) {
            Toast.makeText(view.getContext(), "Escolha um tipo de entrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                if (isDespesa) {
                    Despesa despesa = new Despesa();
                    despesa.setId(id);
                    despesa.setValor(valor);
                    despesaController.modificar(despesa);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Despesa editada com sucesso", Toast.LENGTH_LONG).show()
                    );
                } else if (isReceita) {
                    Receita receita = new Receita();
                    receita.setId(id);
                    receita.setValor(valor);
                    receitaController.modificar(receita);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Receita editada com sucesso", Toast.LENGTH_LONG).show()
                    );
                } else {
                    ReservaFinanceira reserva = reservaController.buscarPorId(id);
                    reserva.setId(id);
                    reserva.setValor(valor);
                    reservaController.modificar(reserva);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Reserva editada com sucesso", Toast.LENGTH_LONG).show()
                    );
                }
                requireActivity().runOnUiThread(() -> {
                    listarTodasEntradas();
                    limparCampos();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao editar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    // ------------------------------------------------------------------
    // Excluir
    // ------------------------------------------------------------------

    private void excluirEntrada() {
        String idStr    = etIDEntradaHistorico.getText().toString();
        String valorStr = etValorHistorico.getText().toString();
        if (idStr.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe ID e Valor.", Toast.LENGTH_SHORT).show();
            return;
        }
        int id       = Integer.parseInt(idStr);
        double valor = Double.parseDouble(valorStr);
        boolean isDespesa = rbDespesaHistorico.isChecked();
        boolean isReceita = rbReceitaHistorico.isChecked();
        boolean isReserva = rbReservaHistorico.isChecked();

        if (!isDespesa && !isReceita && !isReserva) {
            Toast.makeText(view.getContext(), "Escolha um tipo de entrada.", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                if (isDespesa) {
                    Despesa despesa = new Despesa();
                    despesa.setId(id);
                    despesa.setValor(valor);
                    despesaController.deletar(despesa);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Despesa excluída com sucesso", Toast.LENGTH_LONG).show()
                    );
                } else if (isReceita) {
                    Receita receita = new Receita();
                    receita.setId(id);
                    receita.setValor(valor);
                    receitaController.deletar(receita);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Receita excluída com sucesso", Toast.LENGTH_LONG).show()
                    );
                } else {
                    ReservaFinanceira reserva = new ReservaFinanceira();
                    reserva.setId(id);
                    reserva.setValor(valor);
                    reservaController.deletar(reserva);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(view.getContext(), "Reserva excluída com sucesso", Toast.LENGTH_LONG).show()
                    );
                }
                requireActivity().runOnUiThread(() -> {
                    listarTodasEntradas();
                    limparCampos();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    private void limparCampos() {
        etIDEntradaHistorico.setText("");
        etValorHistorico.setText("");
        radioGroup3.clearCheck();
    }
}
