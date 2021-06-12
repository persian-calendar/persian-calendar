package com.byagowi.persiancalendar.ui.preferences

import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.createStatusIcon
import com.byagowi.persiancalendar.utils.dp
import com.byagowi.persiancalendar.utils.getDayIconResource

// Debug only dialog to check validity of dynamic icons generation
fun Fragment.showIconsDemoDialog() {
    val context = layoutInflater.context
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        Toast.makeText(context, "Not supported", Toast.LENGTH_LONG).show()
        return
    }
    AlertDialog.Builder(context)
        .setView(TableLayout(context).also { tableLayout ->
            (1..31)
                .map { number -> createStatusIcon(context, number) }
                .chunked(7)
                .forEach { chunk ->
                    tableLayout.addView(TableRow(context).also { tableRow ->
                        chunk.forEach { icon ->
                            tableRow.addView(FrameLayout(context).also { frameLayout ->
                                frameLayout.setPadding(4.dp)
                                frameLayout.addView(ImageView(context).also { imageView ->
                                    imageView.setImageIcon(icon.toIcon(context))
                                    imageView.setBackgroundColor(Color.BLACK)
                                    imageView.layoutParams =
                                        ViewGroup.LayoutParams(40.dp, 40.dp)
                                })
                            })
                        }
                    })
                }
            (1..31)
                .map { number -> getDayIconResource(number) }
                .chunked(7)
                .forEach { chunk ->
                    tableLayout.addView(TableRow(context).also { tableRow ->
                        chunk.forEach { icon ->
                            tableRow.addView(FrameLayout(context).also { frameLayout ->
                                frameLayout.setPadding(4.dp)
                                frameLayout.addView(ImageView(context).also { imageView ->
                                    imageView.setImageResource(icon)
                                    imageView.setBackgroundColor(Color.BLACK)
                                    imageView.layoutParams =
                                        ViewGroup.LayoutParams(40.dp, 40.dp)
                                })
                            })
                        }
                    })
                }
            tableLayout.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            tableLayout.setBackgroundColor(Color.WHITE)
        })
        .setCancelable(true)
        .setNegativeButton(R.string.cancel, null)
        .show()
}
