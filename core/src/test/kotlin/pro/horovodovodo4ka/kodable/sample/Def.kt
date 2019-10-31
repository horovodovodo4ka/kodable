package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Dekoder

class Def {
    constructor(boo: List<List<Int>>)
    @Dekoder
    constructor(
        int: Int?,
        double: Double?,
        short: Short?,
        byte: Byte?,
        float: Float?,
        boolean: Boolean?,
        long: Long?,
        number: Number?,
        string: String?
    )
}