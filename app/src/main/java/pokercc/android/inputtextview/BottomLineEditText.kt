package pokercc.android.inputtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.addTextChangedListener

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
class BottomLineEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {
    companion object {
        private const val LOG_TAG = "BottomLineEditText"
    }

    /** 单个字符的宽度 */
    private var charWidth = 0
    private var templateText: String? = null

    /** 是否显示下划线 */
    private var bottomLineVisible = true

    private val bottomLinePaint = Paint().also {
        it.color = 0xffE7E7E7.toInt()
        it.strokeWidth = 1.0f.dpToPx()
        it.style = Paint.Style.FILL
        it.strokeCap = Paint.Cap.ROUND
    }

    init {
        isCursorVisible = false
        setTextColor(Color.TRANSPARENT)
        setBackgroundColor(Color.TRANSPARENT)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                Log.d(LOG_TAG, "afterTextChanged($s)")
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d(LOG_TAG, "beforeTextChanged($s,$start,$count,$after)")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d(LOG_TAG, "onTextChanged($s,$start,$before,$count")
            }

        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)

    /** 设置模板字符串 */
    fun setTemplateText(templateText: String) {
        if (this.templateText != templateText) {
            this.templateText = templateText
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val templateText = this.templateText
        if (templateText.isNullOrEmpty()) return
        val charWidth = width / templateText.length
        val charMargin = 5.0f.dpToPx()
        val bottomLineWidth = charWidth - charMargin * 2
        val bottomLineY = height - paddingBottom - 0.5f.dpToPx()
        // 划线
        if (bottomLineVisible) {
            for (i in templateText.indices) {
                val lineLeft = i * charWidth + charMargin
                canvas.drawLine(
                    lineLeft,
                    bottomLineY,
                    lineLeft + bottomLineWidth,
                    bottomLineY,
                    bottomLinePaint
                )
            }
        }
        // 绘制文字

    }

}