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
    SIGNED_INTEGER_LESS_OR_EQUAL(41);
    
    private final int code;

    Comparator(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
