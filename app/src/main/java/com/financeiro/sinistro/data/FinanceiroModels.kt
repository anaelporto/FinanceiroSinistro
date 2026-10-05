package com.financeiro.sinistro.data

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TipoCaixa(val rotulo: String) {
    ABERTURA("Abertura"),
    FECHAMENTO("Fechamento")
}

data class DenominacaoDinheiro(
    val rotulo: String,
    val valorCentavos: Long
)

data class LinhaCaixa(
    val denominacao: DenominacaoDinheiro,
    val quantidade: Int = 0
) {
    val totalCentavos: Long get() = denominacao.valorCentavos * quantidade
}

data class ContagemCaixa(
    val tipo: TipoCaixa = TipoCaixa.FECHAMENTO,
    val linhas: List<LinhaCaixa> = denominacoesPadrao.map { LinhaCaixa(it) }
) {
    val totalCentavos: Long get() = linhas.sumOf { it.totalCentavos }
}

data class ItemIfoodPredefinido(
    val codigo: String,
    val descricao: String,
    val valorCentavos: Long
)

data class LancamentoIfood(
    val codigo: String,
    val descricao: String,
    val valorOriginalCentavos: Long,
    val descontoCentavos: Long = 0L
) {
    val valorFinalCentavos: Long get() = (valorOriginalCentavos - descontoCentavos).coerceAtLeast(0L)
}

data class ContabilidadeFinal(
    val debitoCentavos: Long = 0L,
    val creditoCentavos: Long = 0L,
    val pixCentavos: Long = 0L
)

data class ResumoFinanceiro(
    val criadoEmMillis: Long = System.currentTimeMillis(),
    val caixa: ContagemCaixa = ContagemCaixa(),
    val ifood: List<LancamentoIfood> = emptyList(),
    val contabilidade: ContabilidadeFinal = ContabilidadeFinal()
) {
    val totalIfoodCentavos: Long get() = ifood.sumOf { it.valorFinalCentavos }
    val totalMovimentadoCentavos: Long
        get() = contabilidade.debitoCentavos +
            contabilidade.creditoCentavos +
            contabilidade.pixCentavos +
            totalIfoodCentavos +
            caixa.totalCentavos
}

val denominacoesPadrao = listOf(
    DenominacaoDinheiro("R$ 50,00", 5_000),
    DenominacaoDinheiro("R$ 20,00", 2_000),
    DenominacaoDinheiro("R$ 10,00", 1_000),
    DenominacaoDinheiro("R$ 5,00", 500),
    DenominacaoDinheiro("R$ 2,00", 200),
    DenominacaoDinheiro("R$ 1,00", 100),
    DenominacaoDinheiro("R$ 0,50", 50),
    DenominacaoDinheiro("R$ 0,25", 25)
)

val itensIfoodPadrao = listOf(
    ItemIfoodPredefinido("ACAI300", "Acai 300 ml", 1_500),
    ItemIfoodPredefinido("ACAI500", "Acai 500 ml", 2_200),
    ItemIfoodPredefinido("CASQ", "Casquinha", 700),
    ItemIfoodPredefinido("SUNDAE", "Sundae", 1_200),
    ItemIfoodPredefinido("BEBIDA", "Bebida", 600)
)

private val moedaFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
private val dataFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

fun Long.formatarMoeda(): String = moedaFormat.format(this / 100.0)

fun Long.formatarDataHora(): String = dataFormat.format(Date(this))
