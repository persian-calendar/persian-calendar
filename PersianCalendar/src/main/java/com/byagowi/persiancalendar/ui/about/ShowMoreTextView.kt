//package com.byagowi.persiancalendar.ui.about
//
//import android.content.Context
//import android.os.Parcelable
//import android.text.Spannable
//import android.text.SpannableString
//import android.text.TextPaint
//import android.text.method.LinkMovementMethod
//import android.text.style.ClickableSpan
//import android.text.style.ForegroundColorSpan
//import android.util.AttributeSet
//import android.view.View
//import android.view.ViewTreeObserver.OnGlobalLayoutListener
//import androidx.annotation.Nullable
//import androidx.appcompat.widget.AppCompatTextView
//import com.byagowi.persiancalendar.R
//
//class ShowMoreTextView : AppCompatTextView {
//    private var showingLine = 1
//    private var showingChar = 0
//    private var isCharEnable = false
//    private var showMore = "اطلاعات بیشتر"
//    private var showLess = "اطلاعات کمتر"
//    private val dot = "..."
//    private val magicNumber = 5
//    private var showMoreTextColor = R.attr.coloredTabTextColor
//    private var showLessTextColor = R.attr.coloredTabTextColor
//    private var mainText: String? = null
//    private var isAlreadySet = false
//
//    constructor(context: Context?) : super(context!!)
//    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
//
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        mainText = text.toString()
//    }
//
//    override fun onRestoreInstanceState(state: Parcelable) {
//        super.onRestoreInstanceState(state)
//    }
//
//    private fun addShowMore() {
//        val vto = viewTreeObserver
//        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                val text = text.toString()
//                if (!isAlreadySet) {
//                    mainText = getText().toString()
//                    isAlreadySet = true
//                }
//                var showingText = ""
//                if (isCharEnable) {
//                    if (showingChar >= text.length) {
//                        try {
//                            throw Exception("Character count cannot be exceed total line count")
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                    var newText = text.substring(0, showingChar)
//                    newText += dot + showMore
//                    SaveState.isCollapse = true
//                    setText(newText)
//                } else {
//                    if (showingLine >= lineCount) {
//                        try {
//                            throw Exception("Line Number cannot be exceed total line count")
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                        viewTreeObserver.removeOnGlobalLayoutListener(this)
//                        return
//                    }
//                    var start = 0
//                    var end: Int
//                    for (i in 0 until showingLine) {
//                        end = layout.getLineEnd(i)
//                        showingText += text.substring(start, end)
//                        start = end
//                    }
//                    var newText = showingText.substring(
//                        0,
//                        showingText.length - (dot.length + showMore.length + magicNumber)
//                    )
//                    newText += dot + showMore
//                    SaveState.isCollapse = true
//                    setText(newText)
//                }
//                setShowMoreColoringAndClickable()
//                viewTreeObserver.removeOnGlobalLayoutListener(this)
//            }
//        })
//    }
//
//    private fun setShowMoreColoringAndClickable() {
//        val spannableString = SpannableString(text)
//        spannableString.setSpan(
//            object : ClickableSpan() {
//                override fun updateDrawState(ds: TextPaint) {
//                    ds.isUnderlineText = false
//                }
//
//                override fun onClick(@Nullable view: View) {
//                    maxLines = Int.MAX_VALUE
//                    text = mainText
//                    SaveState.isCollapse = false
//                    showLessButton()
//                }
//            },
//            text.length - (dot.length + showMore.length),
//            text.length, 0
//        )
//        spannableString.setSpan(
//            ForegroundColorSpan(showMoreTextColor),
//            text.length - (dot.length + showMore.length),
//            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        movementMethod = LinkMovementMethod.getInstance()
//        setText(spannableString, BufferType.SPANNABLE)
//    }
//
//    private fun showLessButton() {
//        val text = text.toString() + dot + showLess
//        val spannableString = SpannableString(text)
//        spannableString.setSpan(
//            object : ClickableSpan() {
//                override fun updateDrawState(ds: TextPaint) {
//                    ds.isUnderlineText = false
//                }
//
//                override fun onClick(@Nullable view: View) {
//                    maxLines = showingLine
//                    addShowMore()
//                }
//            },
//            text.length - (dot.length + showLess.length),
//            text.length, 0
//        )
//        spannableString.setSpan(
//            ForegroundColorSpan(showLessTextColor),
//            text.length - (dot.length + showLess.length),
//            text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        movementMethod = LinkMovementMethod.getInstance()
//        setText(spannableString, BufferType.SPANNABLE)
//    }
//
//    fun setShowingLine(lineNumber: Int) {
//        if (lineNumber == 0) {
//            try {
//                throw Exception("Line Number cannot be 0")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            return
//        }
//        isCharEnable = false
//        showingLine = lineNumber
//        maxLines = showingLine
//        if (SaveState.isCollapse) {
//            addShowMore()
//        } else {
//            maxLines = Int.MAX_VALUE
//            showLessButton()
//        }
//    }
//
//    fun setShowingChar(character: Int) {
//        if (character == 0) {
//            try {
//                throw Exception("Character length cannot be 0")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//            return
//        }
//        isCharEnable = true
//        showingChar = character
//        if (SaveState.isCollapse) {
//            addShowMore()
//        } else {
//            maxLines = Int.MAX_VALUE
//            showLessButton()
//        }
//    }
//
//    companion object {
//        internal object SaveState {
//            var isCollapse = true
//        }
//    }
//}
