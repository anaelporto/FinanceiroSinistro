package com.financeiro.sinistro

import android.app.Application
import com.financeiro.sinistro.data.FinanceiroRepository

class FinanceiroApplication : Application() {
    val repository: FinanceiroRepository by lazy { FinanceiroRepository() }
}
