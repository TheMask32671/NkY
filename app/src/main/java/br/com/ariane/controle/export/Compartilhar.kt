package br.com.ariane.controle.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object Compartilhar {

    fun arquivo(contexto: Context, arquivo: File, tipo: String, assunto: String) {
        try {
            val uri = FileProvider.getUriForFile(
                contexto,
                contexto.packageName + ".fileprovider",
                arquivo
            )
            val intencao = Intent(Intent.ACTION_SEND)
            intencao.type = tipo
            intencao.putExtra(Intent.EXTRA_STREAM, uri)
            intencao.putExtra(Intent.EXTRA_SUBJECT, assunto)
            intencao.putExtra(Intent.EXTRA_TEXT, assunto)
            intencao.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val escolha = Intent.createChooser(intencao, "Compartilhar")
            escolha.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            contexto.startActivity(escolha)
        } catch (erro: Exception) {
            // Sem aplicativo para compartilhar: o arquivo continua salvo no aparelho.
        }
    }

    fun pasta(contexto: Context, nome: String): File {
        val pasta = File(contexto.cacheDir, nome)
        if (!pasta.exists()) pasta.mkdirs()
        return pasta
    }
}
