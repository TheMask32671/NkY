package br.com.ariane.controle

import br.com.ariane.controle.calc.Calculos
import br.com.ariane.controle.data.ExtraReceita
import br.com.ariane.controle.data.Ingrediente
import br.com.ariane.controle.data.ItemReceita
import br.com.ariane.controle.data.Receita
import br.com.ariane.controle.data.Unidade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculosTest {

    @Test
    fun custoPorGramaDoChocolate() {
        val chocolate = Ingrediente(
            id = 1L,
            nome = "Chocolate",
            precoPago = 30.0,
            quantidadeComprada = 1.0,
            unidade = Unidade.QUILO
        )
        assertEquals(0.03, chocolate.custoPorBase, 0.000001)
        assertEquals(1000.0, chocolate.quantidadeBase, 0.000001)
    }

    @Test
    fun precoDeVendaUsaMargemSobreOPreco() {
        // Custo 2,00 com margem de 50% deve dar 4,00 (e nao 3,00).
        assertEquals(4.0, Calculos.precoPorMargem(2.0, 0.5), 0.000001)
        assertEquals(2.5, Calculos.precoPorMargem(2.0, 0.2), 0.000001)
        assertEquals(5.0, Calculos.precoPorMargem(2.0, 0.6), 0.000001)
    }

    @Test
    fun margemEhDiferenteDeMarkup() {
        val precoComMargem = Calculos.precoPorMargem(2.0, 0.5)
        assertEquals(1.0, Calculos.markupEquivalente(2.0, precoComMargem), 0.000001)

        val precoComMarkup = Calculos.precoPorMarkup(2.0, 0.5)
        assertEquals(3.0, precoComMarkup, 0.000001)
        assertEquals(0.333333, Calculos.margemReal(2.0, precoComMarkup), 0.0001)
    }

    @Test
    fun naoDividePorZero() {
        assertEquals(0.0, Calculos.precoPorMargem(0.0, 0.5), 0.000001)
        assertEquals(0.0, Calculos.markupEquivalente(0.0, 10.0), 0.000001)
        assertEquals(0.0, Calculos.margemReal(5.0, 0.0), 0.000001)

        val semRendimento = Receita(id = 1L, nome = "Teste", rendimento = 0.0, margem = 0.5)
        val resumo = Calculos.resumir(semRendimento, emptyList())
        assertEquals(0.0, resumo.custoUnitario, 0.000001)
        assertEquals(0.0, resumo.precoVenda, 0.000001)
    }

    @Test
    fun margemNuncaPassaDoLimite() {
        assertEquals(0.95, Calculos.limitarMargem(1.5), 0.000001)
        assertEquals(0.0, Calculos.limitarMargem(-0.3), 0.000001)
        assertTrue(Calculos.precoPorMargem(1.0, 2.0) > 0.0)
    }

    @Test
    fun pacoteComUnidadesPorPacote() {
        val forminhas = Ingrediente(
            id = 2L,
            nome = "Forminhas",
            precoPago = 10.0,
            quantidadeComprada = 1.0,
            unidade = Unidade.PACOTE,
            unidadesPorPacote = 100.0
        )
        assertEquals(100.0, forminhas.quantidadeBase, 0.000001)
        assertEquals(0.10, forminhas.custoPorBase, 0.000001)
        assertEquals(20.0, forminhas.converterParaBase(20.0, Unidade.UNIDADE), 0.000001)
    }

    @Test
    fun relatorioDaTrufaBate() {
        // 100 trufas: 80,00 de custo total, 0,80 por trufa,
        // com margem de 60% o preco sai 2,00, faturamento 200,00 e lucro 120,00.
        val chocolate = Ingrediente(
            id = 1L, nome = "Chocolate", precoPago = 30.0,
            quantidadeComprada = 1.0, unidade = Unidade.QUILO
        )
        val leite = Ingrediente(
            id = 2L, nome = "Leite condensado", precoPago = 6.0,
            quantidadeComprada = 1.0, unidade = Unidade.UNIDADE
        )
        val receita = Receita(
            id = 1L,
            nome = "Trufa de chocolate",
            rendimento = 100.0,
            margem = 0.6,
            itens = listOf(
                ItemReceita(ingredienteId = 1L, quantidade = 1.0, unidade = Unidade.QUILO),
                ItemReceita(ingredienteId = 2L, quantidade = 2.0, unidade = Unidade.UNIDADE)
            ),
            extras = listOf(
                ExtraReceita(descricao = "Embalagens", valor = 15.0),
                ExtraReceita(descricao = "Outros custos", valor = 23.0)
            )
        )
        val resumo = Calculos.resumir(receita, listOf(chocolate, leite))

        assertEquals(42.0, resumo.custoIngredientes, 0.000001)
        assertEquals(38.0, resumo.custoExtras, 0.000001)
        assertEquals(80.0, resumo.custoTotal, 0.000001)
        assertEquals(0.80, resumo.custoUnitario, 0.000001)
        assertEquals(2.0, resumo.precoVenda, 0.000001)
        assertEquals(200.0, resumo.faturamento, 0.000001)
        assertEquals(120.0, resumo.lucroTotal, 0.000001)
        assertEquals(1.20, resumo.lucroUnitario, 0.000001)
    }

    @Test
    fun simuladorMultiplicaAReceita() {
        val chocolate = Ingrediente(
            id = 1L, nome = "Chocolate", precoPago = 30.0,
            quantidadeComprada = 1.0, unidade = Unidade.QUILO, estoque = 1000.0
        )
        val receita = Receita(
            id = 1L,
            nome = "Trufa",
            rendimento = 20.0,
            margem = 0.5,
            itens = listOf(ItemReceita(ingredienteId = 1L, quantidade = 500.0, unidade = Unidade.GRAMA)),
            extras = listOf(ExtraReceita(descricao = "Embalagens", valor = 4.0))
        )

        val fator = Calculos.fatorPara(receita, 100.0)
        assertEquals(5.0, fator, 0.000001)

        val resumo = Calculos.resumir(receita, listOf(chocolate), fator)
        assertEquals(100.0, resumo.quantidadeProduzida, 0.000001)
        assertEquals(75.0, resumo.custoIngredientes, 0.000001)
        assertEquals(20.0, resumo.custoExtras, 0.000001)
        assertEquals(95.0, resumo.custoTotal, 0.000001)
        assertEquals(0.95, resumo.custoUnitario, 0.000001)
        assertEquals(1.90, resumo.precoVenda, 0.000001)

        val necessidades = Calculos.necessidades(receita, listOf(chocolate), fator)
        assertEquals(1, necessidades.size)
        assertEquals(2500.0, necessidades[0].quantidadeBase, 0.000001)
        assertTrue(!necessidades[0].estoqueSuficiente)
        assertEquals(1500.0, necessidades[0].faltando, 0.000001)
    }

    @Test
    fun leituraDeNumerosDigitados() {
        assertEquals(1234.56, Calculos.paraDouble("1.234,56"), 0.000001)
        assertEquals(1234.56, Calculos.paraDouble("1234,56"), 0.000001)
        assertEquals(12.5, Calculos.paraDouble("12.50"), 0.000001)
        assertEquals(30.0, Calculos.paraDouble("R$ 30,00"), 0.000001)
        assertEquals(0.0, Calculos.paraDouble(""), 0.000001)
        assertEquals(0.0, Calculos.paraDouble("abc"), 0.000001)
    }

    @Test
    fun ingredienteExcluidoNaoQuebraAReceita() {
        val receita = Receita(
            id = 1L,
            nome = "Teste",
            rendimento = 10.0,
            margem = 0.5,
            itens = listOf(ItemReceita(ingredienteId = 99L, quantidade = 100.0, unidade = Unidade.GRAMA))
        )
        val resumo = Calculos.resumir(receita, emptyList())
        assertEquals(0.0, resumo.custoTotal, 0.000001)
        assertTrue(resumo.avisos.isNotEmpty())
    }
}
