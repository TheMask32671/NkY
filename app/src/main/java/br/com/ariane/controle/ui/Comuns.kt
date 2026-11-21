@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.ariane.controle.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import br.com.ariane.controle.calc.Calculos

// ----------------------------------------------------------------- blocos

@Composable
fun Secao(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    corTitulo: Color = Vinho,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (titulo.isNotBlank()) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = corTitulo
                )
            }
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (titulo.isNotBlank() || subtitulo != null) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            conteudo()
        }
    }
}

@Composable
fun LinhaValor(
    rotulo: String,
    valor: String,
    destaque: Boolean = false,
    cor: Color = TextoEscuro,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rotulo,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (destaque) TextoEscuro else TextoSuave,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = if (destaque) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destaque) FontWeight.Bold else FontWeight.SemiBold,
            color = cor,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun Separador(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        color = RosaMedio
    )
}

// ----------------------------------------------------------------- campos

@Composable
fun CampoTexto(
    rotulo: String,
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    apoio: String? = null,
    erro: String? = null,
    linhaUnica: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = aoMudar,
            label = { Text(rotulo) },
            singleLine = linhaUnica,
            isError = erro != null,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )
        val apoioFinal = erro ?: apoio
        if (apoioFinal != null) {
            Text(
                text = apoioFinal,
                style = MaterialTheme.typography.bodySmall,
                color = if (erro != null) Vermelho else TextoSuave,
                modifier = Modifier.padding(start = 14.dp, top = 3.dp)
            )
        }
    }
}

@Composable
fun CampoDecimal(
    rotulo: String,
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier,
    apoio: String? = null,
    erro: String? = null
) {
    CampoTexto(
        rotulo = rotulo,
        valor = valor,
        aoMudar = { texto -> aoMudar(texto.filter { it.isDigit() || it == ',' || it == '.' }) },
        modifier = modifier,
        teclado = KeyboardType.Decimal,
        apoio = apoio,
        erro = erro
    )
}

// ---------------------------------------------------------------- botoes

@Composable
fun BotaoPrincipal(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cor: Color = Vinho
) {
    Button(
        onClick = aoClicar,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cor,
            contentColor = Color.White,
            disabledContainerColor = RosaMedio,
            disabledContentColor = Color.White
        )
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BotaoSecundario(
    texto: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cor: Color = Vinho
) {
    OutlinedButton(
        onClick = aoClicar,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = cor)
    ) {
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ChipSelecao(
    texto: String,
    selecionado: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selecionado,
        onClick = aoClicar,
        label = { Text(texto) },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RosaClaro,
            labelColor = Vinho,
            selectedContainerColor = Vinho,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
fun FileiraDeChips(
    modifier: Modifier = Modifier,
    conteudo: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        conteudo()
    }
}

// --------------------------------------------------------------- dialogos

@Composable
fun DialogoConfirmar(
    titulo: String,
    mensagem: String,
    textoConfirmar: String = "Confirmar",
    aoConfirmar: () -> Unit,
    aoFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text(text = titulo, style = MaterialTheme.typography.titleLarge, color = Vinho) },
        text = { Text(text = mensagem, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = aoConfirmar) {
                Text(text = textoConfirmar, color = Vermelho, fontWeight = FontWeight.Bold)
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

// ------------------------------------------------------------- mensagens

@Composable
fun Aviso(
    texto: String,
    modifier: Modifier = Modifier,
    corFundo: Color = Rosa,
    corTexto: Color = VinhoEscuro
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(corFundo, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = corTexto
        )
    }
}

@Composable
fun EstadoVazio(
    titulo: String,
    mensagem: String,
    modifier: Modifier = Modifier,
    textoBotao: String? = null,
    aoClicar: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            color = Vinho,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mensagem,
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSuave,
            textAlign = TextAlign.Center
        )
        if (textoBotao != null && aoClicar != null) {
            Spacer(modifier = Modifier.height(18.dp))
            BotaoPrincipal(texto = textoBotao, aoClicar = aoClicar)
        }
    }
}

// --------------------------------------------------------------- numeros

@Composable
fun CartaoNumero(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    detalhe: String? = null,
    corValor: Color = Vinho,
    corFundo: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = corFundo),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                color = corValor
            )
            if (detalhe != null) {
                Text(
                    text = detalhe,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave
                )
            }
        }
    }
}

// --------------------------------------------------------------- graficos

data class BarraDado(
    val rotulo: String,
    val valor: Double,
    val texto: String,
    val cor: Color = Vinho
)

@Composable
fun BarrasComparativas(
    itens: List<BarraDado>,
    modifier: Modifier = Modifier
) {
    var maior = 0.0
    for (item in itens) {
        if (item.valor > maior) maior = item.valor
    }
    Column(modifier = modifier.fillMaxWidth()) {
        for (item in itens) {
            val fracao = if (maior > 0.0) (item.valor / maior).toFloat() else 0f
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.rotulo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoEscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = item.cor
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(RosaClaro, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fracao.coerceIn(0.02f, 1f))
                            .fillMaxHeight()
                            .background(item.cor, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

data class ParteDonut(
    val rotulo: String,
    val valor: Double,
    val cor: Color
)

@Composable
fun GraficoDonut(
    partes: List<ParteDonut>,
    modifier: Modifier = Modifier
) {
    var total = 0.0
    for (parte in partes) total += parte.valor

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(118.dp)) {
            val espessura = size.minDimension * 0.24f
            val lado = size.minDimension - espessura
            val canto = Offset(espessura / 2f, espessura / 2f)
            if (total <= 0.0) {
                drawArc(
                    color = RosaClaro,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = canto,
                    size = Size(lado, lado),
                    style = Stroke(width = espessura)
                )
            } else {
                var inicio = -90f
                for (parte in partes) {
                    val varredura = ((parte.valor / total) * 360.0).toFloat()
                    if (varredura <= 0f) continue
                    drawArc(
                        color = parte.cor,
                        startAngle = inicio,
                        sweepAngle = varredura,
                        useCenter = false,
                        topLeft = canto,
                        size = Size(lado, lado),
                        style = Stroke(width = espessura, cap = StrokeCap.Butt)
                    )
                    inicio += varredura
                }
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            for (parte in partes) {
                val percentual = if (total > 0.0) parte.valor / total else 0.0
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(parte.cor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = parte.rotulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoEscuro
                        )
                        Text(
                            text = Calculos.moeda(parte.valor) + "  (" + Calculos.porcentagem(percentual) + ")",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniLinha(
    valores: List<Double>,
    modifier: Modifier = Modifier,
    cor: Color = Vinho
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        if (valores.size < 2) return@Canvas
        var minimo = valores[0]
        var maximo = valores[0]
        for (v in valores) {
            if (v < minimo) minimo = v
            if (v > maximo) maximo = v
        }
        val faixa = if (maximo - minimo < 0.000001) 1.0 else maximo - minimo
        val topo = size.height * 0.1f
        val alturaUtil = size.height * 0.8f
        val passo = size.width / (valores.size - 1).toFloat()
        val caminho = Path()
        for (i in valores.indices) {
            val x = passo * i.toFloat()
            val proporcao = ((valores[i] - minimo) / faixa).toFloat()
            val y = topo + alturaUtil - (proporcao * alturaUtil)
            if (i == 0) caminho.moveTo(x, y) else caminho.lineTo(x, y)
        }
        drawPath(path = caminho, color = cor, style = Stroke(width = 5f, cap = StrokeCap.Round))
        for (i in valores.indices) {
            val x = passo * i.toFloat()
            val proporcao = ((valores[i] - minimo) / faixa).toFloat()
            val y = topo + alturaUtil - (proporcao * alturaUtil)
            drawCircle(color = cor, radius = 6f, center = Offset(x, y))
        }
    }
}

@Composable
fun IconeRedondo(
    icone: ImageVector,
    modifier: Modifier = Modifier,
    corFundo: Color = Rosa,
    corIcone: Color = Vinho,
    tamanho: Int = 44
) {
    Box(
        modifier = modifier
            .size(tamanho.dp)
            .background(corFundo, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = corIcone,
            modifier = Modifier.size((tamanho * 0.52).dp)
        )
    }
}
