package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Kodable

class Def {
    constructor(boo: List<List<Int>>)
    @Kodable
    constructor(int: Int?, double: Double?, short: Short?, byte: Byte?, float: Float?, boolean: Boolean?, long: Long?, number: Number?, string: String?)
}