package pokercc.android.inputtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout


/**
 * 功能特点：
 * - 任意位置可设置固定字符
 * - 非NPC的位置有下划线
 * - 间距可调
 * - 字体大小可调
 * - 字符可染色
 * - 单行，自动大小
 */
@SuppressLint("AppCompatCustomView")
class EditTextGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), InputMethodListener {
    companion object {
        private const val LOG_TAG = "EditTextGroup"
    }

    /** 显示模式 */
    sealed class ShowMode {
        /** 输入模式 */
        object Input : ShowMode()

        /** 标准答案 */
        object StandardAnswer : ShowMode()

        /** 答案对比 */
        object DiffResult : ShowMode()
    }

    private var showMode: ShowMode = ShowMode.Input
    private val textViews = ArrayList<DashEditText>()

    private var templateText: String? = null


    fun setShowMode(showMode: ShowMode) {
        if (this.showMode != showMode) {
            this.showMode = showMode
        }

    }

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    init {
        orientation = LinearLayout.HORIZONTAL
    }

    /** 设置模板字符串 */
    fun setTemplateText(templateText: String) {
        if (this.templateText != templateText) {
            this.templateText = templateText
        }
        removeAllViews()
        textViews.clear()
        for (c in templateText) {
            val textView = createTextView()
            textViews.add(textView)
            addView(
                textView,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            )
        }

        // 设置显示模式
        for ((i, textView) in textViews.withIndex()) {
            textView.text = when (showMode) {
                ShowMode.Input -> null
                ShowMode.StandardAnswer -> templateText.getOrNull(i)?.toString()
                ShowMode.DiffResult -> templateText.getOrNull(i)?.toString()
            }
        }

    }

    private fun createTextView() = DashEditText(context).apply {
        setTextColor(Color.parseColor("#ff48cda1"))
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26f)
        paint.typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        isFocusableInTouchMode = true
    }

    override fun onInputPreviewStart(char: Char) {

    }

    override fun onInputPreviewEnd() {

    }

    private var text = StringBuilder()

    override fun onInput(char: Char) {
        val t = templateText ?: return
        if (text.length >= t.length) return
        text.append(char)
        onTextChange()
    }

    private fun onTextChange() {
        val inputText = text.toString()
        for ((i, textView) in textViews.withIndex()) {
            textView.text = inputText.getOrNull(i)?.toString()
            textView.lightBottomLine =
                i == inputText.length.coerceAtMost(textViews.size - 1)
        }
    }

    override fun onDelete() {
        if (text.isNotEmpty()) {
            text.delete(text.length - 1, text.length)
            onTextChange()
        }
    }

}

private class DashEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    init {
        val paddingHorizontal = 5.0f.dpToPx().toInt()
        val paddingVertical = 10.0f.dpToPx().toInt()
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
    }

    var lightBottomLine = false
        set(value) {
            field = value
            invalidate()
        }
    private val bottomLinePaint = Paint().also {
        it.color = 0xffE7E7E7.toInt()
        it.strokeWidth = 2f.dpToPx()
        it.style = Paint.Style.FILL
        it.strokeCap = Paint.Cap.ROUND
    }


    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bottomLineY = height - 2f
        bottomLinePaint.color = if (lightBottomLine) {
            0xff48cda1
        } else {
            0xffE7E7E7
        }.toInt()
        canvas.drawLine(
            paddingStart.toFloat(), bottomLineY,
            (width - paddingEnd).toFloat(), bottomLineY, bottomLinePaint
        )
    }
}

