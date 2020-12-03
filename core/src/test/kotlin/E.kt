import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.Koder

@Koder
enum class E(val jsonCase: String) {
    @Default
    unknown("unknown"),
    a("a"),
    b("b"),
    logo("MainLogo");

}