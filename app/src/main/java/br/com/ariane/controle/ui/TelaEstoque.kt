@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.ariane.controle.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun TelaEstoque(store: Store) {

    var aba by remember { mutableStateOf(0) }
    var escolhendoIngrediente by remember { mutableStateOf(false) }
    var comprandoPara by remember { mutableStateOf<Ingrediente?>(null) }
    var ajustando by remember { mutableStateOf<Ingrediente?>(null) }
    var mensagem by remember { mutableStateOf<String?>(null) }

    if (escolhendoIngrediente) {
        DialogoEscolherIngrediente(
            ingredientes = store.ingredientes.toList(),
            aoEscolher = { ingrediente ->
                comprandoPara = ingrediente
                escolhendoIngrediente = false
            },
            aoFechar = { escolhendoIngrediente = false }
        )
    }

    val ingredienteAjuste = ajustando
    if (ingredienteAjuste != null) {
        DialogoAjustarEstoque(
            ingrediente = ingredienteAjuste,
            aoSalvar = { novoEstoque ->
                store.definirEstoque(ingredienteAjuste.id, novoEstoque)
                ajustando = null
                mensagem = "Estoque de " + ingredienteAjuste.nome + " atualizado."
            },
            aoFechar = { ajustando = null }
        )
    }

    val ingredienteCompra = comprandoPara
    if (ingredienteCompra != null) {
        FormularioCompra(
            ingrediente = ingredienteCompra,
            aoSalvar = { preco, quantidade, unidade, data, atualizarPreco, somarEstoque ->
                store.registrarCompra(
                    ingrediente = ingredienteCompra,
                    precoPago = preco,
                    quantidade = quantidade,
                    unidade = unidade,
                    data = data,
                    atualizarPreco = atualizarPreco,
                    somarAoEstoque = somarEstoque
                )
                comprandoPara = null
                mensagem = "Compra registrada."
            },
            aoCancelar = { comprandoPara = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        BotaoPrincipal(
            texto = "+ Registrar compra",
            aoClicar = { escolhendoIngrediente = true }
        )

        val textoMensagem = mensagem
        if (textoMensagem != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Aviso(texto = textoMensagem, corFundo = VerdeClaro, corTexto = Verde)
        }

        Spacer(modifier = Modifier.height(14.dp))

        FileiraDeChips {
            ChipSelecao(texto = "Estoque", selecionado = aba == 0, aoClicar = { aba = 0 })
            ChipSelecao(texto = "Histórico de compras", selecionado = aba == 1, aoClicar = { aba = 1 })
            ChipSelecao(texto = "Variação de preços", selecionado = aba == 2, aoClicar = { aba = 2 })
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (store.ingredientes.isEmpty()) {
            EstadoVazio(
                titulo = "Sem ingredientes",
                mensagem = "Cadastre seus ingredientes para controlar estoque, compras e variação de preços."
            )
            return@Column
        }

        if (aba == 0) {
            val baixos = store.ingredientesComEstoqueBaixo()
            if (baixos.isNotEmpty()) {
                Aviso(
                    texto = "Precisa repor: " + baixos.joinToString(", ") { it.nome },
                    corFundo = VermelhoClaro,
                    corTexto = Vermelho
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
            Secao(titulo = "O que eu tenho", subtitulo = "Toque em um item para ajustar a quantidade") {
                for (ingrediente in store.ingredientes) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ajustando = ingrediente }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ingrediente.nome,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextoEscuro
                            )
                            Text(
                                text = Calculos.moedaDetalhada(ingrediente.custoPorBase) +
                                    " por " + ingrediente.siglaBase,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Calculos.quantidadeBaseAmigavel(ingrediente.estoque, ingrediente.grupo),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (ingrediente.estoqueBaixo) Vermelho else Vinho
                            )
                            Text(
                                text = "vale " + Calculos.moeda(ingrediente.estoque * ingrediente.custoPorBase),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                        }
                    }
                    Separador()
                }
                val valorTotal = store.ingredientes.sumOf { it.estoque * it.custoPorBase }
                LinhaValor("Valor do estoque", Calculos.moeda(valorTotal), destaque = true, cor = Vinho)
            }
        }

        if (aba == 1) {
            Secao(titulo = "Histórico de compras", subtitulo = "Total gasto: " + Calculos.moeda(store.totalGastoComIngredientes())) {
                if (store.compras.isEmpty()) {
                    Text(
                        text = "Nenhuma compra registrada ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave
                    )
                }
                for (compra in store.compras) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = compra.nomeIngrediente,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextoEscuro
                            )
                            Text(
                                text = Calculos.quantidade(compra.quantidade) + " " + compra.unidade.sigla +
                                    "  •  " + (if (compra.data.isBlank()) Calculos.dataCurta(compra.criadoEm) else compra.data),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave
                            )
                        }
                        Text(
                            text = Calculos.moeda(compra.precoPago),
                            style = MaterialTheme.typography.titleSmall,
                            color = Vinho
                        )
                        IconButton(onClick = { store.excluirCompra(compra.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Excluir",
                                tint = VinhoClaro,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Separador()
                }
            }
        }

        if (aba == 2) {
            Secao(
                titulo = "Variação de preços",
                subtitulo = "Como o preço dos seus ingredientes mudou"
            ) {
                var algum = false
                for (ingrediente in store.ingredientes) {
                    val compras = store.comprasDoIngrediente(ingrediente.id).reversed()
                    if (compras.size < 2) continue
                    algum = true
                    val primeiro = compras.first().custoBase
                    val ultimo = compras.last().custoBase
                    val variacao = if (primeiro > 0.0) (ultimo - primeiro) / primeiro else 0.0

                    Text(
                        text = ingrediente.nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextoEscuro,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        text = "De " + Calculos.moedaDetalhada(primeiro) + " para " +
                            Calculos.moedaDetalhada(ultimo) + " por " + ingrediente.siglaBase,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                    Text(
                        text = (if (variacao >= 0.0) "Subiu " else "Caiu ") +
                            Calculos.porcentagem(if (variacao < 0.0) -variacao else variacao, 1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (variacao > 0.0) Vermelho else Verde
                    )
                    MiniLinha(valores = compras.map { it.custoBase })
                    Separador()
                }
                if (!algum) {
                    Text(
                        text = "Registre pelo menos duas compras do mesmo ingrediente para ver a variação de preço.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DialogoAjustarEstoque(
    ingrediente: Ingrediente,
    aoSalvar: (Double) -> Unit,
    aoFechar: () -> Unit
) {
    var texto by remember { mutableStateOf(Calculos.quantidade(ingrediente.estoque)) }
    AlertDialog(
        onDismissRequest = aoFechar,
        title = {
            Text(
                text = "Estoque de " + ingrediente.nome,
                style = MaterialTheme.typography.titleLarge,
                color = Vinho
            )
        },
        text = {
            Column {
                CampoDecimal(
                    rotulo = "Quantidade em " + ingrediente.siglaBase,
                    valor = texto,
                    aoMudar = { texto = it },
                    apoio = "Use a unidade base: " + ingrediente.siglaBase
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { aoSalvar(Calculos.paraDouble(texto)) }) {
                Text(text = "Salvar", color = Vinho)
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text(text = "Cancelar", color = TextoSuave)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun FormularioCompra(
    ingrediente: Ingrediente,
    aoSalvar: (Double, Double, Unidade, String, Boolean, Boolean) -> Unit,
    aoCancelar: () -> Unit
) {
    var preco by remember { mutableStateOf("") }
    var quantidade by remember { mutableStateOf("") }
    var unidade by remember { mutableStateOf(ingrediente.unidade) }
    var data by remember { mutableStateOf(Calculos.hoje()) }
    var atualizarPreco by remember { mutableStateOf(true) }
    var somarEstoque by remember { mutableStateOf(true) }
    var tentou by remember { mutableStateOf(false) }

    val precoValor = Calculos.paraDouble(preco)
    val quantidadeValor = Calculos.paraDouble(quantidade)
    val base = ingrediente.converterParaBase(quantidadeValor, unidade)
    val custoBase = if (base > 0.0) precoValor / base else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Secao(
            titulo = "Comprei " + ingrediente.nome,
            subtitulo = "Preço atual: " + Calculos.moedaDetalhada(ingrediente.custoPorBase) + " por " + ingrediente.siglaBase
        ) {
            CampoDecimal(
                rotulo = "Quanto paguei (R$)",
                valor = preco,
                aoMudar = { preco = it },
                erro = if (tentou && precoValor <= 0.0) "Informe o valor pago." else null
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoDecimal(
                rotulo = "Quantidade comprada",
                valor = quantidade,
                aoMudar = { quantidade = it },
                erro = if (tentou && quantidadeValor <= 0.0) "Informe a quantidade." else null
            )
            Spacer(modifier = Modifier.height(12.dp))
            FileiraDeChips {
                for (opcao in Unidade.doGrupo(ingrediente.grupo)) {
                    ChipSelecao(
                        texto = opcao.rotulo,
                        selecionado = unidade == opcao,
                        aoClicar = { unidade = opcao }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            CampoTexto(
                rotulo = "Data da compra",
                valor = data,
                aoMudar = { data = it },
                teclado = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(14.dp))
            Aviso(
                texto = "Novo custo: " + Calculos.moedaDetalhada(custoBase) + " por " + ingrediente.siglaBase +
                    (if (custoBase > ingrediente.custoPorBase && ingrediente.custoPorBase > 0.0) "  (mais caro que antes)" else "")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "O que fazer com essa compra") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = atualizarPreco,
                    onCheckedChange = { atualizarPreco = it },
                    colors = CheckboxDefaults.colors(checkedColor = Vinho)
                )
                Text(
                    text = "Atualizar o preço nas receitas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoEscuro
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = somarEstoque,
                    onCheckedChange = { somarEstoque = it },
                    colors = CheckboxDefaults.colors(checkedColor = Vinho)
                )
                Text(
                    text = "Somar ao meu estoque",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoEscuro
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Os relatórios já salvos continuam com os valores da época.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        BotaoPrincipal(
            texto = "Salvar compra",
            aoClicar = {
                tentou = true
                if (precoValor > 0.0 && quantidadeValor > 0.0) {
                    aoSalvar(precoValor, quantidadeValor, unidade, data.trim(), atualizarPreco, somarEstoque)
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        BotaoSecundario(texto = "Cancelar", aoClicar = aoCancelar)
        Spacer(modifier = Modifier.height(24.dp))
    }
}
