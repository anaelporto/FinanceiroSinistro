package com.financeiro.sinistro.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.financeiro.sinistro.FinanceiroApplication
import com.financeiro.sinistro.R
import com.financeiro.sinistro.data.ResumoFinanceiro
import com.financeiro.sinistro.data.TipoCaixa
import com.financeiro.sinistro.data.formatarMoeda
import com.financeiro.sinistro.databinding.ActivityMainBinding
import com.financeiro.sinistro.util.PdfHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: FinanceiroViewModel by viewModels {
        FinanceiroViewModelFactory((application as FinanceiroApplication).repository)
    }

    private var etapaAtual = 0
    private lateinit var ifoodAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarStepper()
        configurarCaixa()
        configurarIfood()
        configurarContabilidade()
        configurarAcoes()

        viewModel.resumo.observe(this) { resumo ->
            renderizarResumo(resumo)
            renderizarIfood(resumo)
        }
        selecionarEtapa(0)
    }

    private fun configurarStepper() {
        listOf(binding.btnEtapaCaixa, binding.btnEtapaIfood, binding.btnEtapaContabilidade, binding.btnEtapaRevisao)
            .forEachIndexed { index, button -> button.setOnClickListener { selecionarEtapa(index) } }
    }

    private fun configurarCaixa() {
        binding.grupoTipoCaixa.check(R.id.radioFechamento)
        binding.grupoTipoCaixa.setOnCheckedChangeListener { _, checkedId ->
            viewModel.atualizarTipoCaixa(if (checkedId == R.id.radioAbertura) TipoCaixa.ABERTURA else TipoCaixa.FECHAMENTO)
        }

        viewModel.resumo.value?.caixa?.linhas.orEmpty().forEachIndexed { index, linha ->
            val row = layoutInflater.inflate(R.layout.item_quantidade, binding.containerCaixa, false)
            row.findViewById<TextView>(R.id.txtLabel).text = linha.denominacao.rotulo
            row.findViewById<EditText>(R.id.inputQuantidade).doAfterTextChanged {
                viewModel.atualizarQuantidadeCaixa(index, it?.toString().orEmpty())
            }
            binding.containerCaixa.addView(row)
        }
    }

    private fun configurarIfood() {
        ifoodAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.itensIfood.map { "${it.codigo} - ${it.descricao} (${it.valorCentavos.formatarMoeda()})" }
        )
        binding.spinnerIfood.adapter = ifoodAdapter
        binding.btnAdicionarIfood.setOnClickListener {
            val item = viewModel.itensIfood[binding.spinnerIfood.selectedItemPosition]
            viewModel.adicionarIfood(item, binding.inputDescontoIfood.text.toString())
            binding.inputDescontoIfood.text?.clear()
        }
    }

    private fun configurarContabilidade() {
        val watcher = {
            viewModel.atualizarContabilidade(
                debito = binding.inputDebito.text.toString(),
                credito = binding.inputCredito.text.toString(),
                pix = binding.inputPix.text.toString()
            )
        }
        binding.inputDebito.doAfterTextChanged { watcher() }
        binding.inputCredito.doAfterTextChanged { watcher() }
        binding.inputPix.doAfterTextChanged { watcher() }
    }

    private fun configurarAcoes() {
        binding.btnAnterior.setOnClickListener { selecionarEtapa((etapaAtual - 1).coerceAtLeast(0)) }
        binding.btnProxima.setOnClickListener { selecionarEtapa((etapaAtual + 1).coerceAtMost(3)) }
        binding.btnGerarPdf.setOnClickListener {
            val resumo = viewModel.resumo.value ?: return@setOnClickListener
            val sucesso = PdfHelper(this).gerarFechamentoDiario(resumo)
            Toast.makeText(this, if (sucesso) "PDF salvo em Downloads" else "Falha ao gerar PDF", Toast.LENGTH_LONG).show()
        }
        binding.btnLimpar.setOnClickListener {
            viewModel.limparDia()
            recreate()
        }
    }

    private fun selecionarEtapa(index: Int) {
        etapaAtual = index
        val paginas = listOf(binding.paginaCaixa, binding.paginaIfood, binding.paginaContabilidade, binding.paginaRevisao)
        val botoes = listOf(binding.btnEtapaCaixa, binding.btnEtapaIfood, binding.btnEtapaContabilidade, binding.btnEtapaRevisao)

        paginas.forEachIndexed { paginaIndex, view -> view.visibility = if (paginaIndex == index) View.VISIBLE else View.GONE }
        botoes.forEachIndexed { botaoIndex, button -> button.isSelected = botaoIndex == index }

        binding.btnAnterior.isEnabled = index > 0
        binding.btnProxima.visibility = if (index < 3) View.VISIBLE else View.GONE
        binding.btnGerarPdf.visibility = if (index == 3) View.VISIBLE else View.GONE
    }

    private fun renderizarResumo(resumo: ResumoFinanceiro) {
        binding.txtTotalCaixa.text = resumo.caixa.totalCentavos.formatarMoeda()
        binding.txtTotalIfood.text = resumo.totalIfoodCentavos.formatarMoeda()
        binding.txtResumoDebito.text = "Debito: ${resumo.contabilidade.debitoCentavos.formatarMoeda()}"
        binding.txtResumoCredito.text = "Credito: ${resumo.contabilidade.creditoCentavos.formatarMoeda()}"
        binding.txtResumoOnline.text = "Online: ${resumo.totalIfoodCentavos.formatarMoeda()}"
        binding.txtResumoPix.text = "Pix: ${resumo.contabilidade.pixCentavos.formatarMoeda()}"
        binding.txtResumoCaixa.text = "Caixa: ${resumo.caixa.totalCentavos.formatarMoeda()}"
        binding.txtResumoTotal.text = "Total: ${resumo.totalMovimentadoCentavos.formatarMoeda()}"
    }

    private fun renderizarIfood(resumo: ResumoFinanceiro) {
        binding.containerIfood.removeAllViews()
        if (resumo.ifood.isEmpty()) {
            val vazio = TextView(this).apply {
                text = "Nenhum pedido adicionado"
                setTextAppearance(R.style.TextAppearance_Financeiro_Body)
            }
            binding.containerIfood.addView(vazio)
            return
        }
        resumo.ifood.forEachIndexed { index, lancamento ->
            val row = layoutInflater.inflate(R.layout.item_ifood, binding.containerIfood, false)
            row.findViewById<TextView>(R.id.txtDescricaoIfood).text = "${lancamento.codigo} - ${lancamento.descricao}"
            row.findViewById<TextView>(R.id.txtValoresIfood).text =
                "Bruto ${lancamento.valorOriginalCentavos.formatarMoeda()} | Desc. ${lancamento.descontoCentavos.formatarMoeda()} | Final ${lancamento.valorFinalCentavos.formatarMoeda()}"
            row.findViewById<Button>(R.id.btnRemoverIfood).setOnClickListener { viewModel.removerIfood(index) }
            binding.containerIfood.addView(row)
        }
    }
}
