package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.Koder

@Koder
enum class E {
    @Default
    unknown,
    a,
    b;

}