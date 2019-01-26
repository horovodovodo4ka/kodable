package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.ObjectDekoder

class Def {
    @ObjectDekoder
    constructor(boo: List<List<Int>>)
    constructor(int: Int?, double: Double, short: Short, byte: Byte, float: Float, boolean: Boolean, long: Long, number: Number, string: String)
}