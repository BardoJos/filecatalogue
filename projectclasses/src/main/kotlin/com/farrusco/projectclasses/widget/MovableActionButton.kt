package com.farrusco.projectclasses.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.farrusco.projectclasses.utils.Logging
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MovableActionButton : FloatingActionButton, View.OnTouchListener {

    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    //late init var coordinatorLayout: ViewGroup.LayoutParams
    private var bProccess = false
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        //if (bProccess) return true
        bProccess = true
        val bRtn: Boolean
        val viewParent: View
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Logging.d("MovableFAB", "ACTION_DOWN")
                downRawX = motionEvent.rawX
                downRawY = motionEvent.rawY
                dX = view.x - downRawX
                dY = view.y - downRawY
                bRtn = true // Consumed
            }
            MotionEvent.ACTION_MOVE -> {
                val viewWidth: Int = view.width
                val viewHeight: Int = view.height
                viewParent = view.parent as View
                val parentWidth: Int = viewParent.width
                val parentHeight: Int = viewParent.height
                var newX = motionEvent.rawX + dX
                newX =
                    0f.coerceAtLeast(newX) // Don't allow the FAB past the left hand side of the parent
                newX = (parentWidth - viewWidth).toFloat()
                    .coerceAtMost(newX) // Don't allow the FAB past the right hand side of the parent
                var newY = motionEvent.rawY + dY
                newY = 0f.coerceAtLeast(newY) // Don't allow the FAB past the top of the parent
                newY = (parentHeight - viewHeight).toFloat()
                    .coerceAtMost(newY) // Don't allow the FAB past the bottom of the parent
                view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
                bRtn = true // Consumed
            }
            else -> bRtn = super.onTouchEvent(motionEvent)
        }
        bProccess=false
        return bRtn
    }
}