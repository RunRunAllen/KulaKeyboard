package com.freddy.kulakeyboard.sample

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.freddy.kulakeyboard.library.IInputPanel
import com.freddy.kulakeyboard.library.KulaKeyboardHelper
import com.freddy.kulakeyboard.library.OnInputPanelStateChangedListener
import com.freddy.kulakeyboard.library.util.DensityUtil
import com.freddy.kulakeyboard.library.util.UIUtil
import kotlinx.android.synthetic.main.layout_input_panel.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author  FreddyChen
 * @name
 * @date    2020/06/08 13:40
 * @email   chenshichao@outlook.com
 * @github  https://github.com/FreddyChen
 * @desc
 */
class CInputPanel : LinearLayout, IInputPanel {

    private var panelType = PanelType.NONE
    private var lastPanelType = panelType
    private var isKeyboardOpened = false

    companion object {
        const val TAG = "CInputPanel"
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.layout_input_panel, this, true)
        init()
        setListeners()
    }

    private var isActive = false

    enum class PanelType {
        INPUT_MOTHOD, EXPRESSION, MORE, NONE
    }

    private fun init() {
        orientation = HORIZONTAL
        setPadding(
            DensityUtil.dp2px(context, 10.0f),
            DensityUtil.dp2px(context, 6.0f),
            DensityUtil.dp2px(context, 10.0f),
            DensityUtil.dp2px(context, 6.0f)
        )
        gravity = Gravity.BOTTOM
        setBackgroundColor(ContextCompat.getColor(context, R.color.c_77cbcbcb))
        et_content.inputType = InputType.TYPE_NULL
        et_content.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (!isKeyboardOpened) {
                    GlobalScope.launch(Dispatchers.Main) {
                        delay(100)
                        UIUtil.requestFocus(et_content)
                        UIUtil.showSoftInput(context, et_content)
                    }
                    et_content.resetInputType()
                    btn_expression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
                    btn_expression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
                    handleAnimator(PanelType.INPUT_MOTHOD)
                    onInputPanelStateChangedListener?.onShowInputMethodPanel()
                }
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun setListeners() {
        btn_voice.setOnClickListener { }
        btn_expression.setOnClickListener {
            if (lastPanelType == PanelType.EXPRESSION) {
                btn_expression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
                btn_expression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
                UIUtil.requestFocus(et_content)
                UIUtil.showSoftInput(context, et_content)
                handleAnimator(PanelType.INPUT_MOTHOD)
                et_content.resetInputType()
            } else {
                btn_expression.setNormalImageResId(R.drawable.ic_chat_keyboard_normal)
                btn_expression.setPressedImageResId(R.drawable.ic_chat_keyboard_pressed)
                UIUtil.loseFocus(et_content)
                UIUtil.hideSoftInput(context, et_content)
                handleAnimator(PanelType.EXPRESSION)
                onInputPanelStateChangedListener?.onShowExpressionPanel()
            }
        }
        btn_more.setOnClickListener { }
        et_content.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Toast.makeText(context, "发送", Toast.LENGTH_SHORT).show()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun handleAnimator(panelType: PanelType) {
        Log.d(TAG, "lastPanelType = $lastPanelType\tpanelType = $panelType")
        if (lastPanelType == panelType) {
            return
        }
        isActive = true
        Log.d(TAG, "isActive = $isActive")
        this.panelType = panelType
        var fromValue = 0.0f
        var toValue = 0.0f
        when (panelType) {
            PanelType.INPUT_MOTHOD ->
                when (lastPanelType) {
                    PanelType.EXPRESSION -> {
                        fromValue = -KulaKeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = -KulaKeyboardHelper.inputPanelHeight.toFloat()
                    }
                    PanelType.MORE -> {
                    }
                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = -KulaKeyboardHelper.inputPanelHeight.toFloat()
                    }
                    else -> {
                    }
                }
            PanelType.EXPRESSION ->
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KulaKeyboardHelper.inputPanelHeight.toFloat()
                        toValue = -KulaKeyboardHelper.expressionPanelHeight.toFloat()
                    }
                    PanelType.MORE -> {
                    }
                    PanelType.NONE -> {
                        fromValue = 0.0f
                        toValue = -KulaKeyboardHelper.expressionPanelHeight.toFloat()
                    }
                    else -> {
                    }
                }
            PanelType.MORE ->
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                    }
                    PanelType.EXPRESSION -> {
                    }
                    PanelType.NONE -> {
                    }
                    else -> {
                    }
                }
            PanelType.NONE ->
                when (lastPanelType) {
                    PanelType.INPUT_MOTHOD -> {
                        fromValue = -KulaKeyboardHelper.inputPanelHeight.toFloat()
                        toValue = 0.0f
                    }
                    PanelType.EXPRESSION -> {
                        fromValue = -KulaKeyboardHelper.expressionPanelHeight.toFloat()
                        toValue = 0.0f
                    }
                    PanelType.MORE -> {
                    }
                    else -> {
                    }
                }
        }
        onLayoutAnimatorHandleListener?.invoke(fromValue, toValue)
        lastPanelType = panelType
    }

    private var onLayoutAnimatorHandleListener: ((fromValue: Float, toValue: Float) -> Unit)? = null
    private var onInputPanelStateChangedListener: OnInputPanelStateChangedListener? = null
    override fun onSoftKeyboardOpened() {
        isKeyboardOpened = true
        et_content.resetInputType()
    }

    override fun onSoftKeyboardClosed() {
        isKeyboardOpened = false
        et_content.inputType = InputType.TYPE_NULL
        if (lastPanelType == PanelType.INPUT_MOTHOD) {
            UIUtil.loseFocus(et_content)
            UIUtil.hideSoftInput(context, et_content)
            handleAnimator(PanelType.NONE)
        }
    }

    override fun setOnLayoutAnimatorHandleListener(listener: ((fromValue: Float, toValue: Float) -> Unit)?) {
        this.onLayoutAnimatorHandleListener = listener
    }

    override fun setOnInputStateChangedListener(listener: OnInputPanelStateChangedListener?) {
        this.onInputPanelStateChangedListener = listener
    }

    override fun reset() {
        if (!isActive) {
            return
        }
        Log.d(TAG, "reset()")
        UIUtil.loseFocus(et_content)
        UIUtil.hideSoftInput(context, et_content)
        btn_expression.setNormalImageResId(R.drawable.ic_chat_expression_normal)
        btn_expression.setPressedImageResId(R.drawable.ic_chat_expression_pressed)
        GlobalScope.launch(Dispatchers.Main) {
            delay(100)
            handleAnimator(PanelType.NONE)
        }
        isActive = false
    }

    override fun release() {
        Log.d(TAG, "release()")
        reset()
    }

    override fun getPanelHeight(): Int {
        return KulaKeyboardHelper.keyboardHeight
    }
}