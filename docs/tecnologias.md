# Financeiro Sinistro - primeiro prototipo

## Stack sugerida

- Kotlin + Android XML/ViewBinding: reaproveita a arquitetura do `SorveterIACodex`, com telas simples e baixo custo para evoluir.
- `ViewModel` + `LiveData`: mantém totais de caixa, iFood e fechamento sempre atualizados enquanto o usuario preenche as etapas.
- `PdfDocument`: gera um PDF local em Downloads, sem depender de servidor ou internet.
- Valores monetarios em centavos (`Long`): evita divergencias de arredondamento em dinheiro.
- Proxima fase: Room para historico diario, presets editaveis de itens iFood e assinatura/conferencia do fechamento.

## Fluxo do prototipo

1. Contagem de caixa: abertura ou fechamento, com notas/moedas de R$ 50,00 ate R$ 0,25.
2. iFood: escolha por item predefinido, desconto opcional e soma automatica do valor final.
3. Contabilidade final: debito, credito, pix, online e caixa.
4. PDF: resumo estruturado por secoes, salvo em Downloads.

## Arquivos principais

- `app/src/main/java/com/financeiro/sinistro/data/FinanceiroModels.kt`: modelos e calculos.
- `app/src/main/java/com/financeiro/sinistro/data/FinanceiroRepository.kt`: estado em memoria.
- `app/src/main/java/com/financeiro/sinistro/ui/FinanceiroViewModel.kt`: ponte de LiveData entre tela e dados.
- `app/src/main/java/com/financeiro/sinistro/ui/MainActivity.kt`: fluxo por etapas.
- `app/src/main/java/com/financeiro/sinistro/util/PdfHelper.kt`: exportacao do fechamento em PDF.
