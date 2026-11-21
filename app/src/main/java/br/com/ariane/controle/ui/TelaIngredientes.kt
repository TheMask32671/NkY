package br.com.ariane.controle.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Ingrediente
import br.com.ariane.controle.data.Store
import br.com.ariane.controle.data.Unidade

@Composable
fun TelaIngredientes(store: Store) {

    var emEdicao by remember { mutableStateOf<Ingrediente?>(null) }
    var paraExcluir by remember { mutableStateOf<Ingrediente?>(null) }
    var busca by remember { mutableStateOf("") }

    val alvoExclusao = paraExcluir
    if (alvoExclusao != null) {
        DialogoConfirmar(
            titulo = "Excluir ingrediente",
            mensagem = "Tem certeza que deseja excluir \"" + alvoExclusao.nome +
                "\"? Ele também sai das receitas que o utilizam.",
            textoConfirmar = "Excluir",
            aoConfirmar = {
                store.excluirIngrediente(alvoExclusao.id)
                paraExcluir = null
            },
            aoFechar = { paraExcluir = null }
        )
    }

    val emEdicaoAgora = emEdicao
    if (emEdicaoAgora != null) {
        FormularioIngrediente(
            inicial = emEdicaoAgora,
            aoSalvar = { ingrediente ->
                store.salvarIngrediente(ingrediente)
                emEdicao = null
            },
            aoCancelar = { emEdicao = null }
        )
        return
    }

    val filtrados = store.ingredientes.filter { ingrediente ->
        busca.isBlank() ||
            ingrediente.nome.contains(busca, ignoreCase = true) ||
            ingrediente.marca.contains(busca, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            BotaoPrincipal(
                texto = "+ Cadastrar ingrediente",
                aoClicar = { emEdicao = Ingrediente(dataCompra = Calculos.hoje()) }
            )
            if (store.ingredientes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CampoTexto(
                    rotulo = "Procurar ingrediente",
                    valor = busca,
                    aoMudar = { busca = it }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (store.ingredientes.isEmpty()) {
            EstadoVazio(
                titulo = "Nenhum ingrediente ainda",
                mensagem = "Cadastre o que você compra (chocolate, leite condensado, embalagens) " +
                    "com o preço e a quantidade. O aplicativo descobre sozinho quanto custa cada grama."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados, key = { it.id }) { ingrediente ->
                    CartaoIngrediente(
                        ingrediente = ingrediente,
                        aoEditar = { emEdicao = ingrediente },
                        aoExcluir = { paraExcluir = ingrediente }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoIngrediente(
    ingrediente: Ingrediente,
    aoEditar: () -> Unit,
    aoExcluir: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { aoEditar() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingrediente.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoEscuro
                )
                if (ingrediente.marca.isNotBlank()) {
                    Text(
                        text = ingrediente.marca,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = Calculos.moedaDetalhada(ingrediente.custoPorBase) + " por " + ingrediente.siglaBase,
                    style = MaterialTheme.typography.titleSmall,
                    color = Vinho
                )
                Text(
                    text = "Comprou " + Calculos.quantidade(ingrediente.quantidadeComprada) + " " +
                        ingrediente.unidade.sigla + " por " + Calculos.moeda(ingrediente.precoPago),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
                Text(
                    text = "Estoque: " + Calculos.quantidadeBaseAmigavel(ingrediente.estoque, ingrediente.grupo),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ingrediente.estoqueBaixo) Vermelho else TextoSuave
                )
            }
            IconButton(onClick = aoExcluir) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir",
                    tint = VinhoClaro,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FormularioIngrediente(
    inicial: Ingrediente,
    aoSalvar: (Ingrediente) -> Unit,
    aoCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(inicial.nome) }
    var marca by remember { mutableStateOf(inicial.marca) }
    var preco by remember {
        mutableStateOf(if (inicial.precoPago > 0.0) Calculos.numero(inicial.precoPago, 2) else "")
    }
    var quantidade by remember {
        mutableStateOf(if (inicial.quantidadeComprada > 0.0) Calculos.quantidade(inicial.quantidadeComprada) else "")
    }
    var unidade by remember { mutableStateOf(inicial.unidade) }
    var porPacote by remember {
        mutableStateOf(if (inicial.unidadesPorPacote > 0.0) Calculos.quantidade(inicial.unidadesPorPacote) else "")
    }
    var data by remember { mutableStateOf(inicial.dataCompra) }
    var estoque by remember {
        mutableStateOf(if (inicial.estoque > 0.0) Calculos.quantidade(inicial.estoque) else "")
    }
    var estoqueMinimo by remember {
        mutableStateOf(if (inicial.estoqueMinimo > 0.0) Calculos.quantidade(inicial.estoqueMinimo) else "")
    }
    var tentouSalvar by remember { mutableStateOf(false) }

    val precoValor = Calculos.paraDouble(preco)
    val quantidadeValor = Calculos.paraDouble(quantidade)
    val porPacoteValor = Calculos.paraDouble(porPacote)

    val erroNome = if (tentouSalvar && nome.isBlank()) "Informe o nome do ingrediente." else null
    val erroPreco = if (tentouSalvar && precoValor <= 0.0) "Informe quanto você pagou." else null
    val erroQuantidade = if (tentouSalvar && quantidadeValor <= 0.0) "Informe a quantidade comprada." else null

    val previa = Ingrediente(
        precoPago = precoValor,
        quantidadeComprada = quantidadeValor,
        unidade = unidade,
        unidadesPorPacote = porPacoteValor
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Secao(
            titulo = if (inicial.id > 0L) "Editar ingrediente" else "Novo ingrediente",
            subtitulo = "Informe o preço e a quantidade da embalagem que você comprou"
        ) {
            CampoTexto(rotulo = "Nome (ex.: Chocolate ao leite)", valor = nome, aoMudar = { nome = it }, erro = erroNome)
            Spacer(modifier = Modifier.height(12.dp))
            CampoTexto(rotulo = "Marca (opcional)", valor = marca, aoMudar = { marca = it })
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Preço pago (R$)",
                valor = preco,
                aoMudar = { preco = it },
                erro = erroPreco
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Quantidade comprada",
                valor = quantidade,
                aoMudar = { quantidade = it },
                erro = erroQuantidade
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unidade de medida",
                style = MaterialTheme.typography.titleSmall,
                color = TextoEscuro
            )
            Spacer(modifier = Modifier.height(8.dp))
            FileiraDeChips {
                for (opcao in Unidade.values()) {
                    ChipSelecao(
                        texto = opcao.rotulo,
                        selecionado = unidade == opcao,
                        aoClicar = { unidade = opcao }
                    )
                }
            }
            if (unidade == Unidade.PACOTE) {
                Spacer(modifier = Modifier.height(12.dp))
                CampoDecimal(
                    rotulo = "Unidades por pacote (opcional)",
                    valor = porPacote,
                    aoMudar = { porPacote = it },
                    apoio = "Ex.: um pacote com 100 forminhas. Assim o app calcula o custo de cada forminha."
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTexto(
                rotulo = "Data da compra (opcional)",
                valor = data,
                aoMudar = { data = it },
                teclado = KeyboardType.Number,
                apoio = "Formato dia/mês/ano"
            )

            Spacer(modifier = Modifier.height(16.dp))
            Aviso(
                texto = "Custo calculado: " + Calculos.moedaDetalhada(previa.custoPorBase) +
                    " por " + previa.siglaBase +
                    "  •  equivale a " + Calculos.moeda(previa.custoPorBase * 1000.0) +
                    " por " + (if (previa.grupo.siglaBase == "g") "kg" else if (previa.grupo.siglaBase == "ml") "litro" else "1000 un")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Estoque", subtitulo = "Para saber quando precisa repor") {
            CampoDecimal(
                rotulo = "Estoque atual (em " + previa.siglaBase + ")",
                valor = estoque,
                aoMudar = { estoque = it },
                apoio = "Se deixar em branco, usamos a quantidade comprada."
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Avisar quando sobrar menos de (em " + previa.siglaBase + ")",
                valor = estoqueMinimo,
                aoMudar = { estoqueMinimo = it },
                apoio = "Opcional."
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BotaoPrincipal(
            texto = "Salvar ingrediente",
            aoClicar = {
                tentouSalvar = true
                if (nome.isNotBlank() && precoValor > 0.0 && quantidadeValor > 0.0) {
                    val estoqueDigitado = Calculos.paraDouble(estoque)
                    val estoqueFinal = if (estoque.isBlank() && inicial.id <= 0L) {
                        previa.quantidadeBase
                    } else {
                        estoqueDigitado
                    }
                    aoSalvar(
                        inicial.copy(
                            nome = nome.trim(),
                            marca = marca.trim(),
                            precoPago = precoValor,
                            quantidadeComprada = quantidadeValor,
                            unidade = unidade,
                            unidadesPorPacote = porPacoteValor,
                            dataCompra = data.trim(),
                            estoque = estoqueFinal,
                            estoqueMinimo = Calculos.paraDouble(estoqueMinimo)
                        )
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Cancelar", aoClicar = aoCancelar)
        Spacer(modifier = Modifier.height(24.dp))
    }
}
