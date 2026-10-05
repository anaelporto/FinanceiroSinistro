package com.financeiro.sinistro.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.financeiro.sinistro.data.ContabilidadeFinal
import com.financeiro.sinistro.data.FinanceiroRepository
import com.financeiro.sinistro.data.ItemIfoodPredefinido
import com.financeiro.sinistro.data.LancamentoIfood
import com.financeiro.sinistro.data.ResumoFinanceiro
import com.financeiro.sinistro.data.TipoCaixa
import com.financeiro.sinistro.data.itensIfoodPadrao
import kotlin.math.roundToLong

class FinanceiroViewModel(private val repository: FinanceiroRepository) : ViewModel() {
    val resumo: LiveData<ResumoFinanceiro> = repository.resumo
    val itensIfood: List<ItemIfoodPredefinido> = itensIfoodPadrao

    fun atualizarTipoCaixa(tipo: TipoCaixa) = repository.atualizarTipoCaixa(tipo)

    fun atualizarQuantidadeCaixa(index: Int, quantidadeTexto: String) {
        repository.atualizarQuantidadeCaixa(index, quantidadeTexto.toIntOrNull() ?: 0)
    }

    fun adicionarIfood(item: ItemIfoodPredefinido, descontoTexto: String) {
        repository.adicionarIfood(
            LancamentoIfood(
                codigo = item.codigo,
                descricao = item.descricao,
                valorOriginalCentavos = item.valorCentavos,
                descontoCentavos = descontoTexto.toCentavos()
            )
        )
    }

    fun removerIfood(index: Int) = repository.removerIfood(index)

    fun atualizarContabilidade(debito: String, credito: String, pix: String) {
        repository.atualizarContabilidade(
            ContabilidadeFinal(
                debitoCentavos = debito.toCentavos(),
                creditoCentavos = credito.toCentavos(),
                pixCentavos = pix.toCentavos()
            )
        )
    }

    fun limparDia() = repository.limparDia()

    private fun String.toCentavos(): Long {
        val normalizado = trim()
            .replace("R$", "")
            .replace(".", "")
            .replace(",", ".")
        return ((normalizado.toDoubleOrNull() ?: 0.0) * 100).roundToLong()
    }
}

class FinanceiroViewModelFactory(
    private val repository: FinanceiroRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FinanceiroViewModel(repository) as T
    }
}
