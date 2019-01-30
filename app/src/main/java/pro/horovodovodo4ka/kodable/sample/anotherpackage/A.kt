package pro.horovodovodo4ka.kodable.sample.anotherpackage

import pro.horovodovodo4ka.kodable.core.KodableName
import pro.horovodovodo4ka.kodable.core.Dekoder

@Dekoder
class A(@KodableName("aee") val iii: Int = 1) {
    constructor(a: String) : this(1)

}