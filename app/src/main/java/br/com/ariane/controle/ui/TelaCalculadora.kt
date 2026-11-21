package br.com.ariane.controle.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Store

@Composable
fun TelaCalculadora(store: Store) {

    var usarReceita by remember { mutableStateOf(true) }
    var receitaId by remember { mutableStateOf(0L) }
    var escolhendo by remember { mutableStateOf(false) }
    var margem by remember { mutableStateOf(store.margemPadrao) }
    var custoManual by remember { mutableStateOf("") }
    var precoDesejado by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf<String?>(null) }

    val receita = store.receitaPorId(receitaId)
    val resumo = if (receita != null) {
        Calculos.resumir(receita, store.ingredientes, 1.0, margem)
    } else {
        null
    }

    val custoUnitario = if (usarReceita) {
        resumo?.custoUnitario ?: 0.0
    } else {
        Calculos.paraDouble(custoManual)
    }

    val precoVenda = Calculos.precoPorMargem(custoUnitario, margem)
    val lucroUnitario = precoVenda - custoUnitario
    val markup = Calculos.markupEquivalente(custoUnitario, precoVenda)
    val precoSeFosseMarkup = Calculos.precoPorMarkup(custoUnitario, margem)
    val margemSeFosseMarkup = Calculos.margemReal(custoUnitario, precoSeFosseMarkup)

    if (escolhendo) {
        DialogoEscolherReceita(
            receitas = store.receitas.toList(),
            aoEscolher = { escolhida ->
                receitaId = escolhida.id
                margem = escolhida.margem
                escolhendo = false
            },
            aoFechar = { escolhendo = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Secao(titulo = "O que você quer calcular?") {
            FileiraDeChips {
                ChipSelecao(
                    texto = "Usar uma receita",
                    selecionado = usarReceita,
                    aoClicar = { usarReceita = true }
                )
                ChipSelecao(
                    texto = "Digitar o custo",
                    selecionado = !usarReceita,
                    aoClicar = { usarReceita = false }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (usarReceita) {
                BotaoSecundario(
                    texto = if (receita != null) receita.nome else "Escolher produto",
                    aoClicar = { escolhendo = true }
                )
                if (receita != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rende " + Calculos.quantidade(receita.rendimento) +
                            " unidades por receita.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                }
            } else {
                CampoDecimal(
                    rotulo = "Custo para produzir 1 unidade (R$)",
                    valor = custoManual,
                    aoMudar = { custoManual = it },
                    apoio = "Ex.: 2,00"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Margem de lucro desejada") {
            SeletorMargem(margem = margem, aoMudar = { margem = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Resultado", subtitulo = "Por unidade") {
            LinhaValor("Custo de produção", Calculos.moeda(custoUnitario), destaque = true)
            LinhaValor("Margem desejada", Calculos.porcentagem(margem))
            Separador()
            LinhaValor(
                rotulo = "Preço de venda recomendado",
                valor = Calculos.moeda(precoVenda),
                destaque = true,
                cor = Vinho
            )
            LinhaValor("Lucro por unidade", Calculos.moeda(lucroUnitario), cor = Verde)
            LinhaValor("Acréscimo sobre o custo (markup)", Calculos.porcentagem(markup))

            if (usarReceita && resumo != null) {
                Separador()
                Text(
                    text = "Na receita inteira",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextoEscuro
                )
                LinhaValor("Unidades produzidas", Calculos.quantidade(resumo.quantidadeProduzida))
                LinhaValor("Custo total", Calculos.moeda(resumo.custoTotal))
                LinhaValor("Faturamento estimado", Calculos.moeda(precoVenda * resumo.quantidadeProduzida))
                LinhaValor(
                    rotulo = "Lucro total",
                    valor = Calculos.moeda(lucroUnitario * resumo.quantidadeProduzida),
                    cor = Verde
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "Margem não é a mesma coisa que markup",
            subtitulo = "A diferença que faz muita gente vender barato sem perceber"
        ) {
            Text(
                text = "Margem de lucro é a fatia do PREÇO DE VENDA que sobra para você. " +
                    "A conta certa é: preço = custo ÷ (1 − margem).",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoEscuro
            )
            Spacer(modifier = Modifier.height(10.dp))
            Aviso(
                texto = "Com " + Calculos.porcentagem(margem) + " de margem: " +
                    Calculos.moeda(custoUnitario) + " ÷ (1 − " + Calculos.numero(margem, 2) + ") = " +
                    Calculos.moeda(precoVenda) + ". Desse preço, " + Calculos.porcentagem(margem) +
                    " é lucro seu."
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Se em vez disso você só somasse " + Calculos.porcentagem(margem) +
                    " em cima do custo (markup), venderia por " + Calculos.moeda(precoSeFosseMarkup) +
                    " e sua margem real seria de apenas " + Calculos.porcentagem(margemSeFosseMarkup) + ".",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSuave
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "E se eu vender por outro preço?",
            subtitulo = "Descubra a margem real de um preço que você já pratica"
        ) {
            CampoDecimal(
                rotulo = "Quero vender por (R$)",
                valor = precoDesejado,
                aoMudar = { precoDesejado = it }
            )
            val preco = Calculos.paraDouble(precoDesejado)
            if (preco > 0.0 && custoUnitario > 0.0) {
                Spacer(modifier = Modifier.height(12.dp))
                val margemReal = Calculos.margemReal(custoUnitario, preco)
                LinhaValor("Lucro por unidade", Calculos.moeda(preco - custoUnitario), cor = if (preco >= custoUnitario) Verde else Vermelho)
                LinhaValor("Margem real", Calculos.porcentagem(margemReal), destaque = true)
                LinhaValor("Acréscimo sobre o custo", Calculos.porcentagem(Calculos.markupEquivalente(custoUnitario, preco)))
                if (preco < custoUnitario) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Aviso(
                        texto = "Atenção: esse preço está abaixo do custo. Você teria prejuízo de " +
                            Calculos.moeda(custoUnitario - preco) + " por unidade.",
                        corFundo = VermelhoClaro,
                        corTexto = Vermelho
                    )
                }
            }
        }

        if (usarReceita && receita != null && resumo != null) {
            Spacer(modifier = Modifier.height(20.dp))
            BotaoPrincipal(
                texto = "Guardar esta margem na receita",
                aoClicar = {
                    store.salvarReceita(receita.copy(margem = margem))
                    mensagem = "Margem de " + Calculos.porcentagem(margem) + " guardada em " + receita.nome + "."
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Salvar como relatório",
                aoClicar = {
                    store.salvarRelatorio(Calculos.paraRelatorio(receita.id, resumo))
                    mensagem = "Relatório salvo em Meus Relatórios."
                }
            )
        }

        val textoMensagem = mensagem
        if (textoMensagem != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Aviso(texto = textoMensagem, corFundo = VerdeClaro, corTexto = Verde)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
