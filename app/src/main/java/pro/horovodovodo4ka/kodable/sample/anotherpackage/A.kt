package pro.horovodovodo4ka.kodable.sample.anotherpackage

import pro.horovodovodo4ka.kodable.JsonName
import pro.horovodovodo4ka.kodable.ObjectDekoder

@ObjectDekoder
class A(@JsonName("aee") val iii: Int) {
    constructor(a: String) : this(1)

}