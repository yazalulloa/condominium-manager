package kyo.yaz.condominium.manager.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

public class DecimalUtil {

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private DecimalUtil() {
        super();
    }

    public static BigDecimal percentageOf(double percentage, double total) {
        return percentageOf(BigDecimal.valueOf(percentage), BigDecimal.valueOf(total));
    }

    public static BigDecimal percentageOf(BigDecimal percentage, BigDecimal total) {
        return percentageOf(percentage, total, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentageOf(BigDecimal percentage, BigDecimal total, int scale, RoundingMode roundingMode) {

        if (percentage.compareTo(HUNDRED) == 0) {
            return total;
        }

        return percentage.multiply(total).divide(HUNDRED, scale, roundingMode);
    }


    public static BigDecimal randomBigDecimal(int integer, int fraction) {
        return new BigDecimal(RandomUtil.randomIntStr(integer) + "." + RandomUtil.randomIntStr(fraction));
    }

    public static boolean equalsToZero(BigDecimal decimal) {
        return equalsTo(decimal, BigDecimal.ZERO);
    }

    public static boolean equalsTo(BigDecimal lhs, BigDecimal rhs) {
        return lhs.compareTo(rhs) == 0;
    }

    public static boolean greaterThan(BigDecimal lhs, BigDecimal rhs) {
        return lhs.compareTo(rhs) > 0;
    }

    public static boolean greaterThanEquals(BigDecimal lhs, BigDecimal rhs) {
        return lhs.compareTo(rhs) >= 0;
    }

    public static boolean lessThan(BigDecimal lhs, BigDecimal rhs) {
        return lhs.compareTo(rhs) < 0;
    }

    public static boolean lessThanEquals(BigDecimal lhs, BigDecimal rhs) {
        return lhs.compareTo(rhs) <= 0;
    }

    public static boolean greaterThanZero(BigDecimal decimal) {
        return greaterThan(decimal, BigDecimal.ZERO);
    }

    public static boolean zeroOrLess(BigDecimal decimal) {
        return lessThanEquals(decimal, BigDecimal.ZERO);
    }


    public static int getNumberOfDecimalPlaces(String string) {
        int index = string.indexOf(".");
        return index <= 0 ? 0 : string.length() - index - 1;
    }

    public static int getNumberOfDecimalPlaces(double value) {
        return getNumberOfDecimalPlaces(String.valueOf(value));
    }


    public static int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        return getNumberOfDecimalPlaces(string);
    }

    public static void validateNumberOfDecimals(BigDecimal bigDecimal) {
        validateNumberOfDecimals(bigDecimal, 2, () -> new IllegalArgumentException("INVALID_NUMBER"));
    }

    public static void validateNumberOfDecimals(BigDecimal bigDecimal, int numberOfDecimal, Supplier<RuntimeException> supplier) {
        if (getNumberOfDecimalPlaces(bigDecimal) > numberOfDecimal) {
            throw supplier.get();
        }
    }

    public static boolean validateMinMaxMultiple(BigDecimal value, BigDecimal min, BigDecimal max, BigDecimal multiple) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0 && isMultipleOf(value, multiple);
    }

    public static boolean isMultipleOf(BigDecimal value, BigDecimal multiple) {
        return value.remainder(multiple).compareTo(BigDecimal.ZERO) == 0;
    }

    public static BigDecimal fractionPart(BigDecimal value) {
        return value.remainder(BigDecimal.ONE);
    }

    public static boolean isFractionPartGreaterThanZero(BigDecimal value) {
        return greaterThanZero(fractionPart(value));
    }

    public static boolean isFractionPartEqualsToZero(BigDecimal value) {
        return equalsToZero(fractionPart(value));
    }

    public static BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
