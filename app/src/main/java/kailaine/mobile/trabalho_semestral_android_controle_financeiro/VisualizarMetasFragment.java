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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.MetaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.controller.ReservaController;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.MetaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.model.ReservaFinanceira;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.MetaFinanceiraDao;
import kailaine.mobile.trabalho_semestral_android_controle_financeiro.persistence.ReservaFinanceiraDao;

public class VisualizarMetasFragment extends Fragment {
    private View view;
    private TextView tvTituloMetas;
    private Spinner spMetasVisualizar;
    private EditText etValorTotalMeta;
    private Button btnConcluirMeta, btnEditarMeta, btnExluirMeta;
    private TextView tvResultaPorcentagemMeta;

    private MetaController metaController;
    private List<MetaFinanceira> metas;
    private ReservaController reservaController;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public VisualizarMetasFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_visualizar_metas, container, false);
        tvTituloMetas            = view.findViewById(R.id.tvTituloMetas);
        spMetasVisualizar        = view.findViewById(R.id.spMetasVisualizar);
        etValorTotalMeta         = view.findViewById(R.id.etValorTotalMeta);
        btnConcluirMeta          = view.findViewById(R.id.btnConcluirMeta);
        btnEditarMeta            = view.findViewById(R.id.btnEditarMeta);
        btnExluirMeta            = view.findViewById(R.id.btnExluirMeta);
        tvResultaPorcentagemMeta = view.findViewById(R.id.tvResultaPorcentagemMeta);

        metaController    = new MetaController(new MetaFinanceiraDao());
        reservaController = new ReservaController(new ReservaFinanceiraDao());

        preencheSpinner();

        btnConcluirMeta.setOnClickListener(op -> mostrarValor());
        btnEditarMeta.setOnClickListener(op -> editarMeta());
        btnExluirMeta.setOnClickListener(op -> excluirMeta());

        return view;
    }

    // ------------------------------------------------------------------
    // Excluir
    // ------------------------------------------------------------------

    private void excluirMeta() {
        int selectedPosition = spMetasVisualizar.getSelectedItemPosition();
        if (selectedPosition == 0) {
            Toast.makeText(getContext(), "Selecione uma meta válida.", Toast.LENGTH_SHORT).show();
            return;
        }
        MetaFinanceira metaSelecionada = metas.get(selectedPosition);

        executor.execute(() -> {
            try {
                List<ReservaFinanceira> reservasAssociadas = reservaController.buscarReservasPorMeta(metaSelecionada.getId());
                for (ReservaFinanceira reserva : reservasAssociadas) {
                    reservaController.deletar(reserva);
                }
                metaController.deletar(metaSelecionada);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Meta deletada com sucesso", Toast.LENGTH_SHORT).show();
                    preencheSpinner();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erro ao excluir meta: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------
    // Editar
    // ------------------------------------------------------------------

    private void editarMeta() {
        int selectedPosition = spMetasVisualizar.getSelectedItemPosition();
        if (selectedPosition == 0) {
            Toast.makeText(getContext(), "Selecione uma meta válida.", Toast.LENGTH_SHORT).show();
            return;
        }
        String valorStr = etValorTotalMeta.getText().toString();
        if (valorStr.isEmpty()) {
            Toast.makeText(getContext(), "Informe o novo valor da meta.", Toast.LENGTH_SHORT).show();
            return;
        }

        MetaFinanceira metaSelecionada = metas.get(selectedPosition);
        metaSelecionada.setValorMeta(Double.parseDouble(valorStr));

        executor.execute(() -> {
            try {
                metaController.modificar(metaSelecionada);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Valor de meta atualizado: " + metaSelecionada.toString(), Toast.LENGTH_SHORT).show()
                );
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erro ao editar meta: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------
    // Mostrar progresso
    // ------------------------------------------------------------------

    private void mostrarValor() {
        int selectedPosition = spMetasVisualizar.getSelectedItemPosition();
        if (selectedPosition == 0) {
            etValorTotalMeta.setText("");
            tvResultaPorcentagemMeta.setText("Progresso: 0.00%");
            Toast.makeText(getContext(), "Por favor, selecione uma meta válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        MetaFinanceira metaSelecionada = metas.get(selectedPosition);

        executor.execute(() -> {
            try {
                MetaFinanceira metaAtualizada = metaController.buscarPorObjeto(metaSelecionada);
                if (metaAtualizada != null) {
                    double progresso = metaAtualizada.calcularProgresso();
                    @SuppressLint("DefaultLocale")
                    String textoProgresso = String.format(
                            "Valor Reserva: %.2f\nProgresso: %.2f%%",
                            metaAtualizada.getTotalReserva(), progresso);

                    requireActivity().runOnUiThread(() -> {
                        etValorTotalMeta.setText(String.valueOf(metaAtualizada.getValorMeta()));
                        tvResultaPorcentagemMeta.setText(textoProgresso);
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Meta não encontrada no banco de dados.", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Erro ao buscar a meta: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------
    // Spinner
    // ------------------------------------------------------------------

    private void preencheSpinner() {
        executor.execute(() -> {
            try {
                List<MetaFinanceira> listaMetas = metaController.listar();
                MetaFinanceira m0 = new MetaFinanceira();
                m0.setValorMeta(0);
                m0.setNome("Selecione uma Meta");
                listaMetas.add(0, m0);

                metas = listaMetas;

                requireActivity().runOnUiThread(() -> {
                    ArrayAdapter<MetaFinanceira> adapter = new ArrayAdapter<>(
                            view.getContext(),
                            android.R.layout.simple_spinner_item,
                            metas
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spMetasVisualizar.setAdapter(adapter);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao carregar metas: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }
}