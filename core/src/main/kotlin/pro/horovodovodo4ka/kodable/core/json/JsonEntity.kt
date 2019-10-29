package pro.horovodovodo4ka.kodable.core.json

enum class JsonEntity {
    string,
    boolean,
    number,
    `null`,
    `array`,
    `object`,
    eof,
    undefined;
}