package pro.horovodovodo4ka.kodable.sample

import pro.horovodovodo4ka.kodable.Default
import pro.horovodovodo4ka.kodable.SingleValueDekoder

@SingleValueDekoder
enum class E {
    @Default
    unknown,
    a,
    b;

}