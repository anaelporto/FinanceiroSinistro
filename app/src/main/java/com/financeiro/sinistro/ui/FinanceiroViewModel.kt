package com.financeiro.sinistro.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.financeiro.sinistro.data.ContabilidadeFinal
import com.financeiro.sinistro.data.FinanceiroRepository
import com.financeiro.sinistro.data.ItemIfoodPredefinido
import com.financeiro.sinistro.data.ItemPedidoIfood
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

    private val _itensPedido = MutableLiveData<List<ItemPedidoIfood>>(emptyList())
    val itensPedido: LiveData<List<ItemPedidoIfood>> = _itensPedido

    fun adicionarItemAoPedido(item: ItemIfoodPredefinido, quantidadeTexto: String) {
        val quantidade = (quantidadeTexto.trim().toIntOrNull() ?: 1).coerceAtLeast(1)
        val atuais = _itensPedido.value.orEmpty()
        val existente = atuais.indexOfFirst { it.codigo == item.codigo }
        _itensPedido.value = if (existente >= 0) {
            atuais.mapIndexed { i, atual ->
                if (i == existente) atual.copy(quantidade = atual.quantidade + quantidade) else atual
            }
        } else {
            atuais + ItemPedidoIfood(item.codigo, item.descricao, item.valorCentavos, quantidade)
        }
    }

    fun removerItemDoPedido(index: Int) {
        _itensPedido.value = _itensPedido.value.orEmpty().filterIndexed { i, _ -> i != index }
    }

    /** Fecha o pedido em montagem e o envia ao resumo. Retorna false se não houver itens. */
    fun finalizarPedidoIfood(descontoTexto: String, acrescimoTexto: String): Boolean {
        val itens = _itensPedido.value.orEmpty()
        if (itens.isEmpty()) return false
        repository.adicionarIfood(
            LancamentoIfood(
                itens = itens,
                descontoCentavos = descontoTexto.toCentavos(),
                acrescimoCentavos = acrescimoTexto.toCentavos()
            )
        )
        _itensPedido.value = emptyList()
        return true
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

    fun limparDia() {
        _itensPedido.value = emptyList()
        repository.limparDia()
    }

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
