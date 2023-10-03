package org.voidlang.llvm.element;

public enum Comparator {
    /** equal */
    INTEGER_EQUAL(32),

    /** not equal */
    INTEGER_NOT_EQUAL(33),

    /** unsigned greater than */
    UNSIGNED_INTEGER_GREATER_THAN(34),

    /** unsigned greater or equal */
    UNSIGNED_INTEGER_GREATER_OR_EQUAL(35),

    /** unsigned less than */
    UNSIGNED_INTEGER_LESS_THAN(36),

    /** unsigned less or equal */
    UNSIGNED_INTEGER_LESS_OR_EQUAL(37),

    /** signed greater than */
    SIGNED_INTEGER_GREATER_THAN(38),

    /** signed greater or equal */
    SIGNED_INTEGER_GREATER_OR_EQUAL(39),

    /** signed less than */
    SIGNED_INTEGER_LESS_THAN(40),

    /** signed less or equal */
    SIGNED_INTEGER_LESS_OR_EQUAL(41),


    /** True if ordered and equal */
    FLOAT_EQUAL_AND_NOT_NAN(1),

    /** True if ordered and greater than */
    FLOAT_GREATER_THAN_AND_NOT_NAN(2),

    /** True if ordered and greater than or equal */
    FLOAT_GREAT_OR_EQUAL_AND_NOT_NAN(3),

    /** True if ordered and less than */
    FLOAT_LESS_THAN_AND_NOT_NAN(4),

    /** True if ordered and less than or equal */
    FLOAT_LESS_OR_EQUAL_AND_NOT_NAN(5),

    /** True if ordered and operands are unequal */
    FLOAT_NOT_EQUAL_AND_NOT_NAN(6),

    /** True if ordered (no nans) */
    FLOAT_NOT_NAN(7),

    /** True if unordered: isnan(X) | isnan(Y) */
    FLOAT_IS_NAN(8),

    /** True if unordered or equal */
    FLOAT_EQUAL_OR_NAN(9),

    /** True if unordered or greater than */
    FLOAT_GREATER_THAN_OR_NAN(10),

    /** True if unordered, greater than, or equal */
    FLOAT_GREATER_THAN_OR_EQUAL_OR_NAN(11),

    /** True if unordered or less than */
    FLOAT_LESS_THAN_OR_EQUAL(12),

    /** True if unordered, less than, or equal */
    FLOAT_LESS_THAN_OR_EQUAL_OR_NAN(13),

    /** True if unordered or not equal */
    FLOAT_NOT_EQUAL_OR_NAN(14);
    
    private final int code;

    Comparator(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
