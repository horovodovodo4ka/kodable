package pro.horovodovodo4ka.kodable.sample.nested

import pro.horovodovodo4ka.kodable.core.Koder
import pro.horovodovodo4ka.kodable.sample.Dependence

@Koder
internal data class DT(val dependence: Dependence)
