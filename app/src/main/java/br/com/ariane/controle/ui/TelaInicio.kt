package br.com.ariane.controle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.R
import br.com.ariane.controle.Rota
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.calc.Resumo
import br.com.ariane.controle.data.Receita
import br.com.ariane.controle.data.Store

private data class ItemDoMenu(
    val rota: Rota,
    val titulo: String,
    val descricao: String,
    val icone: Int
)

private val ITENS_DO_MENU = listOf(
    ItemDoMenu(Rota.INGREDIENTES, "Ingredientes", "Preços e custo por grama", R.drawable.ic_ingredientes),
    ItemDoMenu(Rota.RECEITAS, "Minhas Receitas", "Monte seus produtos", R.drawable.ic_receitas),
    ItemDoMenu(Rota.CALCULADORA, "Calculadora", "Custo, margem e preço", R.drawable.ic_calculadora),
    ItemDoMenu(Rota.SIMULADOR, "Simulador", "Quanto vou gastar?", R.drawable.ic_simulador),
    ItemDoMenu(Rota.RELATORIOS, "Relatórios", "PDF e planilha CSV", R.drawable.ic_relatorios),
    ItemDoMenu(Rota.ESTOQUE, "Compras e Estoque", "Controle do que tem", R.drawable.ic_estoque),
    ItemDoMenu(Rota.CONFIGURACOES, "Configurações", "Backup e preferências", R.drawable.ic_config)
)

@Composable
fun TelaInicio(store: Store, irPara: (Rota) -> Unit) {

    val receitas: List<Receita> = store.receitas.toList()
    val resumos: List<Resumo> = receitas.map { receita ->
        Calculos.resumir(receita, store.ingredientes)
    }

    val totalGasto = store.totalGastoComIngredientes()
    val comCusto = resumos.filter { it.custoUnitario > 0.0 }
    val custoMedio = if (comCusto.isEmpty()) 0.0 else comCusto.sumOf { it.custoUnitario } / comCusto.size
    val lucroEstimado = resumos.sumOf { it.lucroTotal }
    val estoqueBaixo = store.ingredientesComEstoqueBaixo()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        CabecalhoAriane()

        Spacer(modifier = Modifier.height(16.dp))

        if (store.ingredientes.isEmpty() && receitas.isEmpty()) {
            Secao(titulo = "Comece por aqui", subtitulo = "Três passos e o preço de venda aparece sozinho") {
                PassoSimples(1, "Cadastre seus ingredientes com o preço que você pagou.")
                PassoSimples(2, "Monte a receita dizendo quanto usa de cada um e quantas unidades ela rende.")
                PassoSimples(3, "Escolha a margem de lucro. O aplicativo calcula o preço de venda.")
                Spacer(modifier = Modifier.height(14.dp))
                BotaoPrincipal(
                    texto = "Cadastrar meu primeiro ingrediente",
                    aoClicar = { irPara(Rota.INGREDIENTES) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoNumero(
                titulo = "Total gasto com ingredientes",
                valor = Calculos.moeda(totalGasto),
                modifier = Modifier.weight(1f)
            )
            CartaoNumero(
                titulo = "Custo médio por unidade",
                valor = Calculos.moeda(custoMedio),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoNumero(
                titulo = "Produtos cadastrados",
                valor = receitas.size.toString(),
                detalhe = store.ingredientes.size.toString() + " ingredientes",
                modifier = Modifier.weight(1f)
            )
            CartaoNumero(
                titulo = "Lucro estimado",
                valor = Calculos.moeda(lucroEstimado),
                detalhe = "uma receita de cada produto",
                corValor = Verde,
                modifier = Modifier.weight(1f)
            )
        }

        if (estoqueBaixo.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Aviso(
                texto = "Estoque baixo: " + estoqueBaixo.joinToString(", ") { it.nome },
                corFundo = VermelhoClaro,
                corTexto = Vermelho
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Menu principal",
            style = MaterialTheme.typography.headlineSmall,
            color = Vinho
        )
        Spacer(modifier = Modifier.height(10.dp))

        val linhas = ITENS_DO_MENU.chunked(2)
        for (linha in linhas) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (item in linha) {
                    BotaoDoMenu(
                        item = item,
                        modifier = Modifier.weight(1f),
                        aoClicar = { irPara(item.rota) }
                    )
                }
                if (linha.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (resumos.isNotEmpty()) {

            val custoIngredientes = resumos.sumOf { it.custoIngredientes }
            val custoExtras = resumos.sumOf { it.custoExtras }

            Spacer(modifier = Modifier.height(6.dp))
            Secao(
                titulo = "Composição dos custos",
                subtitulo = "Somando uma receita de cada produto"
            ) {
                GraficoDonut(
                    partes = listOf(
                        ParteDonut("Ingredientes", custoIngredientes, Vinho),
                        ParteDonut("Embalagens e extras", custoExtras, Dourado)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val ordenados = receitas.indices
                .map { indice -> Pair(receitas[indice], resumos[indice]) }
                .sortedByDescending { it.second.lucroUnitario }
                .take(5)

            Secao(
                titulo = "Produtos mais rentáveis",
                subtitulo = "Lucro em cada unidade vendida"
            ) {
                BarrasComparativas(
                    itens = ordenados.map { par ->
                        BarraDado(
                            rotulo = par.first.nome,
                            valor = par.second.lucroUnitario,
                            texto = Calculos.moeda(par.second.lucroUnitario),
                            cor = Verde
                        )
                    }
                )
                Separador()
                for (par in ordenados) {
                    LinhaValor(
                        rotulo = par.first.nome + "  •  margem " + Calculos.porcentagem(par.second.margem),
                        valor = "vender a " + Calculos.moeda(par.second.precoVenda)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Secao(titulo = "Custo x preço de venda", subtitulo = "Por unidade") {
                for (par in ordenados) {
                    Text(
                        text = par.first.nome,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextoEscuro,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    BarrasComparativas(
                        itens = listOf(
                            BarraDado("Custo", par.second.custoUnitario, Calculos.moeda(par.second.custoUnitario), VinhoClaro),
                            BarraDado("Preço", par.second.precoVenda, Calculos.moeda(par.second.precoVenda), Dourado)
                        )
                    )
                }
            }
        }

        val ultimos = store.relatorios.take(3)
        if (ultimos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Secao(titulo = "Últimos relatórios") {
                for (relatorio in ultimos) {
                    LinhaValor(
                        rotulo = relatorio.nomeReceita + "  •  " + Calculos.quantidade(relatorio.quantidade) + " un",
                        valor = Calculos.moeda(relatorio.lucro),
                        cor = Verde
                    )
                    Text(
                        text = Calculos.dataHora(relatorio.criadoEm),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave
                    )
                    Separador()
                }
                BotaoSecundario(texto = "Ver todos os relatórios", aoClicar = { irPara(Rota.RELATORIOS) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CabecalhoAriane() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Vinho),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_monograma),
                    contentDescription = null,
                    tint = Vinho,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Ariane: Controle",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = "Seus custos, sua margem, seu lucro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RosaMedio
                )
            }
        }
    }
}

@Composable
private fun PassoSimples(numero: Int, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(Rosa, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = Vinho
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoEscuro,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BotaoDoMenu(
    item: ItemDoMenu,
    modifier: Modifier = Modifier,
    aoClicar: () -> Unit
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .clickable { aoClicar() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Rosa, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.icone),
                    contentDescription = null,
                    tint = Vinho,
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.titleSmall,
                color = TextoEscuro,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.descricao,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
