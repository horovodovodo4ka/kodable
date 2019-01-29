package pro.horovodovodo4ka.kodable.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind
import me.eugeniomarletti.kotlin.metadata.shadow.serialization.deserialization.getName
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import org.jetbrains.annotations.Nullable
import pro.horovodovodo4ka.kodable.core.CustomKodable
import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.DefaultKodableForType
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.Kodable
import pro.horovodovodo4ka.kodable.core.KodableName
import pro.horovodovodo4ka.kodable.core.KodableReader
import pro.horovodovodo4ka.kodable.core.defaults.BooleanKodable
import pro.horovodovodo4ka.kodable.core.defaults.ByteKodable
import pro.horovodovodo4ka.kodable.core.defaults.DoubleKodable
import pro.horovodovodo4ka.kodable.core.defaults.FloatKodable
import pro.horovodovodo4ka.kodable.core.defaults.IntKodable
import pro.horovodovodo4ka.kodable.core.defaults.LongKodable
import pro.horovodovodo4ka.kodable.core.defaults.NumberKodable
import pro.horovodovodo4ka.kodable.core.defaults.ShortKodable
import pro.horovodovodo4ka.kodable.core.defaults.StringKodable
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CLASS
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import kotlin.reflect.KClass

const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
const val packageName = "pro.horovodovodo4ka.kodable"

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
class GenerateProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    companion object {
        val INT_TYPE = Int::class.asClassName()
        val BYTE_TYPE = Byte::class.asClassName()
        val BOOLEAN_TYPE = Boolean::class.asClassName()
        val DOUBLE_TYPE = Double::class.asClassName()
        val FLOAT_TYPE = Float::class.asClassName()
        val LONG_TYPE = Long::class.asClassName()
        val NUMBER_TYPE = Number::class.asClassName()
        val SHORT_TYPE = Short::class.asClassName()
        val STRING_TYPE = String::class.asClassName()

        private val defaults = mapOf(
            INT_TYPE to IntKodable::class.asClassName(),
            STRING_TYPE to StringKodable::class.asClassName(),
            BYTE_TYPE to ByteKodable::class.asClassName(),
            BOOLEAN_TYPE to BooleanKodable::class.asClassName(),
            DOUBLE_TYPE to DoubleKodable::class.asClassName(),
            FLOAT_TYPE to FloatKodable::class.asClassName(),
            LONG_TYPE to LongKodable::class.asClassName(),
            NUMBER_TYPE to NumberKodable::class.asClassName(),
            SHORT_TYPE to ShortKodable::class.asClassName()
        ).toMutableMap()


        val LIST_TYPE = kotlin.collections.List::class.asClassName()
        val KODABLE_TYPE = IKodable::class.asClassName()
        val READER_TYPE = KodableReader::class.asClassName()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        Kodable::class.java.canonicalName,
        DefaultKodableForType::class.java.canonicalName
    )

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(DefaultKodableForType::class.java).forEach(::registerDefaultKodable)
        roundEnv.getElementsAnnotatedWith(Kodable::class.java).forEach(::prefetchTypes)
        processPrefetchedTypes()
        return false
    }


    // selector
    private val prefetchedTypes = mutableListOf<TypeName>()
    private val prefetchedProcessors = mutableListOf<() -> Boolean>()
    private fun prefetchTypes(element: Element) {
        val meta = element.kotlinMetadata as? KotlinClassMetadata ?: return
        val proto = meta.data.classProto
        val processingFun = when (proto.classKind) {
            Kind.ENUM_CLASS -> ::generateEnumDekoder
            Kind.CLASS -> when {
                proto.isDataClass -> ::generateObjectDekoder
                else -> ::generateObjectDekoder
            }
            else -> throw Exception("Unsupported type $element: must be class or enum")
        }

        prefetchedTypes.add((element as TypeElement).asClassName())
        prefetchedProcessors.add { processingFun(element) }
    }

    private fun processPrefetchedTypes() {
        prefetchedProcessors.forEach { it() }
    }
    // defaults

    private fun registerDefaultKodable(element: Element) {
        if (element !is TypeElement) return

        val kodable = element.asClassName()
        val targetType = element.annotationMirrors.firstOrNull { it.annotationType.asTypeName() == DefaultKodableForType::class.asTypeName() }
            ?.elementValues?.entries?.firstOrNull()?.value?.value?.let { ClassName.bestGuess(it.toString()) } ?: return

        element.interfaces
            .mapNotNull { it.asTypeName() as? ParameterizedTypeName }
            .firstOrNull { it.rawType == KODABLE_TYPE && it.typeArguments.firstOrNull() == targetType } ?: throw Exception("Type $kodable does not implements IKodable<$targetType>")

        defaults[targetType] = kodable
    }

    // objects
    class ClassMeta(val type: TypeElement, val constructor: ExecutableElement, val typeMeta: KotlinClassMetadata, val constructorMeta: ProtoBuf.Constructor)

    private fun getClassAndCostructor(element: Element): ClassMeta? {
        val type: TypeElement
        val constructor: ExecutableElement

        when (element.kind) {
            CLASS -> {
                type = element as TypeElement
                constructor = ElementFilter.constructorsIn(type.enclosedElements).firstOrNull() ?: return null
            }
            CONSTRUCTOR -> {
                constructor = element as ExecutableElement
                type = element.enclosingElement as TypeElement
            }
            else -> return null
        }

        val typeMeta: KotlinClassMetadata = type.kotlinMetadata as? KotlinClassMetadata ?: return null
        val constructorMeta = constructor.asConstructorOrNull(typeMeta) ?: return null

        if (constructorMeta.valueParameterCount == 0) throw Exception("Class '$type' (or it's constructor) is marked by Kodable annotation, but constructor has no parameters")

        return ClassMeta(type, constructor, typeMeta, constructorMeta)
    }

    inner class PropDesc(private val parameter: VariableElement, val name: CharSequence) {
        val jsonName: CharSequence = parameter.getAnnotationsByType(KodableName::class.java).firstOrNull()?.jsonName ?: name
        val customKoder = parameter.customKoder()
        val nullable = parameter.getAnnotationsByType(Nullable::class.java).isNotEmpty()
        val nullableSuffix: String = if (nullable) "?" else ""
        val propertyType: ClassName get() = types.last()
        val propertyNameString get() = types.joinToString("<") { it.toString() } + types.joinToString(">") { "" } + nullableSuffix
        val listNestingCount: Int get() = types.size - 1
        val koderType: ClassName get() = customKoder ?: defaults[propertyType] ?: prefetchedTypes.firstOrNull { it == propertyType}?.let { propertyType.kodableName() } ?: throw Exception("Kodable for '$propertyType' is not defined and not generated")

        val types: List<ClassName>

        init {
            val typeName = parameter.asType().asTypeName()
            types = mutableListOf<ClassName>().also { unwrapType(typeName, it) }
        }

        private fun unwrapType(type: TypeName, result: MutableList<ClassName>) {
            when (type) {
                is ParameterizedTypeName -> {
                    val genericType = fixType(type.rawType)
                    if (genericType != LIST_TYPE) throw Exception("Only kotlin.collections.List is allowed! $genericType found")
                    result.add(genericType)
                    unwrapType(type.typeArguments.first(), result)
                }
                is WildcardTypeName -> {
                    val upperBounds = type.upperBounds.firstOrNull() ?: throw Exception("Only out variance is allowed for collection")
                    unwrapType(upperBounds, result)
                }
                is ClassName -> {
                    result.add(fixType(type))
                }
                else -> {
                }
            }
        }

        private fun fixType(type: ClassName): ClassName = when (type.toString()) {
            "java.lang.String" -> STRING_TYPE
            "java.lang.Integer" -> INT_TYPE
            "java.util.List" -> LIST_TYPE
            "java.lang.Number" -> NUMBER_TYPE
            else -> type
        }
    }

    private fun ExecutableElement.asConstructorOrNull(meta: KotlinClassMetadata): ProtoBuf.Constructor? {
        val sig1 = jvmMethodSignature
        return meta.data.classProto.constructorList.firstOrNull {
            val sig2 = it.getJvmConstructorSignature(meta.data.nameResolver, meta.data.classProto.typeTable)?.replace("$", "/")
            sig2 == sig1
        }
    }

    // enums

    class EnumMeta(val type: TypeElement, val typeMeta: KotlinClassMetadata, val entries: List<ProtoBuf.EnumEntry>, val defaultEntry: ProtoBuf.EnumEntry?)

    private fun getEnumClassAndDefault(element: Element): EnumMeta? {
        val meta = element.kotlinMetadata as? KotlinClassMetadata ?: return null
        val proto = meta.data.classProto
        if (proto.classKind != ProtoBuf.Class.Kind.ENUM_CLASS) return null

        val nameResolver = meta.data.nameResolver

        val clz = element as TypeElement
        val default = clz.enclosedElements
            .firstOrNull { it.getAnnotationsByType(Default::class.java).isNotEmpty() }
            ?.let { field ->
                proto.enumEntryList
                    .firstOrNull { field.toString() == nameResolver.getString(it.name) } ?: throw Exception("Annotation @Default must be used only with enum value, not enum property")
            }

        return EnumMeta(element, meta, proto.enumEntryList, default)
    }


// region    =========== Value ===========

    private fun generateEnumDekoder(element: Element): Boolean {
        val meta = getEnumClassAndDefault(element) ?: return false
        val nameResolver = meta.typeMeta.data.nameResolver

        val clz = meta.type.asClassName()
        val kodable = clz.kodableName()

        val intrface = ParameterizedTypeName.get(KODABLE_TYPE, clz)

        val newType = TypeSpec
            .objectBuilder(kodable)
            .addModifiers(PUBLIC)
            .addSuperinterface(intrface)
            .addFunction(
                FunSpec
                    .builder("readValue")
                    .addModifiers(OVERRIDE)
                    .addParameter("reader", READER_TYPE)
                    .returns(clz)
                    .apply {
                        if (meta.defaultEntry != null)
                            addStatement("return try { %1T.valueOf(reader.readString()) } catch (_: Exception) { %1T.${nameResolver.getString(meta.defaultEntry.name)} }", clz)
                        else
                            addStatement("return enumValueOf<%T>(reader.readString())", clz)
                    }
                    .build()
            )
            .build()

        val file = FileSpec
            .builder(kodable.packageName(), kodable.simpleName())
            .addStaticImport("$packageName.core", "defaults.kodable")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName())
            .addType(newType)
            .addFunction(extFunSpec(clz, kodable))
            .build()

        val output = generatedDir!!
        output.mkdir()

        file.writeTo(output)

        return true
    }

// endregion =========== Value ===========

// region    =========== Object ===========


    private fun generateObjectDekoder(element: Element): Boolean {
        val meta = getClassAndCostructor(element) ?: return false
        val nameResolver = meta.typeMeta.data.nameResolver

        val params = meta.constructorMeta.valueParameterList.mapIndexed { idx, parameter ->
            val variableElement = meta.constructor.parameters[idx]
            val name = nameResolver.getName(parameter.name).asString()

            PropDesc(variableElement, name)
        }

        val clz = meta.type.asClassName()
        val kodable = clz.kodableName()

        val intrface = ParameterizedTypeName.get(KODABLE_TYPE, clz)

        val newType = TypeSpec
            .objectBuilder(kodable)
            .addModifiers(PUBLIC)
            .addSuperinterface(intrface)
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build())
            .addFunction(
                FunSpec
                    .builder("readValue")
                    .addModifiers(OVERRIDE)
                    .addParameter("reader", READER_TYPE)
                    .returns(clz)
                    .apply {
                        params.forEach {
                            addStatement("var ${it.name}: Any? = null")
                        }

                        addStatement("reader.readElementsFromMap {")
                        addStatement("when (it) {")

                        params.forEach {
                            val lists = 0.until(it.listNestingCount).joinToString("") { ".list" }
                            addStatement(""" "${it.jsonName}" -> ${it.name} = ${it.koderType}$lists.readValueOrNull(reader) """)
                        }

                        addStatement("else -> reader.skipValue()")
                        addStatement("}")
                        addStatement("}")


                        addStatement("return %T(${params.joinToString(", ") {
                            "${it.name} = ${it.name} as ${it.propertyNameString}"
                        }})", clz)
                    }
                    .build()
            )
            .build()

        val file = FileSpec
            .builder(kodable.packageName(), kodable.simpleName())
            .addStaticImport("$packageName.core", "defaults.kodable")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName())
            .addType(newType)
            .addFunction(extFunSpec(clz, kodable))
            .build()

        val output = generatedDir!!
        output.mkdir()

        file.writeTo(output)

        return true
    }

    private fun extFunSpec(type: TypeName, kodableType: ClassName): FunSpec {
        val kclassType = ParameterizedTypeName.get(KClass::class.asClassName(), type)
        val kodable = ParameterizedTypeName.get(IKodable::class.asClassName(), type)

        return FunSpec
            .builder("kodable")
            .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember("\"${kodableType.simpleName()}_kodable\"").build())
            .receiver(kclassType)
            .returns(kodable)
            .addStatement("return " + kodableType.simpleName())
            .build()
    }

// endregion =========== Object ===========

    private fun printError(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun printWarning(message: String) {
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, message)
    }
}

private fun ClassName.kodableName(): ClassName {
    val fullName = simpleNames().joinToString("_")
    return ClassName(packageName() + ".generated", "${fullName}_Kodable")
}

private fun Element.customKoder() =
    annotationMirrors.firstOrNull { it.annotationType.asTypeName() == CustomKodable::class.asTypeName() }?.elementValues?.entries?.firstOrNull()?.value?.value?.let { ClassName.bestGuess(it.toString()) }
