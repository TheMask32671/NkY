package br.com.ariane.controle.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Store

private val QUANTIDADES_PRONTAS = listOf(20.0, 50.0, 100.0, 200.0, 500.0)

@Composable
fun TelaSimulador(store: Store) {

    var receitaId by remember { mutableStateOf(0L) }
    var escolhendo by remember { mutableStateOf(false) }
    var quantidadeTexto by remember { mutableStateOf("") }
    var confirmandoBaixa by remember { mutableStateOf(false) }
    var mensagem by remember { mutableStateOf<String?>(null) }

    val receita = store.receitaPorId(receitaId)
    val quantidadeDesejada = Calculos.paraDouble(quantidadeTexto)
    val fator = if (receita != null) Calculos.fatorPara(receita, quantidadeDesejada) else 0.0
    val resumo = if (receita != null && fator > 0.0) {
        Calculos.resumir(receita, store.ingredientes, fator)
    } else {
        null
    }
    val necessidades = if (receita != null && fator > 0.0) {
        Calculos.necessidades(receita, store.ingredientes, fator)
    } else {
        emptyList()
    }
    val faltaAlgo = necessidades.any { !it.estoqueSuficiente }

    if (escolhendo) {
        DialogoEscolherReceita(
            receitas = store.receitas.toList(),
            aoEscolher = { escolhida ->
                receitaId = escolhida.id
                escolhendo = false
            },
            aoFechar = { escolhendo = false }
        )
    }

    if (confirmandoBaixa && receita != null) {
        DialogoConfirmar(
            titulo = "Dar baixa no estoque",
            mensagem = "Vamos descontar do seu estoque tudo o que essa produção usa. Confirma?",
            textoConfirmar = "Dar baixa",
            aoConfirmar = {
                for (necessidade in necessidades) {
                    store.darBaixaNoEstoque(necessidade.ingrediente.id, necessidade.quantidadeBase)
                }
                confirmandoBaixa = false
                mensagem = "Estoque atualizado."
            },
            aoFechar = { confirmandoBaixa = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Secao(
            titulo = "Quanto vou gastar para produzir?",
            subtitulo = "Escolha o produto e diga quantas unidades quer fazer"
        ) {
            BotaoSecundario(
                texto = if (receita != null) receita.nome else "Escolher produto",
                aoClicar = { escolhendo = true }
            )
            Spacer(modifier = Modifier.height(14.dp))
            FileiraDeChips {
                for (opcao in QUANTIDADES_PRONTAS) {
                    ChipSelecao(
                        texto = Calculos.quantidade(opcao) + " un",
                        selecionado = quantidadeDesejada == opcao,
                        aoClicar = { quantidadeTexto = Calculos.quantidade(opcao) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Quantas unidades quero produzir?",
                valor = quantidadeTexto,
                aoMudar = { quantidadeTexto = it }
            )
            if (receita != null && fator > 0.0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Equivale a " + Calculos.numero(fator, 2) + " receita(s) de " +
                        Calculos.quantidade(receita.rendimento) + " unidades.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
        }

        if (receita == null) {
            Spacer(modifier = Modifier.height(16.dp))
            EstadoVazio(
                titulo = "Escolha um produto",
                mensagem = "Selecione uma receita para ver quanto você vai gastar, quanto vai faturar e quanto vai lucrar."
            )
        } else if (resumo == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Aviso(texto = "Digite quantas unidades você quer produzir.")
        } else {

            Spacer(modifier = Modifier.height(16.dp))

            Secao(
                titulo = "Lista de compras",
                subtitulo = "O que essa produção consome"
            ) {
                for (necessidade in necessidades) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = necessidade.ingrediente.nome,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextoEscuro
                            )
                            Text(
                                text = "Precisa de " + Calculos.quantidadeBaseAmigavel(
                                    necessidade.quantidadeBase,
                                    necessidade.ingrediente.grupo
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                            if (!necessidade.estoqueSuficiente) {
                                Text(
                                    text = "Faltam " + Calculos.quantidadeBaseAmigavel(
                                        necessidade.faltando,
                                        necessidade.ingrediente.grupo
                                    ) + " no estoque",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Vermelho
                                )
                            }
                        }
                        Text(
                            text = Calculos.moeda(necessidade.custo),
                            style = MaterialTheme.typography.titleSmall,
                            color = Vinho
                        )
                    }
                    Separador()
                }
                if (resumo.custoExtras > 0.0) {
                    for (linha in resumo.linhas.filter { it.extra }) {
                        LinhaValor(linha.nome, Calculos.moeda(linha.custo))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Secao(titulo = "Investimento necessário") {
                LinhaValor("Custo dos ingredientes", Calculos.moeda(resumo.custoIngredientes))
                LinhaValor("Embalagens e custos extras", Calculos.moeda(resumo.custoExtras))
                LinhaValor(
                    rotulo = "Total a investir",
                    valor = Calculos.moeda(resumo.custoTotal),
                    destaque = true,
                    cor = Vinho
                )
                Separador()
                LinhaValor("Unidades produzidas", Calculos.quantidade(resumo.quantidadeProduzida))
                LinhaValor("Custo de cada unidade", Calculos.moeda(resumo.custoUnitario), destaque = true)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Secao(
                titulo = "Quanto você ganha",
                subtitulo = "Com margem de " + Calculos.porcentagem(resumo.margem)
            ) {
                LinhaValor("Preço de venda recomendado", Calculos.moeda(resumo.precoVenda), destaque = true, cor = Vinho)
                LinhaValor("Faturamento estimado", Calculos.moeda(resumo.faturamento))
                LinhaValor("Lucro estimado", Calculos.moeda(resumo.lucroTotal), destaque = true, cor = Verde)
                LinhaValor("Lucro por unidade", Calculos.moeda(resumo.lucroUnitario), cor = Verde)
                Spacer(modifier = Modifier.height(12.dp))
                GraficoDonut(
                    partes = listOf(
                        ParteDonut("Ingredientes", resumo.custoIngredientes, Vinho),
                        ParteDonut("Extras", resumo.custoExtras, Dourado),
                        ParteDonut("Lucro", if (resumo.lucroTotal > 0.0) resumo.lucroTotal else 0.0, Verde)
                    )
                )
            }

            if (faltaAlgo) {
                Spacer(modifier = Modifier.height(14.dp))
                Aviso(
                    texto = "Seu estoque atual não cobre toda essa produção. Veja os itens marcados em vermelho na lista de compras.",
                    corFundo = VermelhoClaro,
                    corTexto = Vermelho
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            BotaoPrincipal(
                texto = "Salvar como relatório",
                aoClicar = {
                    store.salvarRelatorio(Calculos.paraRelatorio(receita.id, resumo))
                    mensagem = "Relatório salvo em Meus Relatórios."
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Dar baixa no estoque",
                aoClicar = { confirmandoBaixa = true }
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
