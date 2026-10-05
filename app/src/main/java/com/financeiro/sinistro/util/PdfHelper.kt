package com.financeiro.sinistro.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.financeiro.sinistro.data.ResumoFinanceiro
import com.financeiro.sinistro.data.formatarDataHora
import com.financeiro.sinistro.data.formatarMoeda
import java.io.File
import java.io.FileOutputStream

class PdfHelper(private val context: Context) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 44f
    private val contentRight = pageWidth - margin

    fun gerarFechamentoDiario(resumo: ResumoFinanceiro): Boolean {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 20f
            isFakeBoldText = true
            color = Color.rgb(30, 42, 52)
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.rgb(28, 84, 93)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = Color.rgb(35, 35, 35)
        }
        val smallPaint = Paint(textPaint).apply { textSize = 9.5f }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(210, 218, 222)
            strokeWidth = 1f
        }

        var pageNumber = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = desenharCabecalho(canvas, titlePaint, textPaint, resumo, pageNumber)

        fun novaPagina() {
            pdfDocument.finishPage(page)
            pageNumber += 1
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = desenharCabecalho(canvas, titlePaint, textPaint, resumo, pageNumber)
        }

        fun garantirEspaco(altura: Float) {
            if (y + altura > pageHeight - 48f) novaPagina()
        }

        y = secao(canvas, "Contagem de caixa", y, sectionPaint)
        linha(canvas, "Tipo", resumo.caixa.tipo.rotulo, y, textPaint, linePaint).also { y = it }
        resumo.caixa.linhas.forEach { linhaCaixa ->
            garantirEspaco(24f)
            val valor = "${linhaCaixa.quantidade} x ${linhaCaixa.denominacao.rotulo}"
            y = linha(canvas, valor, linhaCaixa.totalCentavos.formatarMoeda(), y, textPaint, linePaint)
        }
        y = linhaTotal(canvas, "Total em caixa", resumo.caixa.totalCentavos.formatarMoeda(), y, textPaint)

        garantirEspaco(76f)
        y = secao(canvas, "Lancamentos iFood", y + 12f, sectionPaint)
        if (resumo.ifood.isEmpty()) {
            y = linha(canvas, "Nenhum pedido informado", "-", y, textPaint, linePaint)
        } else {
            resumo.ifood.forEach {
                garantirEspaco(36f)
                val descricao = "${it.codigo} - ${it.descricao}"
                val detalhe = "Bruto ${it.valorOriginalCentavos.formatarMoeda()} | Desc. ${it.descontoCentavos.formatarMoeda()}"
                canvas.drawText(descricao, margin, y, textPaint)
                canvas.drawText(it.valorFinalCentavos.formatarMoeda(), contentRight - textPaint.measureText(it.valorFinalCentavos.formatarMoeda()), y, textPaint)
                canvas.drawText(detalhe, margin, y + 14f, smallPaint)
                canvas.drawLine(margin, y + 22f, contentRight, y + 22f, linePaint)
                y += 34f
            }
        }
        y = linhaTotal(canvas, "Total online iFood", resumo.totalIfoodCentavos.formatarMoeda(), y, textPaint)

        garantirEspaco(120f)
        y = secao(canvas, "Contabilidade final", y + 12f, sectionPaint)
        y = linha(canvas, "Debito", resumo.contabilidade.debitoCentavos.formatarMoeda(), y, textPaint, linePaint)
        y = linha(canvas, "Credito", resumo.contabilidade.creditoCentavos.formatarMoeda(), y, textPaint, linePaint)
        y = linha(canvas, "Pix", resumo.contabilidade.pixCentavos.formatarMoeda(), y, textPaint, linePaint)
        y = linha(canvas, "Online", resumo.totalIfoodCentavos.formatarMoeda(), y, textPaint, linePaint)
        y = linha(canvas, "Fechamento do caixa", resumo.caixa.totalCentavos.formatarMoeda(), y, textPaint, linePaint)
        linhaTotal(canvas, "Total do dia", resumo.totalMovimentadoCentavos.formatarMoeda(), y, textPaint)

        pdfDocument.finishPage(page)
        return salvarPdf(pdfDocument, "Fechamento_Financeiro_${System.currentTimeMillis()}.pdf")
    }

    private fun desenharCabecalho(
        canvas: Canvas,
        titlePaint: Paint,
        textPaint: Paint,
        resumo: ResumoFinanceiro,
        pageNumber: Int
    ): Float {
        canvas.drawText("Fechamento Financeiro", pageWidth / 2f, 48f, titlePaint)
        canvas.drawText("Gerado em ${resumo.criadoEmMillis.formatarDataHora()}", margin, 76f, textPaint)
        val pagina = "Pagina $pageNumber"
        canvas.drawText(pagina, contentRight - textPaint.measureText(pagina), 76f, textPaint)
        return 110f
    }

    private fun secao(canvas: Canvas, titulo: String, y: Float, paint: Paint): Float {
        canvas.drawText(titulo, margin, y, paint)
        return y + 24f
    }

    private fun linha(
        canvas: Canvas,
        label: String,
        value: String,
        y: Float,
        textPaint: Paint,
        linePaint: Paint
    ): Float {
        canvas.drawText(label, margin, y, textPaint)
        canvas.drawText(value, contentRight - textPaint.measureText(value), y, textPaint)
        canvas.drawLine(margin, y + 8f, contentRight, y + 8f, linePaint)
        return y + 24f
    }

    private fun linhaTotal(canvas: Canvas, label: String, value: String, y: Float, textPaint: Paint): Float {
        val boldPaint = Paint(textPaint).apply { isFakeBoldText = true; textSize = 13f }
        canvas.drawText(label, margin, y + 6f, boldPaint)
        canvas.drawText(value, contentRight - boldPaint.measureText(value), y + 6f, boldPaint)
        return y + 32f
    }

    private fun salvarPdf(pdfDocument: PdfDocument, fileName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let { context.contentResolver.openOutputStream(it)?.use(pdfDocument::writeTo) }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                FileOutputStream(File(dir, fileName)).use(pdfDocument::writeTo)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            pdfDocument.close()
        }
    }
}
