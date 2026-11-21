package br.com.ariane.controle.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Banco de dados local do aplicativo (SQLite no proprio aparelho).
 * Nada sai do celular: funciona totalmente offline.
 */
class Banco(contexto: Context) : SQLiteOpenHelper(contexto.applicationContext, NOME, null, VERSAO) {

    companion object {
        const val NOME = "ariane_controle.db"
        const val VERSAO = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ingredientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL DEFAULT '', " +
                "marca TEXT NOT NULL DEFAULT '', " +
                "preco_pago REAL NOT NULL DEFAULT 0, " +
                "quantidade REAL NOT NULL DEFAULT 0, " +
                "unidade TEXT NOT NULL DEFAULT 'g', " +
                "unidades_por_pacote REAL NOT NULL DEFAULT 0, " +
                "data_compra TEXT NOT NULL DEFAULT '', " +
                "estoque REAL NOT NULL DEFAULT 0, " +
                "estoque_minimo REAL NOT NULL DEFAULT 0, " +
                "atualizado_em INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE receitas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL DEFAULT '', " +
                "rendimento REAL NOT NULL DEFAULT 1, " +
                "margem REAL NOT NULL DEFAULT 0.5, " +
                "observacoes TEXT NOT NULL DEFAULT '', " +
                "criado_em INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE itens_receita (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "receita_id INTEGER NOT NULL DEFAULT 0, " +
                "ingrediente_id INTEGER NOT NULL DEFAULT 0, " +
                "quantidade REAL NOT NULL DEFAULT 0, " +
                "unidade TEXT NOT NULL DEFAULT 'g')"
        )
        db.execSQL(
            "CREATE TABLE extras_receita (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "receita_id INTEGER NOT NULL DEFAULT 0, " +
                "descricao TEXT NOT NULL DEFAULT '', " +
                "valor REAL NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE compras (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ingrediente_id INTEGER NOT NULL DEFAULT 0, " +
                "nome_ingrediente TEXT NOT NULL DEFAULT '', " +
                "preco_pago REAL NOT NULL DEFAULT 0, " +
                "quantidade REAL NOT NULL DEFAULT 0, " +
                "unidade TEXT NOT NULL DEFAULT 'g', " +
                "custo_base REAL NOT NULL DEFAULT 0, " +
                "data TEXT NOT NULL DEFAULT '', " +
                "criado_em INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE relatorios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "receita_id INTEGER NOT NULL DEFAULT 0, " +
                "nome_receita TEXT NOT NULL DEFAULT '', " +
                "quantidade REAL NOT NULL DEFAULT 0, " +
                "custo_ingredientes REAL NOT NULL DEFAULT 0, " +
                "custo_extras REAL NOT NULL DEFAULT 0, " +
                "custo_total REAL NOT NULL DEFAULT 0, " +
                "custo_unitario REAL NOT NULL DEFAULT 0, " +
                "margem REAL NOT NULL DEFAULT 0, " +
                "preco_venda REAL NOT NULL DEFAULT 0, " +
                "faturamento REAL NOT NULL DEFAULT 0, " +
                "lucro REAL NOT NULL DEFAULT 0, " +
                "detalhes TEXT NOT NULL DEFAULT '', " +
                "criado_em INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL("CREATE INDEX idx_itens_receita ON itens_receita (receita_id)")
        db.execSQL("CREATE INDEX idx_extras_receita ON extras_receita (receita_id)")
        db.execSQL("CREATE INDEX idx_compras_ingrediente ON compras (ingrediente_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, versaoAntiga: Int, versaoNova: Int) {
        // Versao 1: nada a migrar ainda.
    }

    // ------------------------------------------------------------- leitura

    private fun Cursor.texto(coluna: String): String {
        val indice = getColumnIndex(coluna)
        if (indice < 0 || isNull(indice)) return ""
        return getString(indice) ?: ""
    }

    private fun Cursor.decimal(coluna: String): Double {
        val indice = getColumnIndex(coluna)
        if (indice < 0 || isNull(indice)) return 0.0
        return getDouble(indice)
    }

    private fun Cursor.inteiro(coluna: String): Long {
        val indice = getColumnIndex(coluna)
        if (indice < 0 || isNull(indice)) return 0L
        return getLong(indice)
    }

    // -------------------------------------------------------- ingredientes

    fun listarIngredientes(): List<Ingrediente> {
        val lista = ArrayList<Ingrediente>()
        readableDatabase.rawQuery(
            "SELECT * FROM ingredientes ORDER BY nome COLLATE NOCASE ASC", null
        ).use { c ->
            while (c.moveToNext()) lista.add(lerIngrediente(c))
        }
        return lista
    }

    private fun lerIngrediente(c: Cursor): Ingrediente = Ingrediente(
        id = c.inteiro("id"),
        nome = c.texto("nome"),
        marca = c.texto("marca"),
        precoPago = c.decimal("preco_pago"),
        quantidadeComprada = c.decimal("quantidade"),
        unidade = Unidade.porSigla(c.texto("unidade")),
        unidadesPorPacote = c.decimal("unidades_por_pacote"),
        dataCompra = c.texto("data_compra"),
        estoque = c.decimal("estoque"),
        estoqueMinimo = c.decimal("estoque_minimo"),
        atualizadoEm = c.inteiro("atualizado_em")
    )

    fun salvarIngrediente(ingrediente: Ingrediente): Long {
        val valores = ContentValues()
        valores.put("nome", ingrediente.nome)
        valores.put("marca", ingrediente.marca)
        valores.put("preco_pago", ingrediente.precoPago)
        valores.put("quantidade", ingrediente.quantidadeComprada)
        valores.put("unidade", ingrediente.unidade.sigla)
        valores.put("unidades_por_pacote", ingrediente.unidadesPorPacote)
        valores.put("data_compra", ingrediente.dataCompra)
        valores.put("estoque", ingrediente.estoque)
        valores.put("estoque_minimo", ingrediente.estoqueMinimo)
        valores.put("atualizado_em", System.currentTimeMillis())
        val db = writableDatabase
        return if (ingrediente.id > 0L) {
            db.update("ingredientes", valores, "id = ?", arrayOf(ingrediente.id.toString()))
            ingrediente.id
        } else {
            db.insert("ingredientes", null, valores)
        }
    }

    fun excluirIngrediente(id: Long) {
        val db = writableDatabase
        db.delete("itens_receita", "ingrediente_id = ?", arrayOf(id.toString()))
        db.delete("compras", "ingrediente_id = ?", arrayOf(id.toString()))
        db.delete("ingredientes", "id = ?", arrayOf(id.toString()))
    }

    fun atualizarEstoque(id: Long, novoEstoque: Double) {
        val valores = ContentValues()
        valores.put("estoque", if (novoEstoque < 0.0) 0.0 else novoEstoque)
        writableDatabase.update("ingredientes", valores, "id = ?", arrayOf(id.toString()))
    }

    // ------------------------------------------------------------ receitas

    fun listarReceitas(): List<Receita> {
        val receitas = ArrayList<Receita>()
        readableDatabase.rawQuery(
            "SELECT * FROM receitas ORDER BY nome COLLATE NOCASE ASC", null
        ).use { c ->
            while (c.moveToNext()) {
                receitas.add(
                    Receita(
                        id = c.inteiro("id"),
                        nome = c.texto("nome"),
                        rendimento = c.decimal("rendimento"),
                        margem = c.decimal("margem"),
                        observacoes = c.texto("observacoes"),
                        criadoEm = c.inteiro("criado_em")
                    )
                )
            }
        }
        val completas = ArrayList<Receita>()
        for (receita in receitas) {
            completas.add(
                receita.copy(
                    itens = listarItens(receita.id),
                    extras = listarExtras(receita.id)
                )
            )
        }
        return completas
    }

    private fun listarItens(receitaId: Long): List<ItemReceita> {
        val lista = ArrayList<ItemReceita>()
        readableDatabase.rawQuery(
            "SELECT * FROM itens_receita WHERE receita_id = ? ORDER BY id ASC",
            arrayOf(receitaId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    ItemReceita(
                        id = c.inteiro("id"),
                        receitaId = c.inteiro("receita_id"),
                        ingredienteId = c.inteiro("ingrediente_id"),
                        quantidade = c.decimal("quantidade"),
                        unidade = Unidade.porSigla(c.texto("unidade"))
                    )
                )
            }
        }
        return lista
    }

    private fun listarExtras(receitaId: Long): List<ExtraReceita> {
        val lista = ArrayList<ExtraReceita>()
        readableDatabase.rawQuery(
            "SELECT * FROM extras_receita WHERE receita_id = ? ORDER BY id ASC",
            arrayOf(receitaId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    ExtraReceita(
                        id = c.inteiro("id"),
                        receitaId = c.inteiro("receita_id"),
                        descricao = c.texto("descricao"),
                        valor = c.decimal("valor")
                    )
                )
            }
        }
        return lista
    }

    fun salvarReceita(receita: Receita): Long {
        val db = writableDatabase
        val valores = ContentValues()
        valores.put("nome", receita.nome)
        valores.put("rendimento", receita.rendimento)
        valores.put("margem", receita.margem)
        valores.put("observacoes", receita.observacoes)
        valores.put("criado_em", if (receita.criadoEm > 0L) receita.criadoEm else System.currentTimeMillis())

        db.beginTransaction()
        try {
            val id: Long = if (receita.id > 0L) {
                db.update("receitas", valores, "id = ?", arrayOf(receita.id.toString()))
                receita.id
            } else {
                db.insert("receitas", null, valores)
            }
            db.delete("itens_receita", "receita_id = ?", arrayOf(id.toString()))
            db.delete("extras_receita", "receita_id = ?", arrayOf(id.toString()))
            for (item in receita.itens) {
                val v = ContentValues()
                v.put("receita_id", id)
                v.put("ingrediente_id", item.ingredienteId)
                v.put("quantidade", item.quantidade)
                v.put("unidade", item.unidade.sigla)
                db.insert("itens_receita", null, v)
            }
            for (extra in receita.extras) {
                val v = ContentValues()
                v.put("receita_id", id)
                v.put("descricao", extra.descricao)
                v.put("valor", extra.valor)
                db.insert("extras_receita", null, v)
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun excluirReceita(id: Long) {
        val db = writableDatabase
        db.delete("itens_receita", "receita_id = ?", arrayOf(id.toString()))
        db.delete("extras_receita", "receita_id = ?", arrayOf(id.toString()))
        db.delete("receitas", "id = ?", arrayOf(id.toString()))
    }

    // ------------------------------------------------------------- compras

    fun listarCompras(): List<Compra> {
        val lista = ArrayList<Compra>()
        readableDatabase.rawQuery(
            "SELECT * FROM compras ORDER BY criado_em DESC", null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Compra(
                        id = c.inteiro("id"),
                        ingredienteId = c.inteiro("ingrediente_id"),
                        nomeIngrediente = c.texto("nome_ingrediente"),
                        precoPago = c.decimal("preco_pago"),
                        quantidade = c.decimal("quantidade"),
                        unidade = Unidade.porSigla(c.texto("unidade")),
                        custoBase = c.decimal("custo_base"),
                        data = c.texto("data"),
                        criadoEm = c.inteiro("criado_em")
                    )
                )
            }
        }
        return lista
    }

    fun salvarCompra(compra: Compra): Long {
        val valores = ContentValues()
        valores.put("ingrediente_id", compra.ingredienteId)
        valores.put("nome_ingrediente", compra.nomeIngrediente)
        valores.put("preco_pago", compra.precoPago)
        valores.put("quantidade", compra.quantidade)
        valores.put("unidade", compra.unidade.sigla)
        valores.put("custo_base", compra.custoBase)
        valores.put("data", compra.data)
        valores.put("criado_em", if (compra.criadoEm > 0L) compra.criadoEm else System.currentTimeMillis())
        return writableDatabase.insert("compras", null, valores)
    }

    fun excluirCompra(id: Long) {
        writableDatabase.delete("compras", "id = ?", arrayOf(id.toString()))
    }

    // ---------------------------------------------------------- relatorios

    fun listarRelatorios(): List<Relatorio> {
        val lista = ArrayList<Relatorio>()
        readableDatabase.rawQuery(
            "SELECT * FROM relatorios ORDER BY criado_em DESC", null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Relatorio(
                        id = c.inteiro("id"),
                        receitaId = c.inteiro("receita_id"),
                        nomeReceita = c.texto("nome_receita"),
                        quantidade = c.decimal("quantidade"),
                        custoIngredientes = c.decimal("custo_ingredientes"),
                        custoExtras = c.decimal("custo_extras"),
                        custoTotal = c.decimal("custo_total"),
                        custoUnitario = c.decimal("custo_unitario"),
                        margem = c.decimal("margem"),
                        precoVenda = c.decimal("preco_venda"),
                        faturamento = c.decimal("faturamento"),
                        lucro = c.decimal("lucro"),
                        linhas = linhasDeJson(c.texto("detalhes")),
                        criadoEm = c.inteiro("criado_em")
                    )
                )
            }
        }
        return lista
    }

    fun salvarRelatorio(relatorio: Relatorio): Long {
        val valores = ContentValues()
        valores.put("receita_id", relatorio.receitaId)
        valores.put("nome_receita", relatorio.nomeReceita)
        valores.put("quantidade", relatorio.quantidade)
        valores.put("custo_ingredientes", relatorio.custoIngredientes)
        valores.put("custo_extras", relatorio.custoExtras)
        valores.put("custo_total", relatorio.custoTotal)
        valores.put("custo_unitario", relatorio.custoUnitario)
        valores.put("margem", relatorio.margem)
        valores.put("preco_venda", relatorio.precoVenda)
        valores.put("faturamento", relatorio.faturamento)
        valores.put("lucro", relatorio.lucro)
        valores.put("detalhes", linhasParaJson(relatorio.linhas))
        valores.put("criado_em", if (relatorio.criadoEm > 0L) relatorio.criadoEm else System.currentTimeMillis())
        return writableDatabase.insert("relatorios", null, valores)
    }

    fun excluirRelatorio(id: Long) {
        writableDatabase.delete("relatorios", "id = ?", arrayOf(id.toString()))
    }

    private fun linhasParaJson(linhas: List<LinhaCusto>): String {
        val array = JSONArray()
        for (linha in linhas) {
            val objeto = JSONObject()
            objeto.put("nome", linha.nome)
            objeto.put("detalhe", linha.detalhe)
            objeto.put("custo", linha.custo)
            objeto.put("extra", linha.extra)
            array.put(objeto)
        }
        return array.toString()
    }

    private fun linhasDeJson(texto: String): List<LinhaCusto> {
        if (texto.isBlank()) return emptyList()
        return try {
            val array = JSONArray(texto)
            val lista = ArrayList<LinhaCusto>()
            for (i in 0 until array.length()) {
                val objeto = array.optJSONObject(i) ?: continue
                lista.add(
                    LinhaCusto(
                        nome = objeto.optString("nome", ""),
                        detalhe = objeto.optString("detalhe", ""),
                        custo = objeto.optDouble("custo", 0.0),
                        extra = objeto.optBoolean("extra", false)
                    )
                )
            }
            lista
        } catch (erro: Exception) {
            emptyList()
        }
    }

    // --------------------------------------------------- backup e limpeza

    fun limparTudo() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("itens_receita", null, null)
            db.delete("extras_receita", null, null)
            db.delete("receitas", null, null)
            db.delete("compras", null, null)
            db.delete("relatorios", null, null)
            db.delete("ingredientes", null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun exportarJson(): String {
        val raiz = JSONObject()
        raiz.put("aplicativo", "Ariane: Controle")
        raiz.put("versao_backup", 1)
        raiz.put("gerado_em", System.currentTimeMillis())

        val ingredientes = JSONArray()
        for (i in listarIngredientes()) {
            val o = JSONObject()
            o.put("id", i.id)
            o.put("nome", i.nome)
            o.put("marca", i.marca)
            o.put("preco_pago", i.precoPago)
            o.put("quantidade", i.quantidadeComprada)
            o.put("unidade", i.unidade.sigla)
            o.put("unidades_por_pacote", i.unidadesPorPacote)
            o.put("data_compra", i.dataCompra)
            o.put("estoque", i.estoque)
            o.put("estoque_minimo", i.estoqueMinimo)
            ingredientes.put(o)
        }
        raiz.put("ingredientes", ingredientes)

        val receitas = JSONArray()
        for (r in listarReceitas()) {
            val o = JSONObject()
            o.put("id", r.id)
            o.put("nome", r.nome)
            o.put("rendimento", r.rendimento)
            o.put("margem", r.margem)
            o.put("observacoes", r.observacoes)
            o.put("criado_em", r.criadoEm)
            val itens = JSONArray()
            for (item in r.itens) {
                val oi = JSONObject()
                oi.put("ingrediente_id", item.ingredienteId)
                oi.put("quantidade", item.quantidade)
                oi.put("unidade", item.unidade.sigla)
                itens.put(oi)
            }
            o.put("itens", itens)
            val extras = JSONArray()
            for (extra in r.extras) {
                val oe = JSONObject()
                oe.put("descricao", extra.descricao)
                oe.put("valor", extra.valor)
                extras.put(oe)
            }
            o.put("extras", extras)
            receitas.put(o)
        }
        raiz.put("receitas", receitas)

        val compras = JSONArray()
        for (c in listarCompras()) {
            val o = JSONObject()
            o.put("ingrediente_id", c.ingredienteId)
            o.put("nome_ingrediente", c.nomeIngrediente)
            o.put("preco_pago", c.precoPago)
            o.put("quantidade", c.quantidade)
            o.put("unidade", c.unidade.sigla)
            o.put("custo_base", c.custoBase)
            o.put("data", c.data)
            o.put("criado_em", c.criadoEm)
            compras.put(o)
        }
        raiz.put("compras", compras)

        val relatorios = JSONArray()
        for (r in listarRelatorios()) {
            val o = JSONObject()
            o.put("receita_id", r.receitaId)
            o.put("nome_receita", r.nomeReceita)
            o.put("quantidade", r.quantidade)
            o.put("custo_ingredientes", r.custoIngredientes)
            o.put("custo_extras", r.custoExtras)
            o.put("custo_total", r.custoTotal)
            o.put("custo_unitario", r.custoUnitario)
            o.put("margem", r.margem)
            o.put("preco_venda", r.precoVenda)
            o.put("faturamento", r.faturamento)
            o.put("lucro", r.lucro)
            o.put("detalhes", linhasParaJson(r.linhas))
            o.put("criado_em", r.criadoEm)
            relatorios.put(o)
        }
        raiz.put("relatorios", relatorios)

        return raiz.toString(2)
    }

    /** Restaura um backup. Substitui todos os dados atuais. */
    fun importarJson(texto: String): Boolean {
        val raiz = try {
            JSONObject(texto)
        } catch (erro: Exception) {
            return false
        }
        if (!raiz.has("ingredientes") && !raiz.has("receitas")) return false

        val db = writableDatabase
        db.beginTransaction()
        try {
            limparTudoSemTransacao(db)

            val mapaIds = HashMap<Long, Long>()
            val ingredientes = raiz.optJSONArray("ingredientes") ?: JSONArray()
            for (i in 0 until ingredientes.length()) {
                val o = ingredientes.optJSONObject(i) ?: continue
                val v = ContentValues()
                v.put("nome", o.optString("nome", ""))
                v.put("marca", o.optString("marca", ""))
                v.put("preco_pago", o.optDouble("preco_pago", 0.0))
                v.put("quantidade", o.optDouble("quantidade", 0.0))
                v.put("unidade", o.optString("unidade", "g"))
                v.put("unidades_por_pacote", o.optDouble("unidades_por_pacote", 0.0))
                v.put("data_compra", o.optString("data_compra", ""))
                v.put("estoque", o.optDouble("estoque", 0.0))
                v.put("estoque_minimo", o.optDouble("estoque_minimo", 0.0))
                v.put("atualizado_em", System.currentTimeMillis())
                val novoId = db.insert("ingredientes", null, v)
                mapaIds[o.optLong("id", -1L)] = novoId
            }

            val receitas = raiz.optJSONArray("receitas") ?: JSONArray()
            for (i in 0 until receitas.length()) {
                val o = receitas.optJSONObject(i) ?: continue
                val v = ContentValues()
                v.put("nome", o.optString("nome", ""))
                v.put("rendimento", o.optDouble("rendimento", 1.0))
                v.put("margem", o.optDouble("margem", 0.5))
                v.put("observacoes", o.optString("observacoes", ""))
                v.put("criado_em", o.optLong("criado_em", System.currentTimeMillis()))
                val receitaId = db.insert("receitas", null, v)

                val itens = o.optJSONArray("itens") ?: JSONArray()
                for (j in 0 until itens.length()) {
                    val oi = itens.optJSONObject(j) ?: continue
                    val vi = ContentValues()
                    val antigo = oi.optLong("ingrediente_id", -1L)
                    vi.put("receita_id", receitaId)
                    vi.put("ingrediente_id", mapaIds[antigo] ?: antigo)
                    vi.put("quantidade", oi.optDouble("quantidade", 0.0))
                    vi.put("unidade", oi.optString("unidade", "g"))
                    db.insert("itens_receita", null, vi)
                }

                val extras = o.optJSONArray("extras") ?: JSONArray()
                for (j in 0 until extras.length()) {
                    val oe = extras.optJSONObject(j) ?: continue
                    val ve = ContentValues()
                    ve.put("receita_id", receitaId)
                    ve.put("descricao", oe.optString("descricao", ""))
                    ve.put("valor", oe.optDouble("valor", 0.0))
                    db.insert("extras_receita", null, ve)
                }
            }

            val compras = raiz.optJSONArray("compras") ?: JSONArray()
            for (i in 0 until compras.length()) {
                val o = compras.optJSONObject(i) ?: continue
                val v = ContentValues()
                val antigo = o.optLong("ingrediente_id", -1L)
                v.put("ingrediente_id", mapaIds[antigo] ?: antigo)
                v.put("nome_ingrediente", o.optString("nome_ingrediente", ""))
                v.put("preco_pago", o.optDouble("preco_pago", 0.0))
                v.put("quantidade", o.optDouble("quantidade", 0.0))
                v.put("unidade", o.optString("unidade", "g"))
                v.put("custo_base", o.optDouble("custo_base", 0.0))
                v.put("data", o.optString("data", ""))
                v.put("criado_em", o.optLong("criado_em", System.currentTimeMillis()))
                db.insert("compras", null, v)
            }

            val relatorios = raiz.optJSONArray("relatorios") ?: JSONArray()
            for (i in 0 until relatorios.length()) {
                val o = relatorios.optJSONObject(i) ?: continue
                val v = ContentValues()
                val antigo = o.optLong("receita_id", -1L)
                v.put("receita_id", antigo)
                v.put("nome_receita", o.optString("nome_receita", ""))
                v.put("quantidade", o.optDouble("quantidade", 0.0))
                v.put("custo_ingredientes", o.optDouble("custo_ingredientes", 0.0))
                v.put("custo_extras", o.optDouble("custo_extras", 0.0))
                v.put("custo_total", o.optDouble("custo_total", 0.0))
                v.put("custo_unitario", o.optDouble("custo_unitario", 0.0))
                v.put("margem", o.optDouble("margem", 0.0))
                v.put("preco_venda", o.optDouble("preco_venda", 0.0))
                v.put("faturamento", o.optDouble("faturamento", 0.0))
                v.put("lucro", o.optDouble("lucro", 0.0))
                v.put("detalhes", o.optString("detalhes", ""))
                v.put("criado_em", o.optLong("criado_em", System.currentTimeMillis()))
                db.insert("relatorios", null, v)
            }

            db.setTransactionSuccessful()
            return true
        } catch (erro: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    private fun limparTudoSemTransacao(db: SQLiteDatabase) {
        db.delete("itens_receita", null, null)
        db.delete("extras_receita", null, null)
        db.delete("receitas", null, null)
        db.delete("compras", null, null)
        db.delete("relatorios", null, null)
        db.delete("ingredientes", null, null)
    }
}
