package pro.horovodovodo4ka.kodable.sample.anotherpackage

import pro.horovodovodo4ka.kodable.core.KodableName
import pro.horovodovodo4ka.kodable.core.Koder

@Koder
data class A(@KodableName("aee") val iii: Int = 1)