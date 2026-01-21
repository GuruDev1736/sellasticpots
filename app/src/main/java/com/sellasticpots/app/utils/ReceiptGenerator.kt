package com.sellasticpots.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.sellasticpots.app.models.Order
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ReceiptGenerator {

    fun generateReceipt(context: Context, order: Order): File? {
        try {
            val formattedOrderId = formatOrderId(order.orderId)
            val fileName = "Receipt_${formattedOrderId}.pdf"

            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val file = File(documentsDir, fileName)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawReceipt(canvas, order, formattedOrderId)

            pdfDocument.finishPage(page)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun drawReceipt(canvas: Canvas, order: Order, formattedOrderId: String) {
        val paint = Paint()
        var yPosition = 50f

        paint.color = Color.rgb(76, 175, 80)
        paint.textSize = 32f
        paint.isFakeBoldText = true
        canvas.drawText("SELLASTIC POTS", 50f, yPosition, paint)
        yPosition += 40f

        paint.color = Color.GRAY
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Traditional Pottery, Modern Convenience", 50f, yPosition, paint)
        yPosition += 40f

        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText("ORDER RECEIPT", 50f, yPosition, paint)
        yPosition += 40f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Order ID: $formattedOrderId", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Order Date: ${order.getFormattedDate()}", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Status: ${order.orderStatus}", 50f, yPosition, paint)
        yPosition += 35f

        paint.strokeWidth = 1f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        paint.isFakeBoldText = true
        canvas.drawText("CUSTOMER DETAILS", 50f, yPosition, paint)
        yPosition += 25f

        paint.isFakeBoldText = false
        canvas.drawText("Name: ${order.fullName}", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Phone: ${order.phone}", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Email: ${order.email}", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Address: ${order.address}", 50f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("${order.city}, ${order.state} - ${order.pincode}", 50f, yPosition, paint)
        yPosition += 35f

        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        paint.isFakeBoldText = true
        canvas.drawText("ORDER ITEMS", 50f, yPosition, paint)
        yPosition += 30f

        canvas.drawText("Item", 50f, yPosition, paint)
        canvas.drawText("Qty", 300f, yPosition, paint)
        canvas.drawText("Price", 380f, yPosition, paint)
        canvas.drawText("Total", 480f, yPosition, paint)
        yPosition += 5f

        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 20f

        paint.isFakeBoldText = false
        for (item in order.items) {
            val itemName = if (item.productName.length > 30) {
                item.productName.substring(0, 27) + "..."
            } else {
                item.productName
            }
            canvas.drawText(itemName, 50f, yPosition, paint)
            canvas.drawText("x${item.quantity}", 300f, yPosition, paint)
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.productPrice)}", 380f, yPosition, paint)
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.totalPrice)}", 480f, yPosition, paint)
            yPosition += 25f
        }

        yPosition += 10f
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Subtotal:", 380f, yPosition, paint)
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}", 480f, yPosition, paint)
        yPosition += 25f

        canvas.drawText("Delivery Fee:", 380f, yPosition, paint)
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", order.deliveryFee)}", 480f, yPosition, paint)
        yPosition += 25f

        paint.strokeWidth = 2f
        canvas.drawLine(380f, yPosition, 545f, yPosition, paint)
        yPosition += 25f

        paint.isFakeBoldText = true
        paint.textSize = 16f
        canvas.drawText("Total Amount:", 380f, yPosition, paint)
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", order.totalAmount + order.deliveryFee)}", 480f, yPosition, paint)
        yPosition += 40f

        paint.strokeWidth = 1f
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        canvas.drawText("Estimated Delivery: ${order.getFormattedDeliveryDate()}", 50f, yPosition, paint)
        yPosition += 25f

        paint.color = Color.GRAY
        paint.textSize = 12f
        canvas.drawText("Thank you for shopping with Sellastic Pots!", 50f, yPosition, paint)
        yPosition += 18f

        canvas.drawText("For any queries, contact us at: support@potssellastic.in", 50f, yPosition, paint)
        yPosition += 18f

        canvas.drawText("Website: https://potssellastic.in", 50f, yPosition, paint)
    }

    private fun formatOrderId(orderId: String): String {
        val numericPart = orderId.hashCode().toString().takeLast(5).padStart(5, '0')
        return "ORD-$numericPart"
    }
}
