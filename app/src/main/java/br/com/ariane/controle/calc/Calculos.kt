package br.com.ariane.controle.calc

import br.com.ariane.controle.data.Grupo
import br.com.ariane.controle.data.Ingrediente
import br.com.ariane.controle.data.LinhaCusto
import br.com.ariane.controle.data.Receita
import br.com.ariane.controle.data.Relatorio
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

/** Resultado completo do calculo de uma receita. */
data class Resumo(
    val nomeReceita: String = "",
    val linhas: List<LinhaCusto> = emptyList(),
    val avisos: List<String> = emptyList(),
    val custoIngredientes: Double = 0.0,
    val custoExtras: Double = 0.0,
    val custoTotal: Double = 0.0,
    val quantidadeProduzida: Double = 0.0,
    val custoUnitario: Double = 0.0,
    val margem: Double = 0.0,
    val precoVenda: Double = 0.0,
    val lucroUnitario: Double = 0.0,
    val faturamento: Double = 0.0,
    val lucroTotal: Double = 0.0
)

/** Quantidade de um ingrediente necessaria para uma producao. */
data class NecessidadeIngrediente(
    val ingrediente: Ingrediente,
    val quantidadeNaUnidadeDoItem: Double,
    val siglaItem: String,
    val quantidadeBase: Double,
    val custo: Double,
    val estoqueSuficiente: Boolean,
    val faltando: Double
)

object Calculos {

    private val LOCAL: Locale = Locale.forLanguageTag("pt-BR")
    private const val MARGEM_MAXIMA = 0.95

    // ----------------------------------------------------------------- textos

    fun numero(valor: Double, decimais: Int = 2): String {
        val v = if (valor.isNaN() || valor.isInfinite()) 0.0 else valor
        return String.format(LOCAL, "%,." + decimais + "f", v)
    }

    fun moeda(valor: Double): String = "R$ " + numero(valor, 2)

    /** Para custos muito pequenos, como o custo por grama. */
    fun moedaDetalhada(valor: Double): String {
        val v = if (valor.isNaN() || valor.isInfinite()) 0.0 else valor
        return if (abs(v) > 0.0 && abs(v) < 1.0) "R$ " + numero(v, 4) else "R$ " + numero(v, 2)
    }

    fun quantidade(valor: Double): String {
        val v = if (valor.isNaN() || valor.isInfinite()) 0.0 else valor
        return if (abs(v - round(v)) < 0.0005) numero(v, 0) else numero(v, 2)
    }

    /** Mostra 1500 g como "1,5 kg" e 2000 ml como "2 L". */
    fun quantidadeBaseAmigavel(valor: Double, grupo: Grupo): String {
        if (grupo == Grupo.MASSA && abs(valor) >= 1000.0) {
            return quantidade(valor / 1000.0) + " kg"
        }
        if (grupo == Grupo.VOLUME && abs(valor) >= 1000.0) {
            return quantidade(valor / 1000.0) + " L"
        }
        return quantidade(valor) + " " + grupo.siglaBase
    }

    fun porcentagem(fracao: Double, decimais: Int = 0): String =
        numero(fracao * 100.0, decimais) + "%"

    /** Aceita 1.234,56 / 1234,56 / 1234.56 / R$ 12,00. */
    fun paraDouble(texto: String): Double {
        var limpo = texto.trim()
            .replace("R$", "")
            .replace(" ", "")
            .replace(" ", "")
        if (limpo.isEmpty()) return 0.0
        val temVirgula = limpo.contains(",")
        val temPonto = limpo.contains(".")
        limpo = when {
            temVirgula && temPonto -> limpo.replace(".", "").replace(",", ".")
            temVirgula -> limpo.replace(",", ".")
            else -> limpo
        }
        val valor = limpo.toDoubleOrNull() ?: 0.0
        return if (valor.isNaN() || valor.isInfinite()) 0.0 else valor
    }

    fun hoje(): String = SimpleDateFormat("dd/MM/yyyy", LOCAL).format(Date())

    fun dataCurta(milissegundos: Long): String =
        SimpleDateFormat("dd/MM/yyyy", LOCAL).format(Date(milissegundos))

    fun dataHora(milissegundos: Long): String =
        SimpleDateFormat("dd/MM/yyyy 'as' HH:mm", LOCAL).format(Date(milissegundos))

    fun nomeArquivo(texto: String): String {
        val limpo = StringBuilder()
        for (c in texto.lowercase()) {
            when {
                c in 'a'..'z' || c in '0'..'9' -> limpo.append(c)
                c == 'á' || c == 'à' || c == 'ã' || c == 'â' -> limpo.append('a')
                c == 'é' || c == 'ê' -> limpo.append('e')
                c == 'í' -> limpo.append('i')
                c == 'ó' || c == 'ô' || c == 'õ' -> limpo.append('o')
                c == 'ú' -> limpo.append('u')
                c == 'ç' -> limpo.append('c')
                else -> limpo.append('_')
            }
        }
        val resultado = limpo.toString().trim('_')
        return if (resultado.isEmpty()) "relatorio" else resultado
    }

    // --------------------------------------------------------------- margem

    /**
     * MARGEM DE LUCRO (sobre o preco de venda):
     * preco = custo / (1 - margem).
     * Diferente do markup, que e um acrescimo sobre o custo.
     */
    fun precoPorMargem(custoUnitario: Double, margem: Double): Double {
        if (custoUnitario <= 0.0) return 0.0
        val m = margem.coerceIn(0.0, MARGEM_MAXIMA)
        return custoUnitario / (1.0 - m)
    }

    /** Preco pelo markup (acrescimo sobre o custo): preco = custo x (1 + markup). */
    fun precoPorMarkup(custoUnitario: Double, markup: Double): Double {
        if (custoUnitario <= 0.0) return 0.0
        return custoUnitario * (1.0 + markup.coerceAtLeast(0.0))
    }

    /** Quanto por cento foi acrescentado sobre o custo para chegar neste preco. */
    fun markupEquivalente(custoUnitario: Double, precoVenda: Double): Double {
        if (custoUnitario <= 0.0) return 0.0
        return (precoVenda - custoUnitario) / custoUnitario
    }

    /** Margem real que um preco escolhido na mao produz. */
    fun margemReal(custoUnitario: Double, precoVenda: Double): Double {
        if (precoVenda <= 0.0) return 0.0
        return (precoVenda - custoUnitario) / precoVenda
    }

    fun limitarMargem(margem: Double): Double = margem.coerceIn(0.0, MARGEM_MAXIMA)

    // --------------------------------------------------------------- receita

    /**
     * Calcula todos os numeros de uma receita.
     * fator = 1 calcula uma receita inteira; fator = 5 calcula cinco receitas.
     */
    fun resumir(
        receita: Receita,
        ingredientes: List<Ingrediente>,
        fator: Double = 1.0,
        margemForcada: Double? = null
    ): Resumo {
        val mapa = HashMap<Long, Ingrediente>()
        for (i in ingredientes) mapa[i.id] = i

        val linhas = ArrayList<LinhaCusto>()
        val avisos = ArrayList<String>()
        var custoIngredientes = 0.0

        for (item in receita.itens) {
            val ingrediente = mapa[item.ingredienteId]
            if (ingrediente == null) {
                avisos.add("Um ingrediente da receita foi excluido do cadastro.")
                continue
            }
            val quantidadeUsada = item.quantidade * fator
            val base = ingrediente.converterParaBase(quantidadeUsada, item.unidade)
            val custo = base * ingrediente.custoPorBase
            custoIngredientes += custo
            if (ingrediente.custoPorBase <= 0.0) {
                avisos.add("Sem preco cadastrado para " + ingrediente.nome + ".")
            }
            linhas.add(
                LinhaCusto(
                    nome = ingrediente.nome,
                    detalhe = quantidade(quantidadeUsada) + " " + item.unidade.sigla,
                    custo = custo,
                    extra = false
                )
            )
        }

        var custoExtras = 0.0
        for (extra in receita.extras) {
            val valor = extra.valor * fator
            custoExtras += valor
            linhas.add(
                LinhaCusto(
                    nome = if (extra.descricao.isBlank()) "Custo extra" else extra.descricao,
                    detalhe = "",
                    custo = valor,
                    extra = true
                )
            )
        }

        val custoTotal = custoIngredientes + custoExtras
        val produzido = (receita.rendimento * fator).coerceAtLeast(0.0)
        val custoUnitario = if (produzido > 0.0) custoTotal / produzido else 0.0
        val margem = limitarMargem(margemForcada ?: receita.margem)
        val precoVenda = precoPorMargem(custoUnitario, margem)
        val lucroUnitario = precoVenda - custoUnitario

        return Resumo(
            nomeReceita = receita.nome,
            linhas = linhas,
            avisos = avisos.distinct(),
            custoIngredientes = custoIngredientes,
            custoExtras = custoExtras,
            custoTotal = custoTotal,
            quantidadeProduzida = produzido,
            custoUnitario = custoUnitario,
            margem = margem,
            precoVenda = precoVenda,
            lucroUnitario = lucroUnitario,
            faturamento = precoVenda * produzido,
            lucroTotal = lucroUnitario * produzido
        )
    }

    /** Transforma um resumo calculado em um relatorio para guardar. */
    fun paraRelatorio(receitaId: Long, resumo: Resumo): Relatorio = Relatorio(
        receitaId = receitaId,
        nomeReceita = resumo.nomeReceita,
        quantidade = resumo.quantidadeProduzida,
        custoIngredientes = resumo.custoIngredientes,
        custoExtras = resumo.custoExtras,
        custoTotal = resumo.custoTotal,
        custoUnitario = resumo.custoUnitario,
        margem = resumo.margem,
        precoVenda = resumo.precoVenda,
        faturamento = resumo.faturamento,
        lucro = resumo.lucroTotal,
        linhas = resumo.linhas,
        criadoEm = System.currentTimeMillis()
    )

    /** Fator necessario para produzir uma quantidade desejada de unidades. */
    fun fatorPara(receita: Receita, unidadesDesejadas: Double): Double {
        if (receita.rendimento <= 0.0) return 0.0
        return unidadesDesejadas / receita.rendimento
    }

    /** Lista o que precisa ser comprado/usado para produzir, conferindo o estoque. */
    fun necessidades(
        receita: Receita,
        ingredientes: List<Ingrediente>,
        fator: Double
    ): List<NecessidadeIngrediente> {
        val mapa = HashMap<Long, Ingrediente>()
        for (i in ingredientes) mapa[i.id] = i

        val juntos = LinkedHashMap<Long, NecessidadeIngrediente>()
        for (item in receita.itens) {
            val ingrediente = mapa[item.ingredienteId] ?: continue
            val quantidadeUsada = item.quantidade * fator
            val base = ingrediente.converterParaBase(quantidadeUsada, item.unidade)
            val custo = base * ingrediente.custoPorBase
            val anterior = juntos[ingrediente.id]
            val novaQuantidade = (anterior?.quantidadeNaUnidadeDoItem ?: 0.0) + quantidadeUsada
            val novaBase = (anterior?.quantidadeBase ?: 0.0) + base
            val novoCusto = (anterior?.custo ?: 0.0) + custo
            val falta = novaBase - ingrediente.estoque
            juntos[ingrediente.id] = NecessidadeIngrediente(
                ingrediente = ingrediente,
                quantidadeNaUnidadeDoItem = novaQuantidade,
                siglaItem = item.unidade.sigla,
                quantidadeBase = novaBase,
                custo = novoCusto,
                estoqueSuficiente = ingrediente.estoque >= novaBase,
                faltando = if (falta > 0.0) falta else 0.0
            )
        }
        return ArrayList(juntos.values)
    }
}
