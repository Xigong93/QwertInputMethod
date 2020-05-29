package pokercc.android.inputtextview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.PopupWindow
import androidx.annotation.CallSuper
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.TraceCompat

/**
 * Qwert 键盘
 * @author
 */
class QwertInputMethod @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private companion object {
        private const val LOG_TAG = "QwertInputMethod"
    }

    var inputMethodListener: InputMethodListener? = null

    private val lines get() = if (showNumber) 4 else 3
    private val lineMargin get() = 12.0f.dpToPx()
    private val charHeight get() = 48.0f.dpToPx()
    private val charMargin get() = 5.0f.dpToPx()
    private val charWidth get() = (width - 9 * charMargin) / 10
    private val keys = Keys(context)
    private val charDrawables = ArrayList<BlockDrawable>()
    private val backButton = FunctionButtonDrawable(
        '<',
        context,
        ResourcesCompat.getDrawable(context.resources, R.drawable.keyboard_backspace, null)!!
    )

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    private var previewPopupWindow: PopupWindow? = null
    private val onCharClickListener = object : OnCharClickListener {
        override fun onCharClickDown(char: BlockDrawable) {
            inputMethodListener?.onInputPreviewStart(char.char)
            // 显示点击预览
//            val charView = View(context)
//            charView.background = CharDrawable(context, char.char)
//            previewPopupWindow = PopupWindow(charView).also {
//                val pWidth = (charWidth * 1.3f).toInt()
//                val pHeight = (charHeight * 1.3f).toInt()
//                it.width = pWidth
//                it.height = pHeight
//                it.showAtLocation(
//                    parent as View,
//                    Gravity.TOP or Gravity.START,
//                    (x + char.bounds.left - (pWidth - char.bounds.width()) * 0.5f).toInt(),
//                    (y + char.bounds.top).toInt()
//                )
//            }

        }

        override fun onCharClickUp(char: BlockDrawable) {
            inputMethodListener?.onInput(char.char)
            previewPopupWindow?.dismiss()
            previewPopupWindow = null
        }

        override fun onCharClickCancel(char: BlockDrawable) {
            inputMethodListener?.onInputPreviewEnd()
            previewPopupWindow?.dismiss()
            previewPopupWindow = null
        }
    }

    init {
        charDrawables.addAll(keys.line1)
        charDrawables.addAll(keys.line2)
        charDrawables.addAll(keys.line3)
        charDrawables.add(backButton)

    }

    /** 是否显示数字 */
    var showNumber: Boolean = false
        set(value) {
            field = value
            requestLayout()
            invalidate()
            if (value) {
                charDrawables.addAll(keys.numbers)
            } else {
                charDrawables.removeAll(keys.numbers)
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, h: Int) {
        val width = getDefaultSize(resources.displayMetrics.widthPixels, widthMeasureSpec)
        val height = charHeight * lines + lineMargin * lines
        setMeasuredDimension(width, height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        TraceCompat.beginSection("${LOG_TAG}:layoutDrawables")
        try {
            var line = 0
            if (showNumber) {
                layoutChars(keys.numbers, line++)
            }
            layoutChars(keys.line1, line++)
            layoutChars(keys.line2, line++)
            layoutChars(keys.line3, line)
            // 布局返回键
            val charWidth = (width - 9 * charMargin) / 10
            val backspaceWidth =
                (width - (charWidth * keys.line3.size + charMargin * (keys.line3.size + 1))) * 0.5f
            val y = ((charHeight + lineMargin) * line).toInt() + (lineMargin / 2).toInt()
            backButton.setBounds(
                (width - backspaceWidth).toInt(),
                y,
                width,
                (y + charHeight).toInt()
            )
            configTouchArea(backButton)
            backButton.callback = this
            charDrawables.forEach { it.onCharClickListener = onCharClickListener }
            backButton.onCharClickListener = object : OnCharClickListener {
                override fun onCharClickDown(char: BlockDrawable) {


                }

                override fun onCharClickUp(char: BlockDrawable) {
                    inputMethodListener?.onDelete()
                }

                override fun onCharClickCancel(char: BlockDrawable) {
                }
            }
        } finally {
            TraceCompat.endSection()
        }

    }

    private fun configTouchArea(char: BlockDrawable) {
        char.touchArea.set(
            char.bounds.left - (charMargin / 2).toInt(),
            char.bounds.top - (lineMargin / 2).toInt(),
            char.bounds.right + (charMargin / 2).toInt(),
            char.bounds.bottom + (lineMargin / 2).toInt()
        )
    }

    private fun layoutChars(chars: List<CharDrawable>, lineIndex: Int) {
        val y = ((charHeight + lineMargin) * lineIndex).toInt() + (lineMargin / 2).toInt()
        val firstLeft = (width - (charWidth * chars.size + charMargin * (chars.size - 1))) * 0.5f
        for ((i, char) in chars.withIndex()) {
            val charLeft = firstLeft + (charWidth + charMargin) * i
            char.setBounds(
                charLeft.toInt(),
                y,
                (charLeft + charWidth).toInt(),
                (y + charHeight).toInt()
            )
            configTouchArea(char)
            char.callback = this
        }
    }


    override fun onDraw(canvas: Canvas) {
        TraceCompat.beginSection("${LOG_TAG}:drawAllDrawables")
        try {
            charDrawables.forEach { it.draw(canvas) }
        } finally {
            TraceCompat.endSection()
        }

    }

    private var handleDrawable: BlockDrawable? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
//        Log.d(LOG_TAG, "onTouchEvent($event)")
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handleDrawable = charDrawables.firstOrNull { it.onTouch(event) }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handleDrawable?.onTouch(event)
                handleDrawable = null
            }
        }
        return handleDrawable != null
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        super.verifyDrawable(who)
        return true
    }

}

interface InputMethodListener {
    /** 按下了还没有输入 */
    fun onInputPreviewStart(char: Char)

    /** 抬起了 */
    fun onInputPreviewEnd()

    /** 已经输入 */
    fun onInput(char: Char)

    /** 点击了删除 */
    fun onDelete()
}

private class Keys(private val context: Context) {
    val numbers: List<CharDrawable> =
        arrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0').map { CharDrawable(context, it) }
    val line1: List<CharDrawable> =
        arrayOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p').map { CharDrawable(context, it) }
    val line2: List<CharDrawable> =
        arrayOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l').map { CharDrawable(context, it) }
    val line3: List<CharDrawable> =
        arrayOf('z', 'x', 'c', 'v', 'b', 'n', 'm').map { CharDrawable(context, it) }
}

private interface OnCharClickListener {
    fun onCharClickDown(char: BlockDrawable)
    fun onCharClickUp(char: BlockDrawable)
    fun onCharClickCancel(char: BlockDrawable)
}

private abstract class BlockDrawable(val char: Char, private val context: Context) :
    Drawable() {

    private val backgroundDrawable: ShapeDrawable

    val touchArea = Rect()

    init {
        val radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            4.0f,
            context.resources.displayMetrics
        )
        val radiusArr = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        val shape = RoundRectShape(radiusArr, null, null)
        backgroundDrawable = ShapeDrawable(shape)
        backgroundDrawable.paint.color = Color.parseColor("#F3F4F5")
    }

    var onCharClickListener: OnCharClickListener? = null
    fun onTouch(event: MotionEvent): Boolean {

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val contains = touchArea.contains(event.x.toInt(), event.y.toInt())
                if (contains) {
                    onCharClickListener?.onCharClickDown(this)
                    backgroundDrawable.paint?.color = Color.parseColor("#76D9B8")
                    invalidateSelf()
                }

                return contains
            }
            MotionEvent.ACTION_MOVE -> {

            }
            MotionEvent.ACTION_UP -> {
                onCharClickListener?.onCharClickUp(this)
                backgroundDrawable.paint?.color = Color.parseColor("#F3F4F5")
                invalidateSelf()
            }
            MotionEvent.ACTION_CANCEL -> {
                backgroundDrawable.paint?.color = Color.parseColor("#F3F4F5")
                invalidateSelf()
                onCharClickListener?.onCharClickCancel(this)
            }
        }
        return true
    }

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        backgroundDrawable.bounds = bounds
    }

    @CallSuper
    override fun draw(canvas: Canvas) {
        backgroundDrawable.draw(canvas)
    }

    @CallSuper
    override fun setAlpha(alpha: Int) {
        backgroundDrawable.alpha = alpha
    }
}

private class CharDrawable(private val context: Context, char: Char) :
    BlockDrawable(char, context) {
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = Color.parseColor("#333333")
        p.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            context.resources.displayMetrics
        )
        p.typeface = Typeface.DEFAULT_BOLD
    }

    private val fontMetrics = Paint.FontMetrics()
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val c = char.toString()
        val textWidth = paint.measureText(c)
        paint.getFontMetrics(fontMetrics)
        val textHeight = fontMetrics.ascent + fontMetrics.descent
        canvas.drawText(
            c,
            0,
            1,
            bounds.left + (bounds.width() - textWidth) * 0.5f,
            bounds.top + (bounds.height() - textHeight) * 0.5f,
            paint
        )

    }

    override fun setAlpha(alpha: Int) {
        super.setAlpha(alpha)
        paint.alpha = alpha
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun toString(): String {
        return "CharDrawable(char:$char)"
    }
}

private class FunctionButtonDrawable(char: Char, context: Context, private val drawable: Drawable) :
    BlockDrawable(char, context) {


    override fun setAlpha(alpha: Int) {
        super.setAlpha(alpha)
        drawable.alpha = alpha

    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val left = (bounds.left + (bounds.width() - drawable.intrinsicWidth) * 0.5f).toInt()
        val top = (bounds.top + (bounds.height() - drawable.intrinsicHeight) * 0.5f).toInt()
        drawable.setBounds(
            left, top, left + drawable.intrinsicWidth, top + drawable.intrinsicHeight
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawable.draw(canvas)
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }
}