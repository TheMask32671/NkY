package br.com.ariane.controle.export

import android.content.Context
import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.Ingrediente
import br.com.ariane.controle.data.Receita
import br.com.ariane.controle.data.Relatorio
import java.io.File

/**
 * Planilhas em CSV separadas por ponto e virgula,
 * que e o formato que o Excel brasileiro abre direto.
 */
object Csv {

    private const val SEPARADOR = ";"

    private fun campo(texto: String): String {
        val limpo = texto.replace("\"", "'").replace("\n", " ")
        return "\"" + limpo + "\""
    }

    private fun numero(valor: Double): String = Calculos.numero(valor, 2).replace(".", "")

    private fun salvar(contexto: Context, nome: String, conteudo: String): File {
        val pasta = Compartilhar.pasta(contexto, "planilhas")
        val arquivo = File(pasta, nome)
        arquivo.writeText("﻿" + conteudo, Charsets.UTF_8)
        return arquivo
    }

    fun doRelatorio(contexto: Context, relatorio: Relatorio): File {
        val linhas = StringBuilder()
        linhas.append(campo("RELATÓRIO DE PRODUÇÃO")).append(SEPARADOR)
            .append(campo(relatorio.nomeReceita)).append("\n")
        linhas.append(campo("Gerado em")).append(SEPARADOR)
            .append(campo(Calculos.dataHora(relatorio.criadoEm))).append("\n")
        linhas.append(campo("Quantidade produzida")).append(SEPARADOR)
            .append(campo(Calculos.quantidade(relatorio.quantidade))).append("\n\n")

        linhas.append(campo("Item")).append(SEPARADOR)
            .append(campo("Quantidade")).append(SEPARADOR)
            .append(campo("Custo (R$)")).append("\n")
        for (linha in relatorio.linhas) {
            linhas.append(campo(linha.nome)).append(SEPARADOR)
                .append(campo(linha.detalhe)).append(SEPARADOR)
                .append(numero(linha.custo)).append("\n")
        }

        linhas.append("\n")
        linhas.append(campo("Custo dos ingredientes")).append(SEPARADOR).append(numero(relatorio.custoIngredientes)).append("\n")
        linhas.append(campo("Custos extras")).append(SEPARADOR).append(numero(relatorio.custoExtras)).append("\n")
        linhas.append(campo("Custo total")).append(SEPARADOR).append(numero(relatorio.custoTotal)).append("\n")
        linhas.append(campo("Custo por unidade")).append(SEPARADOR).append(numero(relatorio.custoUnitario)).append("\n")
        linhas.append(campo("Margem de lucro")).append(SEPARADOR).append(campo(Calculos.porcentagem(relatorio.margem))).append("\n")
        linhas.append(campo("Preço de venda")).append(SEPARADOR).append(numero(relatorio.precoVenda)).append("\n")
        linhas.append(campo("Faturamento estimado")).append(SEPARADOR).append(numero(relatorio.faturamento)).append("\n")
        linhas.append(campo("Lucro estimado")).append(SEPARADOR).append(numero(relatorio.lucro)).append("\n")

        val nome = "relatorio_" + Calculos.nomeArquivo(relatorio.nomeReceita) + ".csv"
        return salvar(contexto, nome, linhas.toString())
    }

    fun dosIngredientes(contexto: Context, ingredientes: List<Ingrediente>): File {
        val linhas = StringBuilder()
        linhas.append(campo("Ingrediente")).append(SEPARADOR)
            .append(campo("Marca")).append(SEPARADOR)
            .append(campo("Preço pago (R$)")).append(SEPARADOR)
            .append(campo("Quantidade")).append(SEPARADOR)
            .append(campo("Unidade")).append(SEPARADOR)
            .append(campo("Custo por unidade base (R$)")).append(SEPARADOR)
            .append(campo("Unidade base")).append(SEPARADOR)
            .append(campo("Estoque")).append(SEPARADOR)
            .append(campo("Data da compra")).append("\n")
        for (i in ingredientes) {
            linhas.append(campo(i.nome)).append(SEPARADOR)
                .append(campo(i.marca)).append(SEPARADOR)
                .append(numero(i.precoPago)).append(SEPARADOR)
                .append(campo(Calculos.quantidade(i.quantidadeComprada))).append(SEPARADOR)
                .append(campo(i.unidade.sigla)).append(SEPARADOR)
                .append(Calculos.numero(i.custoPorBase, 4).replace(".", "")).append(SEPARADOR)
                .append(campo(i.siglaBase)).append(SEPARADOR)
                .append(campo(Calculos.quantidade(i.estoque))).append(SEPARADOR)
                .append(campo(i.dataCompra)).append("\n")
        }
        return salvar(contexto, "ingredientes.csv", linhas.toString())
    }

    fun dasReceitas(contexto: Context, receitas: List<Receita>, ingredientes: List<Ingrediente>): File {
        val linhas = StringBuilder()
        linhas.append(campo("Produto")).append(SEPARADOR)
            .append(campo("Rendimento")).append(SEPARADOR)
            .append(campo("Custo total (R$)")).append(SEPARADOR)
            .append(campo("Custo por unidade (R$)")).append(SEPARADOR)
            .append(campo("Margem")).append(SEPARADOR)
            .append(campo("Preço de venda (R$)")).append(SEPARADOR)
            .append(campo("Lucro por unidade (R$)")).append("\n")
        for (receita in receitas) {
            val resumo = Calculos.resumir(receita, ingredientes)
            linhas.append(campo(receita.nome)).append(SEPARADOR)
                .append(campo(Calculos.quantidade(receita.rendimento))).append(SEPARADOR)
                .append(numero(resumo.custoTotal)).append(SEPARADOR)
                .append(numero(resumo.custoUnitario)).append(SEPARADOR)
                .append(campo(Calculos.porcentagem(resumo.margem))).append(SEPARADOR)
                .append(numero(resumo.precoVenda)).append(SEPARADOR)
                .append(numero(resumo.lucroUnitario)).append("\n")
        }
        return salvar(contexto, "receitas.csv", linhas.toString())
    }

    fun dosRelatorios(contexto: Context, relatorios: List<Relatorio>): File {
        val linhas = StringBuilder()
        linhas.append(campo("Data")).append(SEPARADOR)
            .append(campo("Produto")).append(SEPARADOR)
            .append(campo("Quantidade")).append(SEPARADOR)
            .append(campo("Custo total (R$)")).append(SEPARADOR)
            .append(campo("Custo por unidade (R$)")).append(SEPARADOR)
            .append(campo("Preço de venda (R$)")).append(SEPARADOR)
            .append(campo("Faturamento (R$)")).append(SEPARADOR)
            .append(campo("Lucro (R$)")).append("\n")
        for (r in relatorios) {
            linhas.append(campo(Calculos.dataHora(r.criadoEm))).append(SEPARADOR)
                .append(campo(r.nomeReceita)).append(SEPARADOR)
                .append(campo(Calculos.quantidade(r.quantidade))).append(SEPARADOR)
                .append(numero(r.custoTotal)).append(SEPARADOR)
                .append(numero(r.custoUnitario)).append(SEPARADOR)
                .append(numero(r.precoVenda)).append(SEPARADOR)
                .append(numero(r.faturamento)).append(SEPARADOR)
                .append(numero(r.lucro)).append("\n")
        }
        return salvar(contexto, "relatorios.csv", linhas.toString())
    }
}
