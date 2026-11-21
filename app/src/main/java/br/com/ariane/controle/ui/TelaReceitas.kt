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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.ExtraReceita
import br.com.ariane.controle.data.ItemReceita
import br.com.ariane.controle.data.Receita
import br.com.ariane.controle.data.Store
import br.com.ariane.controle.data.Unidade

private data class ItemEditor(
    val ingredienteId: Long,
    val quantidadeTexto: String,
    val unidade: Unidade
)

private data class ExtraEditor(
    val descricao: String,
    val valorTexto: String
)

private val EXTRAS_SUGERIDOS = listOf(
    "Embalagens", "Etiquetas", "Sacolas", "Gás", "Energia elétrica", "Mão de obra", "Transporte"
)

@Composable
fun TelaReceitas(store: Store) {

    var emEdicao by remember { mutableStateOf<Receita?>(null) }
    var paraExcluir by remember { mutableStateOf<Receita?>(null) }

    val alvo = paraExcluir
    if (alvo != null) {
        DialogoConfirmar(
            titulo = "Excluir receita",
            mensagem = "Deseja excluir \"" + alvo.nome + "\"? Os relatórios já salvos continuam guardados.",
            textoConfirmar = "Excluir",
            aoConfirmar = {
                store.excluirReceita(alvo.id)
                paraExcluir = null
            },
            aoFechar = { paraExcluir = null }
        )
    }

    val receitaEmEdicao = emEdicao
    if (receitaEmEdicao != null) {
        FormularioReceita(
            store = store,
            inicial = receitaEmEdicao,
            aoSalvar = { receita ->
                store.salvarReceita(receita)
                emEdicao = null
            },
            aoCancelar = { emEdicao = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            BotaoPrincipal(
                texto = "+ Nova receita",
                aoClicar = { emEdicao = Receita(margem = store.margemPadrao) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (store.receitas.isEmpty()) {
            EstadoVazio(
                titulo = "Nenhuma receita ainda",
                mensagem = "Crie uma receita (trufa, bolo, brigadeiro), informe quanto usa de cada ingrediente " +
                    "e quantas unidades ela rende. O preço de venda aparece sozinho."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(store.receitas.toList(), key = { it.id }) { receita ->
                    val resumo = Calculos.resumir(receita, store.ingredientes)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { emEdicao = receita },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = receita.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextoEscuro
                                    )
                                    Text(
                                        text = "Rende " + Calculos.quantidade(receita.rendimento) +
                                            " unidades  •  " + receita.itens.size + " ingredientes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextoSuave
                                    )
                                }
                                IconButton(onClick = { paraExcluir = receita }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Excluir",
                                        tint = VinhoClaro,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Separador()
                            LinhaValor("Custo por unidade", Calculos.moeda(resumo.custoUnitario))
                            LinhaValor(
                                rotulo = "Preço de venda (" + Calculos.porcentagem(resumo.margem) + " de margem)",
                                valor = Calculos.moeda(resumo.precoVenda),
                                destaque = true,
                                cor = Vinho
                            )
                            LinhaValor(
                                rotulo = "Lucro por unidade",
                                valor = Calculos.moeda(resumo.lucroUnitario),
                                cor = Verde
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioReceita(
    store: Store,
    inicial: Receita,
    aoSalvar: (Receita) -> Unit,
    aoCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial.nome) }
    var rendimento by remember {
        mutableStateOf(if (inicial.rendimento > 0.0) Calculos.quantidade(inicial.rendimento) else "")
    }
    var margem by remember { mutableStateOf(inicial.margem) }
    var observacoes by remember { mutableStateOf(inicial.observacoes) }
    var escolhendoIngrediente by remember { mutableStateOf(false) }
    var tentouSalvar by remember { mutableStateOf(false) }

    val itens = remember {
        val lista = mutableStateListOf<ItemEditor>()
        for (item in inicial.itens) {
            lista.add(
                ItemEditor(
                    ingredienteId = item.ingredienteId,
                    quantidadeTexto = Calculos.quantidade(item.quantidade),
                    unidade = item.unidade
                )
            )
        }
        lista
    }

    val extras = remember {
        val lista = mutableStateListOf<ExtraEditor>()
        for (extra in inicial.extras) {
            lista.add(ExtraEditor(extra.descricao, Calculos.numero(extra.valor, 2)))
        }
        lista
    }

    val rendimentoValor = Calculos.paraDouble(rendimento)

    val receitaPrevia = Receita(
        id = inicial.id,
        criadoEm = inicial.criadoEm,
        nome = nome,
        rendimento = rendimentoValor,
        margem = margem,
        itens = itens.map { editor ->
            ItemReceita(
                ingredienteId = editor.ingredienteId,
                quantidade = Calculos.paraDouble(editor.quantidadeTexto),
                unidade = editor.unidade
            )
        },
        extras = extras.map { editor ->
            ExtraReceita(
                descricao = editor.descricao,
                valor = Calculos.paraDouble(editor.valorTexto)
            )
        }
    )
    val resumo = Calculos.resumir(receitaPrevia, store.ingredientes)

    if (escolhendoIngrediente) {
        DialogoEscolherIngrediente(
            ingredientes = store.ingredientes.toList(),
            aoEscolher = { ingrediente ->
                itens.add(
                    ItemEditor(
                        ingredienteId = ingrediente.id,
                        quantidadeTexto = "",
                        unidade = ingrediente.unidade
                    )
                )
                escolhendoIngrediente = false
            },
            aoFechar = { escolhendoIngrediente = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Secao(
            titulo = if (inicial.id > 0L) "Editar receita" else "Nova receita",
            subtitulo = "Ex.: Trufa de chocolate"
        ) {
            CampoTexto(
                rotulo = "Nome do produto",
                valor = nome,
                aoMudar = { nome = it },
                erro = if (tentouSalvar && nome.isBlank()) "Informe o nome do produto." else null
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Quantas unidades esta receita rende?",
                valor = rendimento,
                aoMudar = { rendimento = it },
                apoio = "Ex.: 20 trufas",
                erro = if (tentouSalvar && rendimentoValor <= 0.0) "Informe quantas unidades a receita rende." else null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "Ingredientes da receita",
            subtitulo = "Quanto você usa de cada um"
        ) {
            if (itens.isEmpty()) {
                Text(
                    text = "Nenhum ingrediente adicionado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave
                )
            }
            for (indice in itens.indices) {
                val editor = itens[indice]
                val ingrediente = store.ingredientePorId(editor.ingredienteId)
                val quantidadeItem = Calculos.paraDouble(editor.quantidadeTexto)
                val custoItem = if (ingrediente != null) {
                    ingrediente.converterParaBase(quantidadeItem, editor.unidade) * ingrediente.custoPorBase
                } else {
                    0.0
                }

                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ingrediente?.nome ?: "Ingrediente excluído",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextoEscuro
                            )
                            Text(
                                text = Calculos.moeda(custoItem) + " nesta receita",
                                style = MaterialTheme.typography.bodySmall,
                                color = Vinho
                            )
                        }
                        IconButton(onClick = { itens.removeAt(indice) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remover",
                                tint = VinhoClaro,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    CampoDecimal(
                        rotulo = "Quantidade usada",
                        valor = editor.quantidadeTexto,
                        aoMudar = { texto -> itens[indice] = editor.copy(quantidadeTexto = texto) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val unidadesPossiveis = if (ingrediente != null) {
                        Unidade.doGrupo(ingrediente.grupo)
                    } else {
                        Unidade.values().toList()
                    }
                    FileiraDeChips {
                        for (opcao in unidadesPossiveis) {
                            ChipSelecao(
                                texto = opcao.rotulo,
                                selecionado = editor.unidade == opcao,
                                aoClicar = { itens[indice] = editor.copy(unidade = opcao) }
                            )
                        }
                    }
                    Separador()
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            BotaoSecundario(
                texto = "+ Adicionar ingrediente",
                aoClicar = { escolhendoIngrediente = true }
            )
            if (tentouSalvar && itens.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adicione pelo menos um ingrediente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Vermelho
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "Custos extras",
            subtitulo = "Embalagem, etiqueta, gás, energia e o que mais entrar"
        ) {
            for (indice in extras.indices) {
                val editor = extras[indice]
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (editor.descricao.isBlank()) "Custo extra" else editor.descricao,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextoEscuro,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { extras.removeAt(indice) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remover",
                                tint = VinhoClaro,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    CampoTexto(
                        rotulo = "Descrição",
                        valor = editor.descricao,
                        aoMudar = { texto -> extras[indice] = editor.copy(descricao = texto) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CampoDecimal(
                        rotulo = "Valor total na receita (R$)",
                        valor = editor.valorTexto,
                        aoMudar = { texto -> extras[indice] = editor.copy(valorTexto = texto) }
                    )
                    Separador()
                }
            }

            Text(
                text = "Adicionar rapidamente:",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave
            )
            Spacer(modifier = Modifier.height(8.dp))
            FileiraDeChips {
                for (sugestao in EXTRAS_SUGERIDOS) {
                    ChipSelecao(
                        texto = sugestao,
                        selecionado = false,
                        aoClicar = { extras.add(ExtraEditor(sugestao, "")) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "+ Outro custo",
                aoClicar = { extras.add(ExtraEditor("", "")) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "Margem de lucro",
            subtitulo = "Quanto do preço de venda será lucro"
        ) {
            SeletorMargem(margem = margem, aoMudar = { margem = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Resultado", subtitulo = "Calculado automaticamente") {
            LinhaValor("Custo dos ingredientes", Calculos.moeda(resumo.custoIngredientes))
            LinhaValor("Custos extras", Calculos.moeda(resumo.custoExtras))
            LinhaValor("Custo total da produção", Calculos.moeda(resumo.custoTotal), destaque = true)
            Separador()
            LinhaValor("Unidades produzidas", Calculos.quantidade(resumo.quantidadeProduzida))
            LinhaValor("Custo de cada unidade", Calculos.moeda(resumo.custoUnitario), destaque = true)
            Separador()
            LinhaValor(
                rotulo = "Preço de venda recomendado",
                valor = Calculos.moeda(resumo.precoVenda),
                destaque = true,
                cor = Vinho
            )
            LinhaValor("Lucro por unidade", Calculos.moeda(resumo.lucroUnitario), cor = Verde)
            LinhaValor("Lucro total da receita", Calculos.moeda(resumo.lucroTotal), cor = Verde)
            if (resumo.avisos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                for (aviso in resumo.avisos) {
                    Aviso(texto = aviso, corFundo = VermelhoClaro, corTexto = Vermelho)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Observações", subtitulo = "Opcional") {
            CampoTexto(
                rotulo = "Anotações sobre a receita",
                valor = observacoes,
                aoMudar = { observacoes = it },
                linhaUnica = false
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BotaoPrincipal(
            texto = "Salvar receita",
            aoClicar = {
                tentouSalvar = true
                if (nome.isNotBlank() && rendimentoValor > 0.0 && itens.isNotEmpty()) {
                    aoSalvar(receitaPrevia.copy(nome = nome.trim(), observacoes = observacoes.trim()))
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Cancelar", aoClicar = aoCancelar)
        Spacer(modifier = Modifier.height(24.dp))
    }
}
