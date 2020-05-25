package pokercc.android.inputtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible


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
) : FrameLayout(context, attrs, defStyleAttr) {
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

    private val linearLayout = LinearLayout(context).also {
        it.orientation = LinearLayout.HORIZONTAL
    }
    private var showMode: ShowMode = ShowMode.Input
    private val textViews = ArrayList<DashEditText>()

    private val editText = EditText(context).also {
        it.maxLines = 1
        it.isFocusable = true
        it.isFocusableInTouchMode = true
        it.setOnLongClickListener { true }// 屏蔽长按
        it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS //限制输入类型
        it.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val inputText = s?.toString() ?: ""
                for ((i, textView) in textViews.withIndex()) {
                    textView.text = inputText.getOrNull(i)?.toString()
                    textView.lightBottomLine =
                        i == inputText.length.coerceAtMost(textViews.size - 1)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        it.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                it.setSelection(it.text.length)
            }
        }

        // 设置只能输入英文
        it.keyListener = object : DigitsKeyListener() {
            private val regular = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

            override fun getInputType(): Int {
                return  InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }

            override fun getAcceptedChars(): CharArray {
                return regular.toCharArray()
            }

        }
//        it.requestFocus()// 请求焦点，弹出键盘
    }
    private var templateText: String? = null

    init {
        editText.alpha = 0f
        addView(editText)
        addView(linearLayout)
    }

    fun setShowMode(showMode: ShowMode) {
        if (this.showMode != showMode) {
            this.showMode = showMode
        }

    }

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    /** 设置模板字符串 */
    fun setTemplateText(templateText: String) {
        if (this.templateText != templateText) {
            this.templateText = templateText
        }
        linearLayout.removeAllViews()
        textViews.clear()
        for (c in templateText) {
            val textView = createTextView()
            textViews.add(textView)
            linearLayout.addView(
                textView,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            )
        }

        // 清空状态
        editText.text = null
        editText.filters = arrayOf(InputFilter.LengthFilter(templateText.length))// 限制输入长度
        // 设置显示模式
        for ((i, textView) in textViews.withIndex()) {
            textView.text = when (showMode) {
                ShowMode.Input -> null
                ShowMode.StandardAnswer -> templateText.getOrNull(i)?.toString()
                ShowMode.DiffResult -> templateText.getOrNull(i)?.toString()
            }
        }
        editText.isVisible = when (showMode) {
            ShowMode.Input -> true
            ShowMode.StandardAnswer -> false
            ShowMode.DiffResult -> false
        }
    }

    private fun createTextView() = DashEditText(context).apply {
        setTextColor(Color.parseColor("#ff48cda1"))
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26f)
        paint.typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        isFocusableInTouchMode = true
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

