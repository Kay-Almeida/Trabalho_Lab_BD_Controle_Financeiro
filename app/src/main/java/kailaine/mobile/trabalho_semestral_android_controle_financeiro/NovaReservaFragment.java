package kailaine.mobile.trabalho_semestral_android_controle_financeiro;
/*
 *@author:<Kailaine Almeida de Souza RA: 1110482313026>
 */
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

public class NovaReservaFragment extends Fragment {
    private View view;
    private Spinner spMetaReserva;
    private TextView tvTitulo;
    private EditText etValorReserva;
    private Button btnSalvarReserva;

    private ReservaController reservaController;
    private MetaController metaController;
    private List<MetaFinanceira> metas;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NovaReservaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nova_reserva, container, false);
        spMetaReserva  = view.findViewById(R.id.spMetaReserva);
        tvTitulo       = view.findViewById(R.id.tvTitulo);
        etValorReserva = view.findViewById(R.id.etValorReserva);
        btnSalvarReserva = view.findViewById(R.id.btnSalvarReserva);

        reservaController = new ReservaController(new ReservaFinanceiraDao());
        metaController    = new MetaController(new MetaFinanceiraDao());

        preencheSpinner();
        btnSalvarReserva.setOnClickListener(op -> salvarReserva());

        return view;
    }

    // ------------------------------------------------------------------
    // Salvar
    // ------------------------------------------------------------------

    private void salvarReserva() {
        int spReserva = spMetaReserva.getSelectedItemPosition();
        if (spReserva == 0) {
            Toast.makeText(view.getContext(), "Selecione uma meta.", Toast.LENGTH_SHORT).show();
            return;
        }

        String valorStr = etValorReserva.getText().toString();
        if (valorStr.isEmpty()) {
            Toast.makeText(view.getContext(), "Informe o valor da reserva.", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor = Double.parseDouble(valorStr);
        MetaFinanceira metaSelecionada = (MetaFinanceira) spMetaReserva.getSelectedItem();

        ReservaFinanceira reserva = new ReservaFinanceira();
        reserva.setValor(valor);
        reserva.setMeta(metaSelecionada);
        reserva.getData();

        executor.execute(() -> {
            try {
                reservaController.inserir(reserva);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(view.getContext(), reserva.toString(), Toast.LENGTH_LONG).show();
                    etValorReserva.setText("");
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(view.getContext(), "Erro ao salvar reserva: " + e.getMessage(), Toast.LENGTH_LONG).show()
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
                    spMetaReserva.setAdapter(adapter);
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