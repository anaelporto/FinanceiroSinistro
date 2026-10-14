# Financeiro Sinistro — protótipo de fechamento diário

## Introdução

O Financeiro Sinistro é um protótipo de aplicativo Android desenvolvido em Kotlin para auxiliar na conferência financeira diária de uma operação comercial. A aplicação reúne a contagem de dinheiro em caixa, o registro de pedidos do iFood e a consolidação de valores de débito, crédito e Pix, permitindo gerar um resumo financeiro em PDF.

O projeto busca reduzir cálculos manuais e organizar as informações em um único relatório, mantendo detalhes relevantes para a conferência dos valores.

## Funcionalidades

- Contagem de abertura e fechamento de caixa, com cálculo por denominação de cédulas e moedas.
- Registro de pedidos do iFood com itens predefinidos, quantidades, descontos e acréscimos.
- Consolidação de valores de débito, crédito e Pix.
- Atualização dos totais na interface com LiveData.
- Geração de relatórios em PDF, com seções organizadas e suporte a múltiplas páginas.

## Metodologia e implementação

O aplicativo utiliza modelos Kotlin, como `ContagemCaixa`, `LancamentoIfood` e `ResumoFinanceiro`, para representar os dados e calcular os totais. Os valores monetários são armazenados em centavos usando `Long`, reduzindo problemas de precisão nas operações financeiras.

A arquitetura utiliza ViewModel, LiveData e um repositório em memória para atualizar os dados conforme o usuário preenche as etapas. A geração dos relatórios é feita localmente com a API `PdfDocument` do Android, com salvamento em Downloads por meio do MediaStore nas versões mais recentes do sistema.

## Executar o projeto

1. Abra o projeto no Android Studio e sincronize as dependências Gradle.
2. Configure o JDK 17.
3. Execute a variante de depuração em um emulador ou dispositivo Android com API 24 ou superior.
4. Preencha os dados financeiros e teste a geração do PDF.

## Limitações conhecidas

O projeto ainda é um protótipo: os dados permanecem em memória, não há histórico persistente de fechamentos e os preços dos itens do iFood estão definidos no código. A validação das entradas e os cálculos precisam de testes adicionais.

Também é necessário revisar a composição do total diário, pois a soma dos pedidos do iFood com os valores por forma de pagamento pode gerar dupla contagem, caso essas categorias representem as mesmas vendas. Portanto, os resultados devem ser conferidos antes de serem utilizados como fechamento oficial.

## Conclusão

O Financeiro Sinistro demonstra a aplicação de modelos de dados, atualização reativa da interface, cálculos monetários e geração de documentos em um aplicativo Android. Para evoluir, recomenda-se implementar persistência dos fechamentos, permitir configurar os itens e preços, adicionar testes automatizados e validar as regras de composição dos totais.
