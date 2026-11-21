package br.com.ariane.controle.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Guarda os dados na memoria para a tela e grava tudo no banco local.
 */
class Store(contexto: Context) {

    private val app = contexto.applicationContext
    private val banco = Banco(app)
    private val prefs = app.getSharedPreferences("ariane_config", Context.MODE_PRIVATE)

    val ingredientes = mutableStateListOf<Ingrediente>()
    val receitas = mutableStateListOf<Receita>()
    val compras = mutableStateListOf<Compra>()
    val relatorios = mutableStateListOf<Relatorio>()

    var margemPadrao by mutableStateOf(prefs.getFloat("margem_padrao", 0.5f).toDouble())

    init {
        recarregarTudo()
    }

    // ---------------------------------------------------------- recargas

    fun recarregarTudo() {
        recarregarIngredientes()
        recarregarReceitas()
        recarregarCompras()
        recarregarRelatorios()
    }

    private fun recarregarIngredientes() {
        ingredientes.clear()
        ingredientes.addAll(banco.listarIngredientes())
    }

    private fun recarregarReceitas() {
        receitas.clear()
        receitas.addAll(banco.listarReceitas())
    }

    private fun recarregarCompras() {
        compras.clear()
        compras.addAll(banco.listarCompras())
    }

    private fun recarregarRelatorios() {
        relatorios.clear()
        relatorios.addAll(banco.listarRelatorios())
    }

    // ------------------------------------------------------ configuracao

    fun definirMargemPadrao(valor: Double) {
        val limitado = valor.coerceIn(0.0, 0.95)
        margemPadrao = limitado
        prefs.edit().putFloat("margem_padrao", limitado.toFloat()).apply()
    }

    // ------------------------------------------------------ ingredientes

    fun ingredientePorId(id: Long): Ingrediente? {
        for (i in ingredientes) {
            if (i.id == id) return i
        }
        return null
    }

    fun salvarIngrediente(ingrediente: Ingrediente): Long {
        val novo = ingrediente.id <= 0L
        val id = banco.salvarIngrediente(ingrediente)
        if (novo && ingrediente.precoPago > 0.0 && ingrediente.quantidadeComprada > 0.0) {
            val base = ingrediente.converterParaBase(ingrediente.quantidadeComprada, ingrediente.unidade)
            banco.salvarCompra(
                Compra(
                    ingredienteId = id,
                    nomeIngrediente = ingrediente.nome,
                    precoPago = ingrediente.precoPago,
                    quantidade = ingrediente.quantidadeComprada,
                    unidade = ingrediente.unidade,
                    custoBase = if (base > 0.0) ingrediente.precoPago / base else 0.0,
                    data = ingrediente.dataCompra,
                    criadoEm = System.currentTimeMillis()
                )
            )
            recarregarCompras()
        }
        recarregarIngredientes()
        return id
    }

    fun excluirIngrediente(id: Long) {
        banco.excluirIngrediente(id)
        recarregarIngredientes()
        recarregarReceitas()
        recarregarCompras()
    }

    fun darBaixaNoEstoque(ingredienteId: Long, quantidadeBase: Double) {
        val ingrediente = ingredientePorId(ingredienteId) ?: return
        banco.atualizarEstoque(ingredienteId, ingrediente.estoque - quantidadeBase)
        recarregarIngredientes()
    }

    fun definirEstoque(ingredienteId: Long, quantidadeBase: Double) {
        banco.atualizarEstoque(ingredienteId, quantidadeBase)
        recarregarIngredientes()
    }

    // ---------------------------------------------------------- receitas

    fun receitaPorId(id: Long): Receita? {
        for (r in receitas) {
            if (r.id == id) return r
        }
        return null
    }

    fun salvarReceita(receita: Receita): Long {
        val id = banco.salvarReceita(receita)
        recarregarReceitas()
        return id
    }

    fun excluirReceita(id: Long) {
        banco.excluirReceita(id)
        recarregarReceitas()
    }

    // ----------------------------------------------------------- compras

    fun registrarCompra(
        ingrediente: Ingrediente,
        precoPago: Double,
        quantidade: Double,
        unidade: Unidade,
        data: String,
        atualizarPreco: Boolean,
        somarAoEstoque: Boolean
    ) {
        val base = ingrediente.converterParaBase(quantidade, unidade)
        val custoBase = if (base > 0.0) precoPago / base else 0.0
        banco.salvarCompra(
            Compra(
                ingredienteId = ingrediente.id,
                nomeIngrediente = ingrediente.nome,
                precoPago = precoPago,
                quantidade = quantidade,
                unidade = unidade,
                custoBase = custoBase,
                data = data,
                criadoEm = System.currentTimeMillis()
            )
        )
        var atualizado = ingrediente
        if (atualizarPreco && precoPago > 0.0 && quantidade > 0.0) {
            atualizado = atualizado.copy(
                precoPago = precoPago,
                quantidadeComprada = quantidade,
                unidade = unidade,
                dataCompra = data
            )
        }
        if (somarAoEstoque) {
            atualizado = atualizado.copy(estoque = atualizado.estoque + base)
        }
        banco.salvarIngrediente(atualizado)
        recarregarIngredientes()
        recarregarCompras()
    }

    fun excluirCompra(id: Long) {
        banco.excluirCompra(id)
        recarregarCompras()
    }

    fun comprasDoIngrediente(ingredienteId: Long): List<Compra> {
        val lista = ArrayList<Compra>()
        for (c in compras) {
            if (c.ingredienteId == ingredienteId) lista.add(c)
        }
        return lista
    }

    // -------------------------------------------------------- relatorios

    fun salvarRelatorio(relatorio: Relatorio): Long {
        val id = banco.salvarRelatorio(relatorio)
        recarregarRelatorios()
        return id
    }

    fun excluirRelatorio(id: Long) {
        banco.excluirRelatorio(id)
        recarregarRelatorios()
    }

    // -------------------------------------------------- numeros do painel

    fun totalGastoComIngredientes(): Double {
        var total = 0.0
        for (c in compras) total += c.precoPago
        return total
    }

    fun ingredientesComEstoqueBaixo(): List<Ingrediente> {
        val lista = ArrayList<Ingrediente>()
        for (i in ingredientes) {
            if (i.estoqueBaixo) lista.add(i)
        }
        return lista
    }

    // ------------------------------------------------------------ backup

    fun gerarBackup(): String = banco.exportarJson()

    fun restaurarBackup(texto: String): Boolean {
        val certo = banco.importarJson(texto)
        if (certo) recarregarTudo()
        return certo
    }

    fun apagarTudo() {
        banco.limparTudo()
        recarregarTudo()
    }
}
