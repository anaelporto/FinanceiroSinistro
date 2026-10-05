package com.financeiro.sinistro.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FinanceiroRepository {
    private val _resumo = MutableLiveData(ResumoFinanceiro())
    val resumo: LiveData<ResumoFinanceiro> = _resumo

    fun atualizarTipoCaixa(tipo: TipoCaixa) {
        atualizar { copy(caixa = caixa.copy(tipo = tipo)) }
    }

    fun atualizarQuantidadeCaixa(index: Int, quantidade: Int) {
        atualizar {
            val novasLinhas = caixa.linhas.mapIndexed { linhaIndex, linha ->
                if (linhaIndex == index) linha.copy(quantidade = quantidade.coerceAtLeast(0)) else linha
            }
            copy(caixa = caixa.copy(linhas = novasLinhas))
        }
    }

    fun adicionarIfood(lancamento: LancamentoIfood) {
        atualizar { copy(ifood = ifood + lancamento) }
    }

    fun removerIfood(index: Int) {
        atualizar { copy(ifood = ifood.filterIndexed { linhaIndex, _ -> linhaIndex != index }) }
    }

    fun atualizarContabilidade(contabilidade: ContabilidadeFinal) {
        atualizar { copy(contabilidade = contabilidade) }
    }

    fun limparDia() {
        _resumo.value = ResumoFinanceiro()
    }

    private fun atualizar(transform: ResumoFinanceiro.() -> ResumoFinanceiro) {
        _resumo.value = (_resumo.value ?: ResumoFinanceiro()).transform()
    }
}
