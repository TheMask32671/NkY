package br.com.ariane.controle.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Relatorio
import java.io.File
import java.io.FileOutputStream

/** Gera o relatorio em PDF, pronto para mandar no WhatsApp. */
object PdfRelatorio {

    private const val LARGURA = 595
    private const val ALTURA = 842
    private const val MARGEM = 42f
    private const val LIMITE_RODAPE = 760f

    private val VINHO = Color.rgb(123, 44, 67)
    private val DOURADO = Color.rgb(201, 162, 39)
    private val CINZA = Color.rgb(134, 104, 111)
    private val ROSA = Color.rgb(247, 222, 230)
    private val ESCURO = Color.rgb(58, 42, 47)
    private val VERDE = Color.rgb(46, 125, 91)

    private fun paint(cor: Int, tamanho: Float, negrito: Boolean = false, serif: Boolean = false): Paint {
        val p = Paint()
        p.color = cor
        p.textSize = tamanho
        p.isAntiAlias = true
        p.typeface = when {
            serif && negrito -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
            serif -> Typeface.SERIF
            negrito -> Typeface.DEFAULT_BOLD
            else -> Typeface.DEFAULT
        }
        return p
    }

    fun gerar(contexto: Context, relatorio: Relatorio): File {
        val documento = PdfDocument()

        val pTituloBranco = paint(Color.WHITE, 21f, negrito = true, serif = true)
        val pSubBranco = paint(Color.WHITE, 11f)
        val pProduto = paint(VINHO, 18f, negrito = true, serif = true)
        val pSecao = paint(VINHO, 13f, negrito = true, serif = true)
        val pTexto = paint(ESCURO, 11f)
        val pTextoForte = paint(ESCURO, 11f, negrito = true)
        val pCinza = paint(CINZA, 10f)
        val pValor = paint(ESCURO, 11f)
        pValor.textAlign = Paint.Align.RIGHT
        val pValorForte = paint(VINHO, 12f, negrito = true)
        pValorForte.textAlign = Paint.Align.RIGHT
        val pValorVerde = paint(VERDE, 12f, negrito = true)
        pValorVerde.textAlign = Paint.Align.RIGHT
        val pFundoVinho = Paint()
        pFundoVinho.color = VINHO
        val pFundoDourado = Paint()
        pFundoDourado.color = DOURADO
        val pFundoRosa = Paint()
        pFundoRosa.color = ROSA
        val pLinha = Paint()
        pLinha.color = ROSA
        pLinha.strokeWidth = 1f

        var numeroPagina = 1
        var pagina = documento.startPage(
            PdfDocument.PageInfo.Builder(LARGURA, ALTURA, numeroPagina).create()
        )
        var canvas: Canvas = pagina.canvas
        var y: Float

        fun desenharTopo(primeira: Boolean) {
            canvas.drawRect(0f, 0f, LARGURA.toFloat(), 86f, pFundoVinho)
            canvas.drawRect(0f, 86f, LARGURA.toFloat(), 90f, pFundoDourado)
            canvas.drawText("Ariane: Controle", MARGEM, 38f, pTituloBranco)
            canvas.drawText(
                if (primeira) "RELATÓRIO DE PRODUÇÃO" else "RELATÓRIO DE PRODUÇÃO (continuação)",
                MARGEM, 62f, pSubBranco
            )
        }

        fun desenharRodape() {
            canvas.drawLine(MARGEM, 790f, LARGURA - MARGEM, 790f, pLinha)
            canvas.drawText(
                "Gerado pelo aplicativo Ariane: Controle em " + Calculos.dataHora(relatorio.criadoEm),
                MARGEM, 806f, pCinza
            )
            val pPagina = paint(CINZA, 10f)
            pPagina.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página " + numeroPagina, LARGURA - MARGEM, 806f, pPagina)
        }

        desenharTopo(true)
        y = 128f

        fun novaPagina() {
            desenharRodape()
            documento.finishPage(pagina)
            numeroPagina += 1
            pagina = documento.startPage(
                PdfDocument.PageInfo.Builder(LARGURA, ALTURA, numeroPagina).create()
            )
            canvas = pagina.canvas
            desenharTopo(false)
            y = 128f
        }

        fun espaco(necessario: Float) {
            if (y + necessario > LIMITE_RODAPE) novaPagina()
        }

        fun linhaSimples(rotulo: String, valor: String, forte: Boolean = false, verde: Boolean = false) {
            espaco(20f)
            canvas.drawText(rotulo, MARGEM, y, if (forte) pTextoForte else pTexto)
            canvas.drawText(
                valor,
                LARGURA - MARGEM,
                y,
                when {
                    verde -> pValorVerde
                    forte -> pValorForte
                    else -> pValor
                }
            )
            y += 19f
        }

        fun separador() {
            espaco(12f)
            canvas.drawLine(MARGEM, y - 6f, LARGURA - MARGEM, y - 6f, pLinha)
            y += 8f
        }

        fun tituloSecao(texto: String) {
            espaco(34f)
            y += 10f
            canvas.drawText(texto, MARGEM, y, pSecao)
            y += 16f
        }

        // Cabecalho do relatorio
        canvas.drawText(relatorio.nomeReceita, MARGEM, y, pProduto)
        y += 20f
        canvas.drawText(Calculos.dataHora(relatorio.criadoEm), MARGEM, y, pCinza)
        y += 24f

        canvas.drawRect(MARGEM, y - 14f, LARGURA - MARGEM, y + 12f, pFundoRosa)
        canvas.drawText("Quantidade produzida", MARGEM + 10f, y + 4f, pTextoForte)
        canvas.drawText(
            Calculos.quantidade(relatorio.quantidade) + " unidades",
            LARGURA - MARGEM - 10f, y + 4f, pValorForte
        )
        y += 42f

        tituloSecao("Ingredientes utilizados")
        var temIngredientes = false
        for (linha in relatorio.linhas) {
            if (linha.extra) continue
            temIngredientes = true
            val descricao = if (linha.detalhe.isBlank()) linha.nome else linha.nome + "  (" + linha.detalhe + ")"
            linhaSimples(descricao, Calculos.moeda(linha.custo))
        }
        if (!temIngredientes) {
            linhaSimples("Nenhum ingrediente registrado", "")
        }
        separador()
        linhaSimples("Total dos ingredientes", Calculos.moeda(relatorio.custoIngredientes), forte = true)

        tituloSecao("Embalagens e outros custos")
        var temExtras = false
        for (linha in relatorio.linhas) {
            if (!linha.extra) continue
            temExtras = true
            linhaSimples(linha.nome, Calculos.moeda(linha.custo))
        }
        if (!temExtras) {
            linhaSimples("Nenhum custo extra registrado", Calculos.moeda(0.0))
        }
        separador()
        linhaSimples("Total de custos extras", Calculos.moeda(relatorio.custoExtras), forte = true)

        tituloSecao("Resumo financeiro")
        linhaSimples("Custo total da produção", Calculos.moeda(relatorio.custoTotal), forte = true)
        linhaSimples("Custo de cada unidade", Calculos.moeda(relatorio.custoUnitario), forte = true)
        linhaSimples("Margem de lucro aplicada", Calculos.porcentagem(relatorio.margem))
        linhaSimples(
            "Acréscimo sobre o custo (markup)",
            Calculos.porcentagem(Calculos.markupEquivalente(relatorio.custoUnitario, relatorio.precoVenda))
        )
        separador()
        linhaSimples("Preço de venda recomendado", Calculos.moeda(relatorio.precoVenda), forte = true)
        linhaSimples("Faturamento estimado", Calculos.moeda(relatorio.faturamento), forte = true)
        linhaSimples("Lucro estimado", Calculos.moeda(relatorio.lucro), verde = true)
        linhaSimples(
            "Lucro por unidade",
            Calculos.moeda(relatorio.precoVenda - relatorio.custoUnitario),
            verde = true
        )

        espaco(70f)
        y += 14f
        canvas.drawRect(MARGEM, y, LARGURA - MARGEM, y + 54f, pFundoRosa)
        canvas.drawText("Venda cada unidade por", MARGEM + 14f, y + 22f, pTextoForte)
        val pDestaque = paint(VINHO, 22f, negrito = true, serif = true)
        pDestaque.textAlign = Paint.Align.RIGHT
        canvas.drawText(Calculos.moeda(relatorio.precoVenda), LARGURA - MARGEM - 14f, y + 26f, pDestaque)
        canvas.drawText(
            "Lucro de " + Calculos.moeda(relatorio.precoVenda - relatorio.custoUnitario) + " em cada uma",
            MARGEM + 14f, y + 42f, pCinza
        )

        desenharRodape()
        documento.finishPage(pagina)

        val nome = "relatorio_" + Calculos.nomeArquivo(relatorio.nomeReceita) + "_" +
            Calculos.quantidade(relatorio.quantidade) + "un.pdf"
        val arquivo = File(Compartilhar.pasta(contexto, "relatorios"), nome)
        FileOutputStream(arquivo).use { saida ->
            documento.writeTo(saida)
        }
        documento.close()
        return arquivo
    }
}
