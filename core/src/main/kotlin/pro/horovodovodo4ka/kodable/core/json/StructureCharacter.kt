package pro.horovodovodo4ka.kodable.core.json

enum class StructureCharacter(val char: Char) {
    BEGIN_ARRAY('['),
    END_ARRAY(']'),
    BEGIN_OBJECT('{'),
    END_OBJECT('}'),
    NAME_SEPARATOR(':'),
    VALUE_SEPARATOR(',');
}

