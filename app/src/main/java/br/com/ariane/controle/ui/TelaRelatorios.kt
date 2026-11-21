package br.com.ariane.controle.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Relatorio
import br.com.ariane.controle.data.Store
import br.com.ariane.controle.export.Compartilhar
import br.com.ariane.controle.export.Csv
import br.com.ariane.controle.export.PdfRelatorio

@Composable
fun TelaRelatorios(store: Store) {

    val contexto = LocalContext.current
    var abrindo by remember { mutableStateOf<Relatorio?>(null) }
    var paraExcluir by remember { mutableStateOf<Relatorio?>(null) }
    var gerando by remember { mutableStateOf(false) }
    var escolhendoReceita by remember { mutableStateOf(false) }
    var receitaId by remember { mutableStateOf(0L) }
    var quantidadeTexto by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf<String?>(null) }

    val alvo = paraExcluir
    if (alvo != null) {
        DialogoConfirmar(
            titulo = "Excluir relatório",
            mensagem = "Deseja excluir o relatório de " + alvo.nomeReceita + "?",
            textoConfirmar = "Excluir",
            aoConfirmar = {
                store.excluirRelatorio(alvo.id)
                if (abrindo?.id == alvo.id) abrindo = null
                paraExcluir = null
            },
            aoFechar = { paraExcluir = null }
        )
    }

    if (escolhendoReceita) {
        DialogoEscolherReceita(
            receitas = store.receitas.toList(),
            aoEscolher = { receita ->
                receitaId = receita.id
                quantidadeTexto = Calculos.quantidade(receita.rendimento)
                escolhendoReceita = false
            },
            aoFechar = { escolhendoReceita = false }
        )
    }

    val relatorioAberto = abrindo
    if (relatorioAberto != null) {
        DetalheRelatorio(
            relatorio = relatorioAberto,
            aoVoltar = { abrindo = null },
            aoExcluir = { paraExcluir = relatorioAberto },
            aoCompartilharPdf = {
                val arquivo = PdfRelatorio.gerar(contexto, relatorioAberto)
                Compartilhar.arquivo(
                    contexto,
                    arquivo,
                    "application/pdf",
                    "Relatório de produção - " + relatorioAberto.nomeReceita
                )
            },
            aoExportarCsv = {
                val arquivo = Csv.doRelatorio(contexto, relatorioAberto)
                Compartilhar.arquivo(
                    contexto,
                    arquivo,
                    "text/csv",
                    "Planilha - " + relatorioAberto.nomeReceita
                )
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            if (!gerando) {
                BotaoPrincipal(
                    texto = "+ Gerar novo relatório",
                    aoClicar = { gerando = true }
                )
            } else {
                Secao(titulo = "Novo relatório", subtitulo = "Escolha o produto e a quantidade produzida") {
                    BotaoSecundario(
                        texto = store.receitaPorId(receitaId)?.nome ?: "Escolher produto",
                        aoClicar = { escolhendoReceita = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampoDecimal(
                        rotulo = "Quantidade produzida",
                        valor = quantidadeTexto,
                        aoMudar = { quantidadeTexto = it }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    val receita = store.receitaPorId(receitaId)
                    val quantidade = Calculos.paraDouble(quantidadeTexto)
                    BotaoPrincipal(
                        texto = "Gerar e salvar",
                        habilitado = receita != null && quantidade > 0.0,
                        aoClicar = {
                            if (receita != null && quantidade > 0.0) {
                                val fator = Calculos.fatorPara(receita, quantidade)
                                val resumo = Calculos.resumir(receita, store.ingredientes, fator)
                                store.salvarRelatorio(Calculos.paraRelatorio(receita.id, resumo))
                                gerando = false
                                receitaId = 0L
                                quantidadeTexto = ""
                                mensagem = "Relatório gerado."
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BotaoSecundario(texto = "Cancelar", aoClicar = { gerando = false })
                }
            }

            val textoMensagem = mensagem
            if (textoMensagem != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Aviso(texto = textoMensagem, corFundo = VerdeClaro, corTexto = Verde)
            }

            if (store.relatorios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                BotaoSecundario(
                    texto = "Exportar todos em planilha CSV",
                    aoClicar = {
                        val arquivo = Csv.dosRelatorios(contexto, store.relatorios.toList())
                        Compartilhar.arquivo(contexto, arquivo, "text/csv", "Meus relatórios")
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (store.relatorios.isEmpty()) {
            EstadoVazio(
                titulo = "Nenhum relatório ainda",
                mensagem = "Gere um relatório para guardar os valores de uma produção. " +
                    "Mesmo que os preços mudem depois, o relatório guarda os valores da época."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(store.relatorios.toList(), key = { it.id }) { relatorio ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { abrindo = relatorio },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = relatorio.nomeReceita,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextoEscuro
                            )
                            Text(
                                text = Calculos.dataHora(relatorio.criadoEm) + "  •  " +
                                    Calculos.quantidade(relatorio.quantidade) + " unidades",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                            Separador()
                            LinhaValor("Custo total", Calculos.moeda(relatorio.custoTotal))
                            LinhaValor("Faturamento", Calculos.moeda(relatorio.faturamento))
                            LinhaValor("Lucro", Calculos.moeda(relatorio.lucro), destaque = true, cor = Verde)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalheRelatorio(
    relatorio: Relatorio,
    aoVoltar: () -> Unit,
    aoExcluir: () -> Unit,
    aoCompartilharPdf: () -> Unit,
    aoExportarCsv: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Secao(
            titulo = relatorio.nomeReceita,
            subtitulo = Calculos.dataHora(relatorio.criadoEm)
        ) {
            LinhaValor("Quantidade produzida", Calculos.quantidade(relatorio.quantidade) + " unidades", destaque = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Ingredientes") {
            var algum = false
            for (linha in relatorio.linhas) {
                if (linha.extra) continue
                algum = true
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = linha.nome,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoEscuro
                        )
                        if (linha.detalhe.isNotBlank()) {
                            Text(
                                text = linha.detalhe,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                        }
                    }
                    Text(
                        text = Calculos.moeda(linha.custo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Vinho
                    )
                }
                Separador()
            }
            if (!algum) {
                Text(
                    text = "Sem ingredientes registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave
                )
            }
            LinhaValor("Total dos ingredientes", Calculos.moeda(relatorio.custoIngredientes), destaque = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Embalagens e custos extras") {
            var algum = false
            for (linha in relatorio.linhas) {
                if (!linha.extra) continue
                algum = true
                LinhaValor(linha.nome, Calculos.moeda(linha.custo))
            }
            if (!algum) {
                Text(
                    text = "Sem custos extras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave
                )
            }
            Separador()
            LinhaValor("Total de extras", Calculos.moeda(relatorio.custoExtras), destaque = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Resumo financeiro") {
            LinhaValor("Custo total", Calculos.moeda(relatorio.custoTotal), destaque = true)
            LinhaValor("Custo por unidade", Calculos.moeda(relatorio.custoUnitario), destaque = true)
            LinhaValor("Margem aplicada", Calculos.porcentagem(relatorio.margem))
            Separador()
            LinhaValor("Preço de venda", Calculos.moeda(relatorio.precoVenda), destaque = true, cor = Vinho)
            LinhaValor("Faturamento estimado", Calculos.moeda(relatorio.faturamento))
            LinhaValor("Lucro estimado", Calculos.moeda(relatorio.lucro), destaque = true, cor = Verde)
            Spacer(modifier = Modifier.height(12.dp))
            GraficoDonut(
                partes = listOf(
                    ParteDonut("Ingredientes", relatorio.custoIngredientes, Vinho),
                    ParteDonut("Extras", relatorio.custoExtras, Dourado),
                    ParteDonut("Lucro", if (relatorio.lucro > 0.0) relatorio.lucro else 0.0, Verde)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BotaoPrincipal(texto = "Compartilhar em PDF", aoClicar = aoCompartilharPdf)
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Exportar planilha CSV", aoClicar = aoExportarCsv)
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Excluir relatório", aoClicar = aoExcluir, cor = Vermelho)
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Voltar para a lista", aoClicar = aoVoltar)
        Spacer(modifier = Modifier.height(24.dp))
    }
}
