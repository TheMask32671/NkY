@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.ariane.controle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.ariane.controle.ui.ArianeTema
import br.com.ariane.controle.ui.FundoClaro
import br.com.ariane.controle.ui.TelaCalculadora
import br.com.ariane.controle.ui.TelaConfiguracoes
import br.com.ariane.controle.ui.TelaEstoque
import br.com.ariane.controle.ui.TelaIngredientes
import br.com.ariane.controle.ui.TelaInicio
import br.com.ariane.controle.ui.TelaReceitas
import br.com.ariane.controle.ui.TelaRelatorios
import br.com.ariane.controle.ui.TelaSimulador
import br.com.ariane.controle.ui.Vinho

enum class Rota(val titulo: String) {
    INICIO("Ariane: Controle"),
    INGREDIENTES("Meus Ingredientes"),
    RECEITAS("Minhas Receitas"),
    CALCULADORA("Calculadora de Custos"),
    SIMULADOR("Simulador de Produção"),
    RELATORIOS("Meus Relatórios"),
    ESTOQUE("Compras e Estoque"),
    CONFIGURACOES("Configurações")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArianeTema {
                AplicativoAriane()
            }
        }
    }
}

@Composable
fun AplicativoAriane() {
    val store = ArianeApp.store
    var rota by remember { mutableStateOf(Rota.INICIO) }
    val irPara: (Rota) -> Unit = { destino -> rota = destino }

    BackHandler(enabled = rota != Rota.INICIO) {
        rota = Rota.INICIO
    }

    Scaffold(
        containerColor = FundoClaro,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = rota.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    if (rota != Rota.INICIO) {
                        IconButton(onClick = { rota = Rota.INICIO }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Vinho,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { espacamento ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacamento)
        ) {
            when (rota) {
                Rota.INICIO -> TelaInicio(store = store, irPara = irPara)
                Rota.INGREDIENTES -> TelaIngredientes(store = store)
                Rota.RECEITAS -> TelaReceitas(store = store)
                Rota.CALCULADORA -> TelaCalculadora(store = store)
                Rota.SIMULADOR -> TelaSimulador(store = store)
                Rota.RELATORIOS -> TelaRelatorios(store = store)
                Rota.ESTOQUE -> TelaEstoque(store = store)
                Rota.CONFIGURACOES -> TelaConfiguracoes(store = store)
            }
        }
    }
}
