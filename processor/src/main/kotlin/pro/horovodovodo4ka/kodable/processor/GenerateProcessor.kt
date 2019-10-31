package pro.horovodovodo4ka.kodable.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadataUtils
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.getterVisibility
import me.eugeniomarletti.kotlin.metadata.hasGetter
import me.eugeniomarletti.kotlin.metadata.isDataClass
import me.eugeniomarletti.kotlin.metadata.isInnerClass
import me.eugeniomarletti.kotlin.metadata.jvm.getJvmConstructorSignature
import me.eugeniomarletti.kotlin.metadata.kaptGeneratedOption
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind.COMPANION_OBJECT
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind.ENUM_CLASS
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind.OBJECT
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility.INTERNAL
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility.LOCAL
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility.PRIVATE
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility.PRIVATE_TO_THIS
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Visibility.PROTECTED
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.serialization.deserialization.getName
import me.eugeniomarletti.kotlin.metadata.visibility
import me.eugeniomarletti.kotlin.processing.KotlinAbstractProcessor
import org.jetbrains.annotations.Nullable
import pro.horovodovodo4ka.kodable.core.CustomKodable
import pro.horovodovodo4ka.kodable.core.Default
import pro.horovodovodo4ka.kodable.core.DefaultKodableForType
import pro.horovodovodo4ka.kodable.core.Dekoder
import pro.horovodovodo4ka.kodable.core.Enkoder
import pro.horovodovodo4ka.kodable.core.IKodable
import pro.horovodovodo4ka.kodable.core.KodableName
import pro.horovodovodo4ka.kodable.core.Koder
import pro.horovodovodo4ka.kodable.core.defaults.BooleanKodable
import pro.horovodovodo4ka.kodable.core.defaults.ByteKodable
import pro.horovodovodo4ka.kodable.core.defaults.DoubleKodable
import pro.horovodovodo4ka.kodable.core.defaults.FloatKodable
import pro.horovodovodo4ka.kodable.core.defaults.IntKodable
import pro.horovodovodo4ka.kodable.core.defaults.LongKodable
import pro.horovodovodo4ka.kodable.core.defaults.NumberKodable
import pro.horovodovodo4ka.kodable.core.defaults.ShortKodable
import pro.horovodovodo4ka.kodable.core.defaults.StringKodable
import pro.horovodovodo4ka.kodable.core.json.JsonReader
import pro.horovodovodo4ka.kodable.core.json.JsonWriter
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.DEKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.ENKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.DATACLASS_KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.ENUM_KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.OBJECT_DEKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.OBJECT_ENKODER
import java.io.File
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import kotlin.Exception
import kotlin.reflect.KClass

const val packageName = "pro.horovodovodo4ka.kodable"

class Exception(message: String, val element: Element? = null) : kotlin.Exception(message)

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(kaptGeneratedOption)
class GenerateProcessor : KotlinAbstractProcessor(), KotlinMetadataUtils {

    companion object {
        private var processed = false

        val INT_TYPE = Int::class.asClassName()
        val BYTE_TYPE = Byte::class.asClassName()
        val BOOLEAN_TYPE = Boolean::class.asClassName()
        val DOUBLE_TYPE = Double::class.asClassName()
        val FLOAT_TYPE = Float::class.asClassName()
        val LONG_TYPE = Long::class.asClassName()
        val NUMBER_TYPE = Number::class.asClassName()
        val SHORT_TYPE = Short::class.asClassName()
        val STRING_TYPE = String::class.asClassName()

        private val defaults = mapOf<TypeName, TypeName>(
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
        val MAP_TYPE = kotlin.collections.Map::class.asClassName()
        val KCLASS_TYPE = KClass::class.asClassName()
        val KODABLE_INTERFACE_TYPE = IKodable::class.asClassName()
        val READER_TYPE = JsonReader::class.asClassName()
        val WRITER_TYPE = JsonWriter::class.asClassName()

        private val typesVisibility = mutableMapOf<TypeName, Visibility>()
        private val prefetchedDekoders = mutableListOf<TypeName>()
        private val prefetchedEnkoders = mutableListOf<TypeName>()
        private val prefetchedProcessors = mutableListOf<ProcessorDesc>()

        private var count = 0
    }

    init {
        count++
    }

    override fun getSupportedAnnotationTypes() = mutableSetOf(
        Dekoder::class.java.canonicalName,
        Enkoder::class.java.canonicalName,
        Koder::class.java.canonicalName,
        DefaultKodableForType::class.java.canonicalName
    )

    enum class AnnotationKind {
        DEKODER,
        ENKODER,
        KODER;
    }

    enum class ProcessorType {
        OBJECT_ENKODER,
        OBJECT_DEKODER,
        ENUM_KODER,
        DATACLASS_KODER
    }

    private val processingErrors = mutableListOf<kotlin.Exception>()
    private fun wrapThrowingCode(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            processingErrors.add(e)
        }
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (processed) return false
        printMessage("Kodable generating started")
        roundEnv.getElementsAnnotatedWith(DefaultKodableForType::class.java).forEach { wrapThrowingCode { registerDefaultKodable(it) } }
        roundEnv.getElementsAnnotatedWith(Dekoder::class.java).forEach { wrapThrowingCode { prefetchTypes(it, DEKODER) } }
        roundEnv.getElementsAnnotatedWith(Enkoder::class.java).forEach { wrapThrowingCode { prefetchTypes(it, ENKODER) } }
        roundEnv.getElementsAnnotatedWith(Koder::class.java).forEach { wrapThrowingCode { prefetchTypes(it, KODER) } }
        wrapThrowingCode { processPrefetchedTypes() }

        processingErrors
            .takeIf { it.size > 0 }
            ?.apply {
                outputDir.deleteRecursively()
                forEach { e ->
                    when {
                        e is pro.horovodovodo4ka.kodable.processor.Exception && e.element != null -> printError(e.localizedMessage, e.element)
                        else -> printError(e.localizedMessage)
                    }
                }
            }
        printMessage("Kodable generating finished")
        processed = true
        return false
    }

    // selector

    class ProcessorDesc(val processor: ProcessorType, val targetType: ClassName, val typeDekoderOrKoder: Element? = null, val typeEnkoder: Element? = null)

    private fun prefetchTypes(element: Element, annotationKind: AnnotationKind) {
        val clz = getClass(element) ?: throw Exception("@Dekoder, @Enkoder, @Koder annotations must be used with classes and constructors only")
        val meta = clz.kotlinMetadata as? KotlinClassMetadata ?: return
        val proto = meta.data.classProto

        val targetType = fixType(clz.asClassName())
        val processsor: ProcessorDesc = when {
            proto.classKind == ENUM_CLASS && annotationKind == KODER -> ProcessorDesc(ENUM_KODER, targetType, element)
            proto.classKind == Kind.CLASS -> when {
                proto.sealedSubclassFqNameCount > 0 -> throw Exception("Sealed classes are not supported: '$targetType'")
                proto.isDataClass -> if (annotationKind == KODER) ProcessorDesc(DATACLASS_KODER, targetType, element) else null
                proto.isInnerClass -> if (annotationKind == ENKODER) ProcessorDesc(
                    OBJECT_ENKODER,
                    getClass(element.enclosingElement)!!.asClassName(),
                    typeEnkoder = element
                ) else null
                annotationKind == DEKODER -> ProcessorDesc(OBJECT_DEKODER, targetType, typeDekoderOrKoder = element)
                else -> null
            }
            else -> null
        }
            ?: throw Exception("\nCheck annotation for type '$element' - it must be one of:\n@Koder - enums and data classes\n@Enkoder - inner classes for decoding nesting classes\n@Dekoder - usual classes")

        when (annotationKind) {
            DEKODER -> prefetchedDekoders.add(targetType)
            ENKODER -> prefetchedEnkoders.add(targetType)
            KODER -> {
                prefetchedDekoders.add(targetType)
                prefetchedEnkoders.add(targetType)
            }
        }

        prefetchedProcessors.add(processsor)
        proto.visibility?.also { typesVisibility[targetType] = it }
    }

    private fun List<ProcessorDesc>.mix(): ProcessorDesc = reduce { a, b ->
        if (a.targetType != b.targetType) throw Exception("Incompatible processors")
        return ProcessorDesc(a.processor, a.targetType, a.typeDekoderOrKoder ?: b.typeDekoderOrKoder, a.typeEnkoder ?: b.typeEnkoder)
    }

    private fun processPrefetchedTypes() {
        prefetchedProcessors
            .groupBy { it.targetType }
            .map { it.value.mix() }
            .forEach { desc ->
                printMessage("Kodable processing: ${desc.targetType}")
                when (desc.processor) {
                    ENUM_KODER -> generateEnumKoder(desc.targetType, desc.typeDekoderOrKoder!!)
                    DATACLASS_KODER -> generateDataClassKoder(desc.targetType, desc.typeDekoderOrKoder!!)
                    OBJECT_ENKODER, OBJECT_DEKODER -> generateObjectKoder(desc.targetType, desc.typeDekoderOrKoder, desc.typeEnkoder)
                }
            }
    }
    // defaults

    private fun registerDefaultKodable(element: Element) {
        if (element !is TypeElement) return

        element.qualifiedName

        val kodable = element.asClassName()
        val targetType = element.defaultKoder() ?: return
        val typeMeta: KotlinClassMetadata = element.kotlinMetadata as? KotlinClassMetadata ?: return

        if (typeMeta.data.classProto.classKind != OBJECT && typeMeta.data.classProto.classKind != COMPANION_OBJECT)
            throw Exception("Default kodable '$kodable' for type '$targetType' MUST be an object")

        element.interfaces
            .mapNotNull { it.asTypeName() as? ParameterizedTypeName }
            .firstOrNull { it.rawType == KODABLE_INTERFACE_TYPE && it.typeArguments.firstOrNull() == targetType }
            ?: throw Exception("Type $kodable does not implements IKodable<$targetType>")

        if (defaults[targetType] != null) throw Exception("Default Dekoder for '$element' already defined: ${defaults[targetType]}")

        defaults[targetType] = kodable
    }

    // objects

    class KoderMeta(val type: TypeElement, val properties: List<Element>, val typeMeta: KotlinClassMetadata, val propertiesMeta: List<ProtoBuf.Property>)

    private fun getDataClassAndProperties(element: Element?): KoderMeta? {
        val type: TypeElement = element as? TypeElement ?: return null
        val constructor = ElementFilter.constructorsIn(type.enclosedElements).firstOrNull() ?: return null
        val typeMeta: KotlinClassMetadata = type.kotlinMetadata as? KotlinClassMetadata ?: return null

        val (nameResolver, propertiesMeta) = typeMeta.getResolverAndMeta()

        val properties = constructor.parameters

        val propertiesMap = propertiesMeta.map { nameResolver.getString(it.name) to it }.toMap()

        val sortedProperties = properties.mapNotNull { executableProperty -> propertiesMap[executableProperty.toString()] }
        if (sortedProperties.isEmpty()) throw Exception("Inner class '$type' is marked by Enkoder annotation, but has no parameters with public getters")

        return KoderMeta(type, properties, typeMeta, sortedProperties)
    }

    class DekoderMeta(val type: TypeElement, val constructor: ExecutableElement, val typeMeta: KotlinClassMetadata, val constructorMeta: ProtoBuf.Constructor)

    private fun getClass(element: Element): TypeElement? {
        return when (element.kind) {
            ElementKind.CLASS, ElementKind.ENUM -> element as TypeElement
            CONSTRUCTOR -> element.enclosingElement as TypeElement
            else -> null
        }
    }

    private fun getClassAndCostructor(element: Element?): DekoderMeta? {
        element ?: return null
        val type: TypeElement
        val constructor: ExecutableElement

        when (element.kind) {
            ElementKind.CLASS -> {
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

        if (constructorMeta.valueParameterCount == 0) throw Exception("Class '$type' (or it's constructor) is marked by Dekoder annotation, but constructor has no parameters")

        return DekoderMeta(type, constructor, typeMeta, constructorMeta)
    }

    class EnkoderMeta(val type: TypeElement, val properties: List<Element>, val typeMeta: KotlinClassMetadata, val propertiesMeta: List<ProtoBuf.Property>)

    private fun getInnerClassAndProperties(element: Element?): EnkoderMeta? {
        element ?: return null

        val type = element as? TypeElement ?: return null
        val typeMeta: KotlinClassMetadata = type.kotlinMetadata as? KotlinClassMetadata ?: return null
        val (nameResolver, propertiesMeta) = typeMeta.getResolverAndMeta()
        val properties = ElementFilter.methodsIn(element.enclosedElements)

        val propertiesMap = propertiesMeta.map { "get${nameResolver.getString(it.name).capitalize()}()" to it }.toMap()

        val sortedProperties = properties.mapNotNull { executableProperty -> propertiesMap[executableProperty.toString()] }
        if (sortedProperties.isEmpty()) throw Exception("Inner class '$type' is marked by Enkoder annotation, but has no parameters with public getters")

        return EnkoderMeta(type, properties, typeMeta, sortedProperties)
    }

    open inner class DekoderProperty(val parameter: Element, val name: CharSequence) {
        private val types: List<Pair<String, ClassName>>

        open val jsonName = parameter.getAnnotationsByType(KodableName::class.java).firstOrNull()?.name ?: name
        val nullable = parameter.getAnnotationsByType(Nullable::class.java).isNotEmpty()
        val typeNesting get() = types.reversed().joinToString("") { it.first }

        val customKoder = parameter.customKoder()
        val propertyType get() = types.last().second
        val propertyTypeName: TypeName

        open val koderType
            get() = customKoder ?: defaults[propertyType]
            ?: prefetchedDekoders.firstOrNull { it == propertyType }?.let { propertyType.kodableName() }
            ?: propertyType.kodableName()
                .also { printMessage("!!! Dekoder for '$propertyType' is not defined and not generated, suppose it is external") }

        init {
            @Suppress("LeakingThis")
            val typeName = getPropertyType()
            types = mutableListOf<Pair<String, ClassName>>().also { unwrapType(typeName, it) }
            propertyTypeName = wrapType(types.first().second, *types.drop(1).map { it.second }.toTypedArray())
                .let {
                    if (nullable) it.asNullable()
                    else it
                }
        }

        private fun getPropertyType(): TypeName = when (parameter) {
            is ExecutableElement -> parameter.returnType.asTypeName()
            else -> parameter.asType().asTypeName()
        }

        private fun wrapType(outer: ClassName, vararg inner: ClassName): TypeName = when (outer) {
            MAP_TYPE -> ParameterizedTypeName.get(outer, STRING_TYPE, wrapType(inner.first(), *inner.drop(1).toTypedArray()))
            LIST_TYPE -> ParameterizedTypeName.get(outer, wrapType(inner.first(), *inner.drop(1).toTypedArray()))
            else -> outer
        }

        private fun unwrapType(type: TypeName, result: MutableList<Pair<String, ClassName>>) {
            when (type) {
                is ParameterizedTypeName -> {
                    when (val genericType = fixType(type.rawType)) {
                        LIST_TYPE -> {
                            result.add(".list" to genericType)
                            unwrapType(type.typeArguments.first(), result)
                        }
                        MAP_TYPE -> {
                            result.add(".dictionary" to genericType)
                            unwrapType(type.typeArguments[1], result)
                        }
                        else -> throw Exception("Only kotlin.collections.List<*> and kotlin.collections.Map<String, *> is allowed! $genericType found")
                    }
                }
                is WildcardTypeName -> {
                    val upperBounds = type.upperBounds.firstOrNull() ?: throw Exception("Only out variance is allowed for collection")
                    unwrapType(upperBounds, result)
                }
                is ClassName -> {
                    result.add("" to fixType(type))
                }
                else -> {
                }
            }
        }
    }

    inner class EnkoderProperty(parameter: Element, name: CharSequence) : DekoderProperty(parameter, name) {
        override val koderType
            get() = customKoder ?: defaults[propertyType]
            ?: prefetchedEnkoders
                .firstOrNull { it == propertyType }?.let { propertyType.kodableName() }
            ?: propertyType.kodableName()
                .also { printMessage("!!! Enkoder for '$propertyType' is not defined and not generated, suppose it is external") }
    }

    private fun fixType(type: ClassName): ClassName = when (type.toString()) {
        "java.lang.String" -> STRING_TYPE
        "java.lang.Double" -> DOUBLE_TYPE
        "java.lang.Short" -> SHORT_TYPE
        "java.lang.Byte" -> BYTE_TYPE
        "java.lang.Float" -> FLOAT_TYPE
        "java.lang.Boolean" -> BOOLEAN_TYPE
        "java.lang.Long" -> LONG_TYPE
        "java.lang.Integer" -> INT_TYPE
        "java.lang.Number" -> NUMBER_TYPE
        "java.util.List" -> LIST_TYPE
        "java.util.Map" -> MAP_TYPE
        else -> type
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
        if (proto.classKind != ENUM_CLASS) return null

        val nameResolver = meta.data.nameResolver

        val clz = element as TypeElement
        val default = clz.enclosedElements
            .firstOrNull { it.getAnnotationsByType(Default::class.java).isNotEmpty() }
            ?.let { field ->
                proto.enumEntryList.firstOrNull { field.toString() == nameResolver.getString(it.name) }
                    ?: throw Exception("Annotation @Default must be used only with enum value, not enum property")
            }

        return EnumMeta(element, meta, proto.enumEntryList, default)
    }

    private fun generateDataClassKoder(targetType: ClassName, element: Element): Boolean {
        val meta = getDataClassAndProperties(element) ?: return false
        val nameResolver = meta.typeMeta.data.nameResolver

        val targetKodableType = targetType.kodableName()
        TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(targetType.visibility)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
//            .addTypeProp(targetType)
            .apply {
                val dekoderParams = meta.run {
                    propertiesMeta.mapIndexed { idx, parameter -> DekoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                generateReader(targetType, dekoderParams)
            }
            .apply {
                val enkoderParams = meta.run {
                    propertiesMeta.mapIndexed { idx, parameter -> EnkoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                generateWriter(targetType, enkoderParams)
            }
            .build()
            .writeToFileWithImports(targetType, targetKodableType)

        return true
    }

    private fun generateEnumKoder(targetType: ClassName, element: Element): Boolean {
        val meta = getEnumClassAndDefault(element) ?: return false
        val nameResolver = meta.typeMeta.data.nameResolver

        val targetKodableType = targetType.kodableName()

        TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(targetType.visibility)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
//            .addTypeProp(targetType)
            .addFunction(
                FunSpec
                    .builder("readValue")
                    .addModifiers(OVERRIDE)
                    .addParameter("reader", READER_TYPE)
                    .returns(targetType)
                    .apply {
                        if (meta.defaultEntry != null)
                            addStatement(
                                "return try { %1T.valueOf(reader.readString()) } catch (_: Exception) { %1T.${nameResolver.getString(meta.defaultEntry.name)} }",
                                targetType
                            )
                        else
                            addStatement("return enumValueOf<%T>(reader.readString())", targetType)
                    }
                    .build()
            )
            .addFunction(
                FunSpec
                    .builder("writeValue")
                    .addModifiers(OVERRIDE)
                    .addParameter("writer", WRITER_TYPE)
                    .addParameter("instance", targetType)
                    .addStatement("return writer.writeString(instance.name)")
                    .build()
            )
            .build()
            .writeToFileWithImports(targetType, targetKodableType)

        return true
    }

    private fun generateObjectKoder(targetType: ClassName, dekoderElement: Element?, enkoderElement: Element?): Boolean {
        val dekoderMeta = getClassAndCostructor(dekoderElement)
        val enkoderMeta = getInnerClassAndProperties(enkoderElement)

        if (dekoderElement == null && enkoderElement == null) throw Exception("Undefined behaviour: didn't find Enkoder and Dekoder for type '$targetType'")

        val targetKodableType = targetType.kodableName()

        TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(targetType.visibility)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
//            .addTypeProp(targetType)
            .apply {
                dekoderMeta ?: return@apply

                val dekoderParams = dekoderMeta.run {
                    val nameResolver = typeMeta.data.nameResolver
                    constructorMeta.valueParameterList.mapIndexed { idx, parameter ->
                        DekoderProperty(
                            constructor.parameters[idx],
                            nameResolver.getName(parameter.name).asString()
                        )
                    }
                }

                generateReader(targetType, dekoderParams)
            }
            .apply {
                enkoderMeta ?: return@apply

                val enkoderParams = enkoderMeta.run {
                    val nameResolver = typeMeta.data.nameResolver
                    propertiesMeta.mapIndexed { idx, parameter -> EnkoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                generateWriter(targetType, enkoderParams, enkoderMeta)
            }
            .build()
            .writeToFileWithImports(targetType, targetKodableType)

        return true
    }

    private val outputDir: File by lazy { generatedDir!!.also { it.mkdir() } }

    private fun writeFile(fileSpec: FileSpec) {
        fileSpec.writeTo(outputDir)
    }

    private fun printMessage(message: String) {
        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, message)
    }

    private fun printError(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun printError(message: String, element: Element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    //region Utils

    private val TypeName.visibility: KModifier
        get() = when (typesVisibility[this]) {
            INTERNAL -> KModifier.INTERNAL
            PRIVATE, PRIVATE_TO_THIS, LOCAL -> KModifier.PRIVATE
            PROTECTED -> KModifier.PROTECTED
            else -> PUBLIC
        }

    private fun extFunSpec(type: TypeName, kodableType: ClassName): FunSpec {
        val kclassType = ParameterizedTypeName.get(KCLASS_TYPE, type)
        val kodable = ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, type)

        return FunSpec
            .builder("kodable")
            .addModifiers(type.visibility)
            .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember("\"${kodableType.simpleName()}_kodable\"").build())
            .receiver(kclassType)
            .returns(kodable)
            .addStatement("return " + kodableType.simpleName())
            .build()
    }

    private fun TypeSpec.writeToFileWithImports(targetType: ClassName, targetKodableType: ClassName) {
        val file = FileSpec
            .builder(targetKodableType.packageName(), targetKodableType.simpleName())
            .addStaticImport("$packageName.core", "utils.propertyAssert", "readValueOrNull", "writeValueOrNull")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName(), "objectProperty")
            .addType(this)
            .addFunction(extFunSpec(targetType, targetKodableType))
            .build()

        writeFile(file)
    }

    private fun TypeSpec.Builder.addTypeProp(targetType: ClassName) = addProperty(
        PropertySpec
            .builder("type", ParameterizedTypeName.get(KClass::class.asTypeName(), targetType))
            .addModifiers(OVERRIDE)
            .initializer("%T::class", targetType)
            .build()
    )

    private fun TypeSpec.Builder.generateReader(targetType: ClassName, dekoderParams: List<DekoderProperty>) = addFunction(
        FunSpec
            .builder("readValue")
            .addModifiers(OVERRIDE)
            .addParameter("reader", READER_TYPE)
            .returns(targetType)
            .apply {
                dekoderParams.forEach {
                    addStatement("var %N: %T = null", it.name, it.propertyTypeName.asNullable())
                }

                beginControlFlow("reader.iterateObject")
                beginControlFlow("when (it)")

                dekoderParams.forEach {
                    addStatement(""""${it.jsonName}" -> %N = %T${it.typeNesting}.readValueOrNull(reader) """, it.name, it.koderType)
                }

                addStatement("else -> reader.skipValue()")
                endControlFlow()
                endControlFlow()

                dekoderParams.filter { !it.nullable }.forEach {
                    addStatement("""propertyAssert(%N, "${it.name}", "$targetType")""", it.name)
                }

                addStatement("return %T(%>", targetType)
                dekoderParams.forEachIndexed { idx, it ->
                    val forceNonNull = if (it.nullable) "" else "!!"
                    val div = if (idx < dekoderParams.size - 1) ", " else ""
                    addStatement("%1N = %1N$forceNonNull$div", it.name)
                }
                addStatement("%<)")
            }
            .build()
    )

    private fun TypeSpec.Builder.generateWriter(targetType: ClassName, enkoderParams: List<EnkoderProperty>, context: EnkoderMeta? = null) = addFunction(
        FunSpec
            .builder("writeValue")
            .addModifiers(OVERRIDE)
            .addParameter("writer", WRITER_TYPE)
            .addParameter("instance", targetType)
            .apply {
                val contextWrapper = context?.let { "{ ${context.type.asClassName().simpleName()}() }.apply" } ?: ""
                beginControlFlow("with(instance) $contextWrapper")
                addStatement("""val properties = sequenceOf(""")
                enkoderParams.forEachIndexed { index, prop ->
                    val propertyStatement = if (prop.nullable) "writeValueOrNull" else "writeValue"
                    val separator = if (index < enkoderParams.lastIndex) "," else ""
                    addStatement("\tobjectProperty(\"${prop.jsonName}\") { %T${prop.typeNesting}.$propertyStatement(this, %N) }$separator", prop.koderType, prop.name)
                }
                addStatement(""")""")
                addStatement("writer.iterateObject(properties)")
                endControlFlow()
            }
            .build()
    )

    //endregion
}

private fun KotlinClassMetadata.getResolverAndMeta(): Pair<NameResolver, List<ProtoBuf.Property>> {
    val nameResolver = data.nameResolver
    val propertiesMeta = data.classProto.propertyList
        .filter { it.hasGetter && (it.getterVisibility == Visibility.PUBLIC || it.getterVisibility == Visibility.INTERNAL) }
    return nameResolver to propertiesMeta
}

private fun ClassName.kodableName(): ClassName {
    val fullName = simpleNames().joinToString("")
    return ClassName(packageName(), "${fullName}Kodable")
}

private fun Element.defaultKoder() = annotationValue<DeclaredType>(DefaultKodableForType::class)?.asTypeName()
private fun Element.customKoder() = annotationValue<DeclaredType>(CustomKodable::class)?.asTypeName()

inline fun <reified T> Element.annotationValue(annotation: KClass<out Annotation>, paramIndex: Int = 0): T? =
    annotationMirrors.firstOrNull { it.annotationType.asTypeName() == annotation.asTypeName() }?.elementValues?.values?.toList()?.getOrNull(paramIndex)?.value as? T