# Ariane: Controle

Aplicativo Android de **custos, precificação e lucro** para quem produz e vende doces
artesanais (trufas, bombons, brigadeiros, bolos e sobremesas).

A usuária cadastra os ingredientes com o preço que pagou, monta a receita, escolhe a
margem de lucro — e o aplicativo faz todas as contas sozinho.

## Como baixar o APK

1. Abra a aba **Releases** deste repositório.
2. Baixe o arquivo `ariane-controle.apk` da versão mais recente.
3. No celular, toque no arquivo baixado e confirme a instalação
   (se aparecer um aviso, permita a instalação de "apps de fontes desconhecidas").

O APK é gerado automaticamente pelo GitHub Actions a cada atualização do código.

## O que o aplicativo faz

| Tela | Para que serve |
|---|---|
| **Início** | Resumo financeiro, gráficos, produtos mais rentáveis e menu principal |
| **Meus Ingredientes** | Cadastro com preço, quantidade e unidade; calcula o custo por grama, ml ou unidade |
| **Minhas Receitas** | Monta o produto com ingredientes, custos extras e rendimento |
| **Calculadora de Custos** | Custo por unidade, margem de lucro e preço de venda recomendado |
| **Simulador de Produção** | "Quanto vou gastar para produzir 20, 50, 100, 200 ou 500 unidades?" |
| **Meus Relatórios** | Relatórios salvos, exportação em **PDF** e planilha **CSV** |
| **Compras e Estoque** | Histórico de compras, estoque, alerta de reposição e variação de preços |
| **Configurações** | Margem padrão, **backup e restauração**, exportações |

## A conta da margem de lucro

O aplicativo usa a fórmula correta de **margem sobre o preço de venda**:

```
preço de venda = custo por unidade ÷ (1 − margem)
```

Exemplo: custo de R$ 2,00 com margem de 50% → **R$ 4,00** (e não R$ 3,00).
R$ 3,00 seria apenas um acréscimo de 50% sobre o custo (*markup*), que corresponde
a uma margem real de 33%. O app mostra os dois números lado a lado para não haver engano.

## Como funciona por dentro

- **Kotlin + Jetpack Compose** (Material 3), interface em português do Brasil.
- **Banco de dados local SQLite** no próprio aparelho: funciona **sem internet** e
  nada é enviado para fora do celular.
- Relatórios em **PDF** gerados pelo próprio app e compartilháveis pelo WhatsApp.
- Backup e restauração em arquivo `.json`.
- Cálculos cobertos por **testes automatizados** (`app/src/test`), executados a cada build.

### Estrutura do projeto

```
app/src/main/java/br/com/ariane/controle/
├── MainActivity.kt        navegação entre as telas
├── data/                  modelos, banco SQLite e estado do app
├── calc/                  motor de cálculo (custos, conversões, margem x markup)
├── export/                PDF, CSV e compartilhamento
└── ui/                    tema, componentes e as 8 telas
```

### Compilar manualmente

```bash
./gradlew assembleDebug      # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest  # roda os testes dos cálculos
```

Requisitos: JDK 17 e Android SDK (compileSdk 35). Android mínimo: 8.0 (API 26).
