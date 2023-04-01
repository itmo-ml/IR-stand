package ru.itmo.stand.util.kmeans

import java.text.DecimalFormat
import java.util.*

interface Strings {
    companion object {

        fun fill(ch: Char, len: Int): String {
            val chars = CharArray(len)
            Arrays.fill(chars, ch)
            return chars.toString()
        }

        @JvmOverloads
        fun format(x: Float, trailingZeros: Boolean = false): String? {
            if (MathEx.isZero(x, 1E-7f)) {
                return if (trailingZeros) "0.0000" else "0"
            }
            val ax = Math.abs(x)
            return if (ax >= 1E-3f && ax < 1E7f) {
                if (trailingZeros) String.format("%.4f", x) else DECIMAL_FORMAT.format(x.toDouble())
            } else {
                String.format("%.4e", x)
            }
        }

        @JvmOverloads
        fun format(x: Double, trailingZeros: Boolean = false): String? {
            if (MathEx.isZero(x, 1E-14)) {
                return if (trailingZeros) "0.0000" else "0"
            }
            val ax = Math.abs(x)
            return if (ax >= 1E-3 && ax < 1E7) {
                if (trailingZeros) String.format("%.4f", x) else DECIMAL_FORMAT.format(x)
            } else {
                String.format("%.4e", x)
            }
        }

        /** Decimal format for floating numbers.  */
        val DECIMAL_FORMAT = DecimalFormat("#.####")
    }
}
