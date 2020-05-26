package pokercc.android.inputtextview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.core.content.res.ResourcesCompat

/**
 * Qwert 键盘
 * @author
 */
class QwertInputMethod @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 是否显示数字 */
    var showNumber: Boolean = false
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    var inputMethodListener: InputMethodListener? = null

    private val lines get() = if (showNumber) 4 else 3
    private val lineMargin get() = 9.0f.dpToPx()
    private val charHeight get() = 50.0f.dpToPx()
    private val charMargin get() = 4.0f.dpToPx()
    private val keys = Keys(context)
    private val backButton = BackSpaceDrawable('<', context)

    private fun Float.dpToPx(): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)


    private val onCharClickListener = object : OnCharClickListener {
        override fun onCharClickDown(char: Char) {
            inputMethodListener?.onInputPreviewStart(char)


        }

        override fun onCharClickUp(char: Char) {
            inputMethodListener?.onInput(char)
        }

        override fun onCharClickCancel(char: Char) {
            inputMethodListener?.onInputPreviewEnd()

        }
    }

    init {
        keys.numbers.forEach { it.onCharClickListener = onCharClickListener }
        keys.line1.forEach { it.onCharClickListener = onCharClickListener }
        keys.line2.forEach { it.onCharClickListener = onCharClickListener }
        keys.line3.forEach { it.onCharClickListener = onCharClickListener }
        backButton.onCharClickListener = object : OnCharClickListener {
            override fun onCharClickDown(char: Char) {


            }

            override fun onCharClickUp(char: Char) {
                inputMethodListener?.onDelete()
            }

            override fun onCharClickCancel(char: Char) {
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, h: Int) {
        val width = getDefaultSize(resources.displayMetrics.widthPixels, widthMeasureSpec)
        val height = charHeight * lines + lineMargin * (lines - 1)
        setMeasuredDimension(width, height.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (showNumber) {
            layoutChars(keys.numbers, 0)
        }
        layoutChars(keys.line1, 1)
        layoutChars(keys.line2, 2)
        layoutChars(keys.line3, 3)
        // 布局返回键
        val charWidth = (width - 9 * charMargin) / 10
        val backspaceWidth =
            (width - (charWidth * keys.line3.size + charMargin * (keys.line3.size + 1))) * 0.5f
        val y = ((charHeight + lineMargin) * 3).toInt()
        backButton.setBounds(
            (width - backspaceWidth).toInt(),
            y,
            width,
            (y + charHeight).toInt()
        )
        backButton.callback = this

    }

    private fun layoutChars(chars: List<CharDrawable>, lineIndex: Int) {
        val y = ((charHeight + lineMargin) * lineIndex).toInt()
        val charWidth = (width - 9 * charMargin) / 10
        val firstLeft = (width - (charWidth * chars.size + charMargin * (chars.size - 1))) * 0.5f
        for ((i, char) in chars.withIndex()) {
            val charLeft = firstLeft + (charWidth + charMargin) * i
            char.setBounds(
                charLeft.toInt(),
                y,
                (charLeft + charWidth).toInt(),
                (y + charHeight).toInt()
            )
            char.callback = this
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (showNumber) {
            drawChars(canvas, keys.numbers)
        }
        drawChars(canvas, keys.line1)
        drawChars(canvas, keys.line2)
        drawChars(canvas, keys.line3)
        backButton.draw(canvas)
    }

    private fun drawChars(canvas: Canvas, chars: List<CharDrawable>) {
        for (charDrawable in chars) {
            charDrawable.draw(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (showNumber && onCharsTouch(event, keys.numbers)) return true
        if (onCharsTouch(event, keys.line1)) return true
        if (onCharsTouch(event, keys.line2)) return true
        if (onCharsTouch(event, keys.line3)) return true
        if (backButton.onTouch(event)) return true
        return super.onTouchEvent(event)

    }

    private fun onCharsTouch(event: MotionEvent, chars: List<CharDrawable>): Boolean {
        for (charDrawable in chars) {
            if (charDrawable.onTouch(event)) return true
        }
        return false
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        super.verifyDrawable(who)
        return true
    }

}

interface InputMethodListener {
    /** 按下了还没有输入 */
    fun onInputPreviewStart(char: Char)

    /**
     * 抬起了
     */
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
    fun onCharClickDown(char: Char)
    fun onCharClickUp(char: Char)
    fun onCharClickCancel(char: Char)
}

private abstract class BlockDrawable(private val char: Char, private val context: Context) :
    Drawable() {

    private var backgroundDrawable: ShapeDrawable? = null
    var onCharClickListener: OnCharClickListener? = null
    fun onTouch(event: MotionEvent): Boolean {
        val contains = bounds.contains(event.x.toInt(), event.y.toInt())
        if (contains) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onCharClickListener?.onCharClickDown(char)
                    alpha = (0.6f * 255).toInt()
                    invalidateSelf()
                }
                MotionEvent.ACTION_MOVE -> {

                }
                MotionEvent.ACTION_UP -> {
                    onCharClickListener?.onCharClickUp(char)
                    alpha = 255
                    invalidateSelf()
                }
                MotionEvent.ACTION_CANCEL -> {
                    alpha = 255
                    invalidateSelf()
                    onCharClickListener?.onCharClickCancel(char)
                }
            }
        }
        return contains
    }

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5.0f,
            context.resources.displayMetrics
        )
        val radiusArr = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        val shape = RoundRectShape(radiusArr, null, null)
        backgroundDrawable = ShapeDrawable(shape)
        backgroundDrawable?.paint?.color = Color.parseColor("#60000000")
        backgroundDrawable?.bounds = bounds
    }

    @CallSuper
    override fun draw(canvas: Canvas) {
        backgroundDrawable?.draw(canvas)
    }

    @CallSuper
    override fun setAlpha(alpha: Int) {
        backgroundDrawable?.alpha = alpha
    }
}

private class CharDrawable(private val context: Context, private val char: Char) :
    BlockDrawable(char, context) {
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = Color.BLACK
        p.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
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

private class BackSpaceDrawable(char: Char, context: Context) : BlockDrawable(char, context) {
    private val backSpace =
        ResourcesCompat.getDrawable(context.resources, R.drawable.backspace, null)!!

    override fun setAlpha(alpha: Int) {
        super.setAlpha(alpha)
        backSpace.alpha = alpha

    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val left = (bounds.left + (bounds.width() - backSpace.intrinsicWidth) * 0.5f).toInt()
        val top = (bounds.top + (bounds.height() - backSpace.intrinsicHeight) * 0.5f).toInt()
        backSpace.setBounds(
            left, top, left + backSpace.intrinsicWidth, top + backSpace.intrinsicHeight
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        backSpace.draw(canvas)
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backSpace.colorFilter = colorFilter
    }
}