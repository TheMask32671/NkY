@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.ariane.controle.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Ingrediente
import br.com.ariane.controle.data.Receita
import kotlin.math.abs

/** Escolha de margem com os percentuais prontos e a opção de digitar outro. */
@Composable
fun SeletorMargem(
    margem: Double,
    aoMudar: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val opcoes = listOf(0.20, 0.30, 0.40, 0.50, 0.60)
    var personalizado by remember {
        val ehPadrao = opcoes.any { abs(it - margem) < 0.0001 }
        mutableStateOf(if (!ehPadrao && margem > 0.0) Calculos.numero(margem * 100.0, 0) else "")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FileiraDeChips {
            for (opcao in opcoes) {
                ChipSelecao(
                    texto = Calculos.porcentagem(opcao),
                    selecionado = abs(margem - opcao) < 0.0001,
                    aoClicar = {
                        personalizado = ""
                        aoMudar(opcao)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        CampoDecimal(
            rotulo = "Outra margem (%)",
            valor = personalizado,
            aoMudar = { texto ->
                personalizado = texto
                val valor = Calculos.paraDouble(texto) / 100.0
                if (valor > 0.0) aoMudar(Calculos.limitarMargem(valor))
            },
            apoio = "Margem sobre o preço de venda (máximo 95%)."
        )
    }
}

/** Lista de ingredientes para escolher dentro de uma receita ou compra. */
@Composable
fun DialogoEscolherIngrediente(
    ingredientes: List<Ingrediente>,
    aoEscolher: (Ingrediente) -> Unit,
    aoFechar: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val filtrados = ingredientes.filter {
        busca.isBlank() || it.nome.contains(busca, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = {
            Text(
                text = "Escolher ingrediente",
                style = MaterialTheme.typography.titleLarge,
                color = Vinho
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (ingredientes.isEmpty()) {
                    Text(
                        text = "Você ainda não cadastrou ingredientes. Cadastre primeiro em \"Meus Ingredientes\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave
                    )
                } else {
                    CampoTexto(rotulo = "Procurar", valor = busca, aoMudar = { busca = it })
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtrados, key = { it.id }) { ingrediente ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { aoEscolher(ingrediente) }
                                    .padding(vertical = 10.dp)
                            ) {
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
                            HorizontalDivider(color = RosaClaro)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = aoFechar) {
                Text(text = "Fechar", color = Vinho)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

/** Lista de receitas para escolher na calculadora, no simulador e nos relatórios. */
@Composable
fun DialogoEscolherReceita(
    receitas: List<Receita>,
    aoEscolher: (Receita) -> Unit,
    aoFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = {
            Text(
                text = "Escolher produto",
                style = MaterialTheme.typography.titleLarge,
                color = Vinho
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (receitas.isEmpty()) {
                    Text(
                        text = "Nenhuma receita cadastrada ainda. Crie uma em \"Minhas Receitas\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 340.dp)) {
                        items(receitas, key = { it.id }) { receita ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { aoEscolher(receita) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = receita.nome,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextoEscuro
                                )
                                Text(
                                    text = "Rende " + Calculos.quantidade(receita.rendimento) + " unidades",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextoSuave
                                )
                            }
                            HorizontalDivider(color = RosaClaro)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = aoFechar) {
                Text(text = "Fechar", color = Vinho)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
