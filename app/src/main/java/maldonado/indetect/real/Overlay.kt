package maldonado.indetect.real
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class Overlay @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : View(ctx, attrs, defStyleAttr) {

    private val objBounds: MutableList<Bounds> = mutableListOf()
    private val boundsPaint = Paint()

    init {
        boundsPaint.style = Paint.Style.STROKE
        boundsPaint.color = Color.BLUE
        boundsPaint.strokeWidth = 4f
    }

    fun updateBounds(bounds: List<Bounds>) {
        objBounds.clear()
        objBounds.addAll(bounds)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        objBounds.forEach {
            canvas.drawRect(it.box, boundsPaint)
        }
        Log.i("Bound", objBounds.toString())
    }
}