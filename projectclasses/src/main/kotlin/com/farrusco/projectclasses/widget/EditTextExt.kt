/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.projectclasses.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.icu.text.DecimalFormatSymbols
import android.os.Handler
import android.os.Looper
import android.text.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import androidx.core.view.isVisible
import com.farrusco.projectclasses.R
import com.farrusco.projectclasses.common.ConstantsFixed
import com.farrusco.projectclasses.utils.TagModify
import com.farrusco.projectclasses.widget.validators.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

@Suppress("SpellCheckingInspection")
class EditTextExt : androidx.appcompat.widget.AppCompatEditText   {
    private val mPaint = Paint()
    private var emptyAllowed = true
    private var autoTest = true
    private var drawLines = true
    private var initdrawLines = true
    var skipEditTagOnce = false
    var contextMenuEnabled = false
    private var errorString: String? = null
    private var emptyErrorString: String? = null
    private var displayFormat: String? = null
    var background: Int = R.drawable.edittext_gradient
    private var format: CharSequence? = null
    //private var skipEditTagAlwaysPrivate = false

    var skipEditTagAlways: Boolean = false
        set(value) {
            field = value
            if (value) {
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            }
        }

    var setMaxLength: Int = 0
        set(value) {
            if (field == 0 && value > 0){
                filters = arrayOf<InputFilter>(InputFilter.LengthFilter(value))
            }
        }

    private var currencySymbolPrefix: String = DecimalFormatSymbols.getInstance().currency.symbol
    //private lateinit var textPhotor: CurrencyInputPhotor
    //private var locale: Locale = Locale.getDefault()
    //private var maxDP: Int = 0

    var validators = ArrayList<Validator>()
    var colorView: ConstantsFixed.ColorBasic = ConstantsFixed.ColorBasic.Default
        set(value) {
            this.setTextColor( value.color )
            field = value
            if (isEnabled){
                setBackgroundColor(Color.WHITE)
                if (value.color != Color.BLUE) {
                    this.setTextColor(ConstantsFixed.ColorBasic.Dark.color)
                    field = ConstantsFixed.ColorBasic.Dark
                }
            } else {
                setBackgroundColor(Color.BLACK)
                if (value.color == Color.BLACK) {
                    this.setTextColor(ConstantsFixed.ColorBasic.Edit.color)
                    field = ConstantsFixed.ColorBasic.Edit
                }
            }

            if (background != 0) this.setBackgroundResource(background)
        }

    // auto size text
    private var autoScale = false
    private val defaultTextScale = 0.70f
    private val defaultAnimationDuration = 300
    private val defaultLinesLimit = 2.0f
    private var originalViewWidth = 0
    private var originalTextSize = 0f
    private var resizeInProgress = false
    private var linesLimit = defaultLinesLimit
    private var animationDuration = defaultAnimationDuration
    private var textScale = defaultTextScale
    private var textMeasuringPaint: Paint? = null

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        initControl(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        initControl(context, attrs)
    }

    constructor(context: Context) : super(context) {
        if (tag == null) {
            TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsForeBack.name, "f")
        }
        initControl(context, null)
    }

    init {
        colorView = ConstantsFixed.ColorBasic.Edit

    }

    @SuppressLint("DiscouragedApi")
    private fun initControl(context: Context, attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextExt)

        autoTest = typedArray.getBoolean(R.styleable.EditTextExt_auto, true)
        drawLines = typedArray.getBoolean(R.styleable.EditTextExt_drawlines, true)
        errorString = typedArray.getString(R.styleable.EditTextExt_errorString)
        emptyErrorString = typedArray.getString(R.styleable.EditTextExt_emptyErrorString)
        displayFormat = typedArray.getString(R.styleable.EditTextExt_displayFormat)

        //background = typedArray.getString(R.styleable.EditTextExt_backgroundResource,0)
        contextMenuEnabled = typedArray.getBoolean(
            R.styleable.EditTextExt_editContextMenu,
            false
        )
        var rs = typedArray.getString(R.styleable.EditTextExt_backgroundResource)
        if (rs != null){
            rs = rs.replace("res/","").replace(".xml","")
            background = resources.getIdentifier(rs,null,context.packageName)
            this.setBackgroundResource(background)
        }

        autoScale = typedArray.getBoolean(R.styleable.EditTextExt_autoScale,false)
        linesLimit = typedArray.getFloat(R.styleable.EditTextExt_autoLinesLimit,defaultLinesLimit)
        textScale = typedArray.getFloat(R.styleable.EditTextExt_autoTextScale,defaultTextScale)
        animationDuration = typedArray.getInt(R.styleable.EditTextExt_autoAnimationDuration,defaultAnimationDuration)

        format = this.hint

        // setOnKeyListener(mLocalKeyListener);
        inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        // underline text
        mPaint.style = Paint.Style.STROKE
        mPaint.color = -0x80000000

        if (null == emptyErrorString || TextUtils.isEmpty(emptyErrorString)) {
            emptyErrorString = resources.getString(R.string.error_field_must_not_be_empty)
        }
        addValidator(
            typedArray.getInt(R.styleable.EditTextExt_validate, TEST_REGEXP), errorString,
            typedArray.getString(R.styleable.EditTextExt_validatorRegexp)
        )

        typedArray.recycle()
        addTextChangedListener(errorValidateTextWatcher)

    }

    var textExt: String?
        get() {
            return this.text.toString()
        }
        set(value) {
            super.setText(formatString(value))
        }

    override fun setBackgroundResource(resId: Int){
        background = resId
        super.setBackgroundResource(resId)
    }

    fun setText(textx: String?, init: Boolean) {
        val bSkip = skipEditTagAlways
        if (init) {
            skipEditTagAlways = true
        }
        val tmp = formatString(textx)
        super.setText(tmp)

        skipEditTagAlways=bSkip
    }

    override fun onDraw(canvas: Canvas) {
        if (drawLines && isVisible && isEnabled) {
            val left: Float = left.toFloat()
            val right: Float = right.toFloat()
            val paddingTop: Float = paddingTop.toFloat()
            val paddingBottom: Float = paddingBottom.toFloat()
            val paddingLeft: Float = paddingLeft.toFloat()
            val paddingRight: Float = paddingRight.toFloat()
            val height: Float = height.toFloat()
            val lineHeight: Float = lineHeight.toFloat()
            val count = (height - paddingTop - paddingBottom) / lineHeight
            if (count >= 2) {
                for (i in 0 until count.toInt()) {
                    val baseline = lineHeight * (i + 1.1f) + paddingTop
                    canvas.drawLine(
                        left + paddingLeft,
                        baseline,
                        right - paddingRight,
                        baseline,
                        mPaint
                    )
                }
            }
            if (initdrawLines && count > 1 && autoScale && !resizeInProgress) {
                initdrawLines=false
                this.visibility=INVISIBLE
                val numOfLinesOnScreen: Float = ceil(count)
                if (numOfLinesOnScreen > linesLimit) {
                    resizeTextToSmallSize()
                }
                this.visibility=VISIBLE
            }
        }
        super.onDraw(canvas)
    }

    private fun addValidator(code: Int, error: String?, regExp: String?): Boolean {
        var mError = ""
        if (error != null) mError = error
        //if (code and TEST_ALL == 0) return false
        if (code == 0) return false
        val v = OrValidator()
        if ((code and TEST_REGEXP != 0) && null != regExp) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_regexp_not_valid)
            v.addValidator(RegExpValidator(regExp, mError))
        }
        if (code and TEST_NUMERIC != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_this_field_cannot_contain_special_character)
            v.addValidator(NumericValidator(mError))
        }
        if (code and TEST_ALPHA != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_only_standard_letters_are_allowed)
            v.addValidator(AlphaValidator(mError))
        }
        if (code and TEST_ALPHANUMERIC != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_this_field_cannot_contain_special_character)
            v.addValidator(AlphaNumericValidator(mError))
        }
        if (code and TEST_EMAIL != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_email_address_not_valid)
            v.addValidator(EmailValidator(mError))
        }
        if (code and TEST_CREDITCARD != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_creditcard_number_not_valid)
            v.addValidator(CreditCardValidator(mError))
        }
        if (code and TEST_PHONE != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_phone_not_valid)
            v.addValidator(PhoneValidator(mError))
        }
        if (code and TEST_DOMAINNAME != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_domain_not_valid)
            v.addValidator(DomainValidator(mError))
        }
        if (code and TEST_IPADDRESS != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_ip_not_valid)
            v.addValidator(IpValidator(mError))
        }
        if (code and TEST_WEBURL != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_url_not_valid)
            v.addValidator(WebUrlValidator(mError))
        }
        if (code and TEST_DECIMAL != 0) {
            if (null == error || TextUtils.isEmpty(error)) mError =
                resources.getString(R.string.error_only_numeric_digits_allowed2)
            v.addValidator(DecimalValidator(mError))
        }
        emptyAllowed = code and TEST_EMPTY != 0
        if (0 < v.count) validators.add(v)
        return true
    }

    val isValid: Boolean
        get() {
            if (!this.isVisible || skipEditTagAlways ){
                return true
            }
            //TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsErrorFlag,"")
            if (text.toString().trim { it <= ' ' }.isEmpty()) {
                return if (emptyAllowed) {
                    true
                } else {
                    error = emptyErrorString
                    //TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsErrorFlag,
                    //    ConstantsFixed.TagActionError)
                    false
                }
            }
            var b: Boolean
            for (v in validators) {
                b = try {
                    v.check(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                if (!b) {
                    var error: String? = v.getErrorMessage(this)
                    if (TextUtils.isEmpty(error)) error = errorString
                    if (error != null && !TextUtils.isEmpty(error)) setError(error)
                    //TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsErrorFlag,
                    //    ConstantsFixed.TagActionError)
                    return false
                }
            }
            return true
        }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (autoTest && !focused && null == error) isValid
    }

    private fun formatString(text: String?): String? {
        var originalString = text
        var longval = 0f

        if (displayFormat == null) {
            return text
        }
        if (originalString != null) {
            if (originalString.contains(",")) {
                originalString = originalString.replace(",".toRegex(), "")
            }
            longval = originalString.toFloat()
        }
        val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern(displayFormat)

        return formatter.format(longval)
    }

    fun setDBColumn(dBColumn: String?, dBtable: String?): EditTextExt {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBColumn, dBColumn)
        if (dBtable != null) TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsDBTable, dBtable)
        if (TagModify.getViewTagValue(this, ConstantsFixed.TagSection.TsMessColumn) == ""){
            setMessColumn(dBColumn)
        }
        return this
    }

    fun setMessColumn(messColumn: String?): EditTextExt {
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsMessColumn,messColumn)
        return this
    }

    fun setDBColumn(dBColumn: String?, dBTable: String?, groupno: Int): EditTextExt {
        setDBColumn(dBColumn, dBTable)
        TagModify.setViewTagValue(this, ConstantsFixed.TagSection.TsGroupno,groupno.toString())
        return this
    }

    private val errorValidateTextWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            if (s.isNotEmpty() && error != null) {
                error = null
            }
            if (skipEditTagAlways){
                //tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsModFlag.name,"")
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
            } else if (skipEditTagOnce) {
                skipEditTagOnce = false
                //tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsModFlag.name,"")
                tag = TagModify.deleteTagSection(tag, ConstantsFixed.TagSection.TsUserFlag.name)
                //} else if ( !TagModify.hasTagValue(tag.toString(),ConstantsFixed.TagSection.TsModFlag.name,ConstantsFixed.TagAction.New.name)){
            } else {
                // if mode (new, edit) change color and signal user has edit this field
                tag = TagModify.setTagValue(tag, ConstantsFixed.TagSection.TsUserFlag.name,ConstantsFixed.TagAction.Edit.name)
                colorView = ConstantsFixed.ColorBasic.Modified
            }
            if (autoTest) {
                _handlerTimer.removeCallbacks(_taskChecker)
                _handlerTimer.postDelayed(_taskChecker, AUTOTEST_TIMER.toLong())
            }

        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable) {  }
    }

    private val _taskChecker = Runnable { isValid }
    private val _handlerTimer = Handler(Looper.getMainLooper())

    companion object {
        private const val TEST_REGEXP = 0x0001
        private const val TEST_NUMERIC = 0x0002
        private const val TEST_ALPHA = 0x0004
        private const val TEST_ALPHANUMERIC = 0x0008
        private const val TEST_EMAIL = 0x0010
        private const val TEST_CREDITCARD = 0x0020
        private const val TEST_PHONE = 0x0040
        private const val TEST_DOMAINNAME = 0x0080
        private const val TEST_IPADDRESS = 0x0100
        private const val TEST_WEBURL = 0x0200
        private const val TEST_EMPTY = 0x0400
        private const val TEST_DECIMAL = 0x1000
        //private const val TEST_ALL = 0x07ff

        //private static final int TEST_NOCHECK = 0;
        private const val AUTOTEST_TIMER = 1000
    }

/*
    // region money
    fun setCurrencySymbol(currencySymbol: String, useCurrencySymbolAsHint: Boolean = false) {
        currencySymbolPrefix = currencySymbol
        if (useCurrencySymbolAsHint) hint = currencySymbolPrefix
        invalidateTextWatcher()
    }
*/
/*
    fun setMaxNumberOfDecimalDigits(maxDP: Int) {
        this.maxDP = maxDP
        invalidateTextWatcher()
    }

    private fun invalidateTextWatcher() {
        removeTextChangedListener(textWatcher)
        textWatcher = CurrencyInputWatcher(this, currencySymbolPrefix, locale, maxDP)
        addTextChangedListener(textWatcher)
    }

    fun getNumericValue(): Double {
        return parseMoneyValueWithLocale(
            locale,
            text.toString(),
            textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
            currencySymbolPrefix
        ).toDouble()
    }

    fun parseMoneyValueWithLocale(
        locale: Locale,
        value: String,
        groupingSeparator: String,
        currencySymbol: String
    ): Number {

        val valueWithoutSeparator = value.replace(groupingSeparator, "").replace(currencySymbol, "")
        return try {
            NumberFormat.getInstance(locale).parse(valueWithoutSeparator)!!
        } catch (exception: ParseException) {
            0
        }
    }

    fun getNumericValueBigDecimal(): BigDecimal {
        return BigDecimal(
            parseMoneyValueWithLocale(
                locale,
                text.toString(),
                textWatcher.decimalFormatSymbols.groupingSeparator.toString(),
                currencySymbolPrefix
            ).toString()
        )
    }
    */
    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        getText()?.length?.let { setSelection(it) }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        currencySymbolPrefix=DecimalFormatSymbols.getInstance().currency.symbol
        val symbolLength = currencySymbolPrefix.length
        if (selEnd < symbolLength && text.toString().length >= symbolLength) {
            setSelection(symbolLength)
            if (symbolLength == 1 && selEnd == 0){
                setSelection(0,1)
            }
        } else {
            super.onSelectionChanged(selStart, selEnd)
        }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (autoScale && !resizeInProgress) {
            val numOfLinesOnScreen: Float = calculateNumberOfLinesNedeed()
            if (numOfLinesOnScreen > linesLimit) {
                resizeTextToSmallSize()
            } else {
                resizeTextToNormalSize()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (autoScale) {
            if (originalViewWidth == 0) {
                originalViewWidth = measuredWidth
                originalViewWidth -= paddingRight + paddingLeft
            }
            if (originalTextSize == 0f) {
                originalTextSize = textSize
                initializeTextMeasurerPaint()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (autoScale && keyCode == KeyEvent.KEYCODE_ENTER) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    // end region


    private fun initializeTextMeasurerPaint() {
         textMeasuringPaint = Paint()
        textMeasuringPaint!!.typeface=typeface
        textMeasuringPaint!!.textSize = originalTextSize
    }

    private fun calculateNumberOfLinesNedeed(): Float {
        //val textSizeInPixels: Float = measureText()
        //return textSizeInPixels / originalViewWidth
        return ceil(measureText() / this.width.toDouble()).toFloat()
    }

    private fun measureText(): Float {
        var result = 0f
        if (textMeasuringPaint != null) {
            result = textMeasuringPaint!!.measureText(text.toString())
        }
        return result
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    private fun resizeTextToSmallSize() {
        val smallTextSize = originalTextSize * textScale
        val currentTextSize = textSize
        if (currentTextSize > smallTextSize) {
            playAnimation(currentTextSize, smallTextSize)
        }
    }

    private fun resizeTextToNormalSize() {
        val currentTextSize = textSize
        if (currentTextSize < originalTextSize) {
            playAnimation(currentTextSize, originalTextSize)
        }
    }

    private fun playAnimation(origin: Float, destination: Float) {
        val animator = ObjectAnimator.ofFloat(this, "textSize", origin, destination)
        animator.target = this
        animator.duration = animationDuration.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                resizeInProgress = true
            }

            override fun onAnimationEnd(animation: Animator) {
                resizeInProgress = false
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }
}