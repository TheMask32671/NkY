package br.com.ariane.controle

import android.app.Application
import br.com.ariane.controle.data.Store

class ArianeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        store = Store(this)
    }

    companion object {
        lateinit var store: Store
    }
}
