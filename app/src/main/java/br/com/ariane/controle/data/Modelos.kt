package br.com.ariane.controle.data

/** Grupos de medida compativeis entre si. */
enum class Grupo(val rotulo: String, val siglaBase: String) {
    MASSA("Peso", "g"),
    VOLUME("Volume", "ml"),
    CONTAGEM("Contagem", "un")
}

/**
 * Unidades de medida aceitas no cadastro.
 * O "fator" converte a unidade para a unidade base do grupo
 * (gramas para peso, mililitros para volume e unidades para contagem).
 */
enum class Unidade(
    val sigla: String,
    val rotulo: String,
    val grupo: Grupo,
    val fator: Double
) {
    GRAMA("g", "Gramas", Grupo.MASSA, 1.0),
    QUILO("kg", "Quilos", Grupo.MASSA, 1000.0),
    MILILITRO("ml", "Mililitros", Grupo.VOLUME, 1.0),
    LITRO("l", "Litros", Grupo.VOLUME, 1000.0),
    UNIDADE("un", "Unidades", Grupo.CONTAGEM, 1.0),
    PACOTE("pct", "Pacotes", Grupo.CONTAGEM, 1.0);

    companion object {
        fun porSigla(sigla: String): Unidade {
            val busca = sigla.trim().lowercase()
            for (u in values()) {
                if (u.sigla == busca) return u
            }
            return GRAMA
        }

        fun doGrupo(grupo: Grupo): List<Unidade> {
            val lista = ArrayList<Unidade>()
            for (u in values()) {
                if (u.grupo == grupo) lista.add(u)
            }
            return lista
        }
    }
}

/** Um ingrediente comprado pela usuaria. */
data class Ingrediente(
    val id: Long = 0L,
    val nome: String = "",
    val marca: String = "",
    val precoPago: Double = 0.0,
    val quantidadeComprada: Double = 0.0,
    val unidade: Unidade = Unidade.GRAMA,
    val unidadesPorPacote: Double = 0.0,
    val dataCompra: String = "",
    val estoque: Double = 0.0,
    val estoqueMinimo: Double = 0.0,
    val atualizadoEm: Long = 0L
) {
    val grupo: Grupo
        get() = unidade.grupo

    val siglaBase: String
        get() = unidade.grupo.siglaBase

    /** Converte uma quantidade qualquer para a unidade base deste ingrediente. */
    fun converterParaBase(quantidade: Double, de: Unidade): Double {
        if (de == Unidade.PACOTE && unidadesPorPacote > 0.0) {
            return quantidade * unidadesPorPacote
        }
        return quantidade * de.fator
    }

    /** Quantidade comprada ja convertida para a unidade base. */
    val quantidadeBase: Double
        get() = converterParaBase(quantidadeComprada, unidade)

    /** Quanto custa 1 grama, 1 ml ou 1 unidade deste ingrediente. */
    val custoPorBase: Double
        get() {
            val base = quantidadeBase
            return if (base > 0.0) precoPago / base else 0.0
        }

    /** Estoque abaixo do minimo definido pela usuaria. */
    val estoqueBaixo: Boolean
        get() = estoqueMinimo > 0.0 && estoque <= estoqueMinimo
}

/** Ingrediente usado dentro de uma receita. */
data class ItemReceita(
    val id: Long = 0L,
    val receitaId: Long = 0L,
    val ingredienteId: Long = 0L,
    val quantidade: Double = 0.0,
    val unidade: Unidade = Unidade.GRAMA
)

/** Custo extra da producao: embalagem, etiqueta, gas, energia e outros. */
data class ExtraReceita(
    val id: Long = 0L,
    val receitaId: Long = 0L,
    val descricao: String = "",
    val valor: Double = 0.0
)

/** Uma receita/produto da usuaria. */
data class Receita(
    val id: Long = 0L,
    val nome: String = "",
    val rendimento: Double = 1.0,
    val margem: Double = 0.5,
    val observacoes: String = "",
    val criadoEm: Long = 0L,
    val itens: List<ItemReceita> = emptyList(),
    val extras: List<ExtraReceita> = emptyList()
)

/** Registro de uma compra de ingrediente (historico e variacao de precos). */
data class Compra(
    val id: Long = 0L,
    val ingredienteId: Long = 0L,
    val nomeIngrediente: String = "",
    val precoPago: Double = 0.0,
    val quantidade: Double = 0.0,
    val unidade: Unidade = Unidade.GRAMA,
    val custoBase: Double = 0.0,
    val data: String = "",
    val criadoEm: Long = 0L
)

/** Uma linha de custo dentro de um relatorio. */
data class LinhaCusto(
    val nome: String = "",
    val detalhe: String = "",
    val custo: Double = 0.0,
    val extra: Boolean = false
)

/** Relatorio salvo: fotografia dos valores praticados naquele dia. */
data class Relatorio(
    val id: Long = 0L,
    val receitaId: Long = 0L,
    val nomeReceita: String = "",
    val quantidade: Double = 0.0,
    val custoIngredientes: Double = 0.0,
    val custoExtras: Double = 0.0,
    val custoTotal: Double = 0.0,
    val custoUnitario: Double = 0.0,
    val margem: Double = 0.0,
    val precoVenda: Double = 0.0,
    val faturamento: Double = 0.0,
    val lucro: Double = 0.0,
    val linhas: List<LinhaCusto> = emptyList(),
    val criadoEm: Long = 0L
)
