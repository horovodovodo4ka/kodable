package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.Dekoder

@Dekoder
enum class E {
    @Default
    unknown,
    a,
    b;

}