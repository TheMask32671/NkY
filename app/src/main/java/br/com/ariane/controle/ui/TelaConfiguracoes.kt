package br.com.ariane.controle.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Store
import br.com.ariane.controle.export.Compartilhar
import br.com.ariane.controle.export.Csv
import java.io.File

@Composable
fun TelaConfiguracoes(store: Store) {

    val contexto = LocalContext.current
    var mensagem by remember { mutableStateOf<String?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var confirmandoApagar by remember { mutableStateOf(false) }

    val salvarBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { destino ->
        if (destino != null) {
            try {
                contexto.contentResolver.openOutputStream(destino)?.use { saida ->
                    saida.write(store.gerarBackup().toByteArray(Charsets.UTF_8))
                }
                mensagem = "Backup salvo no aparelho."
                erro = null
            } catch (falha: Exception) {
                erro = "Não foi possível salvar o backup."
            }
        }
    }

    val abrirBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { origem ->
        if (origem != null) {
            try {
                val texto = contexto.contentResolver.openInputStream(origem)?.use { entrada ->
                    entrada.readBytes().toString(Charsets.UTF_8)
                }
                if (texto != null && store.restaurarBackup(texto)) {
                    mensagem = "Backup restaurado com sucesso."
                    erro = null
                } else {
                    erro = "Esse arquivo não parece ser um backup do Ariane: Controle."
                }
            } catch (falha: Exception) {
                erro = "Não foi possível ler o arquivo."
            }
        }
    }

    if (confirmandoApagar) {
        DialogoConfirmar(
            titulo = "Apagar todos os dados",
            mensagem = "Isso apaga ingredientes, receitas, compras e relatórios deste aparelho. " +
                "Essa ação não pode ser desfeita. Tem certeza?",
            textoConfirmar = "Apagar tudo",
            aoConfirmar = {
                store.apagarTudo()
                confirmandoApagar = false
                mensagem = "Todos os dados foram apagados."
            },
            aoFechar = { confirmandoApagar = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Secao(
            titulo = "Margem de lucro padrão",
            subtitulo = "Usada quando você cria uma receita nova"
        ) {
            SeletorMargem(
                margem = store.margemPadrao,
                aoMudar = { store.definirMargemPadrao(it) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Atualmente: " + Calculos.porcentagem(store.margemPadrao),
                style = MaterialTheme.typography.bodyMedium,
                color = Vinho
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(
            titulo = "Backup dos meus dados",
            subtitulo = "Tudo fica salvo no aparelho. O backup protege se você trocar de celular."
        ) {
            BotaoPrincipal(
                texto = "Salvar backup no aparelho",
                aoClicar = { salvarBackup.launch("ariane-controle-backup.json") }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Enviar backup (WhatsApp, e-mail...)",
                aoClicar = {
                    try {
                        val pasta = Compartilhar.pasta(contexto, "backup")
                        val arquivo = File(pasta, "ariane-controle-backup.json")
                        arquivo.writeText(store.gerarBackup(), Charsets.UTF_8)
                        Compartilhar.arquivo(
                            contexto,
                            arquivo,
                            "application/json",
                            "Backup do Ariane: Controle"
                        )
                    } catch (falha: Exception) {
                        erro = "Não foi possível gerar o backup."
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Restaurar backup",
                aoClicar = { abrirBackup.launch(arrayOf("*/*")) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Ao restaurar, os dados atuais são substituídos pelos do arquivo.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Exportar planilhas", subtitulo = "Arquivos CSV que abrem no Excel") {
            BotaoSecundario(
                texto = "Planilha de ingredientes",
                aoClicar = {
                    val arquivo = Csv.dosIngredientes(contexto, store.ingredientes.toList())
                    Compartilhar.arquivo(contexto, arquivo, "text/csv", "Meus ingredientes")
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Planilha de receitas e preços",
                aoClicar = {
                    val arquivo = Csv.dasReceitas(
                        contexto,
                        store.receitas.toList(),
                        store.ingredientes.toList()
                    )
                    Compartilhar.arquivo(contexto, arquivo, "text/csv", "Minhas receitas")
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BotaoSecundario(
                texto = "Planilha de relatórios",
                aoClicar = {
                    val arquivo = Csv.dosRelatorios(contexto, store.relatorios.toList())
                    Compartilhar.arquivo(contexto, arquivo, "text/csv", "Meus relatórios")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Meus números") {
            LinhaValor("Ingredientes cadastrados", store.ingredientes.size.toString())
            LinhaValor("Receitas cadastradas", store.receitas.size.toString())
            LinhaValor("Compras registradas", store.compras.size.toString())
            LinhaValor("Relatórios guardados", store.relatorios.size.toString())
            LinhaValor("Total gasto com ingredientes", Calculos.moeda(store.totalGastoComIngredientes()), destaque = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Zona de cuidado") {
            BotaoSecundario(
                texto = "Apagar todos os dados",
                aoClicar = { confirmandoApagar = true },
                cor = Vermelho
            )
        }

        val textoMensagem = mensagem
        if (textoMensagem != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Aviso(texto = textoMensagem, corFundo = VerdeClaro, corTexto = Verde)
        }
        val textoErro = erro
        if (textoErro != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Aviso(texto = textoErro, corFundo = VermelhoClaro, corTexto = Vermelho)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Secao(titulo = "Sobre o aplicativo") {
            Text(
                text = "Ariane: Controle",
                style = MaterialTheme.typography.titleLarge,
                color = Vinho
            )
            Text(
                text = "Versão 1.0  •  funciona sem internet",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Seus dados ficam guardados só neste celular. Nada é enviado para a internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoEscuro
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
