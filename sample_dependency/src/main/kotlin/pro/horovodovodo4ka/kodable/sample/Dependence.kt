package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Koder
import pro.horovodovodo4ka.kodable.sample.nested.DT

@Koder
data class Dependence(val some: Int)


@Koder
internal data class InversedDT(val dt: DT)