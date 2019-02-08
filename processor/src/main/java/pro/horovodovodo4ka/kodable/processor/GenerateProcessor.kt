package pro.horovodovodo4ka.kodable.processor

import com.github.fluidsonic.fluid.json.JSONReader
import com.github.fluidsonic.fluid.json.JSONWriter
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
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.DEKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.ENKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.AnnotationKind.KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.DATACLASS_KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.ENUM_KODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.OBJECT_DEKODER
import pro.horovodovodo4ka.kodable.processor.GenerateProcessor.ProcessorType.OBJECT_ENKODER
import pro.horovodovodo4ka.kodable.processor.tools.KotlinAbstractProcessor
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
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
        val KCLASS_TYPE = KClass::class.asClassName()
        val KODABLE_INTERFACE_TYPE = IKodable::class.asClassName()
        val READER_TYPE = JSONReader::class.asClassName()
        val WRITER_TYPE = JSONWriter::class.asClassName()
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

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (processed) return false
        roundEnv.getElementsAnnotatedWith(DefaultKodableForType::class.java).forEach(::registerDefaultKodable)
        roundEnv.getElementsAnnotatedWith(Dekoder::class.java).forEach { prefetchTypes(it, DEKODER) }
        roundEnv.getElementsAnnotatedWith(Enkoder::class.java).forEach { prefetchTypes(it, ENKODER) }
        roundEnv.getElementsAnnotatedWith(Koder::class.java).forEach { prefetchTypes(it, KODER) }
        processPrefetchedTypes()
        processed = true
        return false
    }

    // selector

    class ProcessorDesc(val processor: ProcessorType, val targetType: ClassName, val typeDekoderOrKoder: Element? = null, val typeEnkoder: Element? = null)

    private val prefetchedDekoders = mutableListOf<TypeName>()
    private val prefetchedEnkoders = mutableListOf<TypeName>()
    private val prefetchedProcessors = mutableListOf<ProcessorDesc>()
    private fun prefetchTypes(element: Element, annotationKind: AnnotationKind) {
        val clz = getClass(element) ?: throw Exception("@Dekoder, @Enkoder, @Koder annotations must be used with classes and constructors only")
        val meta = clz.kotlinMetadata as? KotlinClassMetadata ?: return
        val proto = meta.data.classProto

        val targetType = fixType(clz.asClassName())

        val processsor: ProcessorDesc = when {
            proto.classKind == Kind.ENUM_CLASS && annotationKind == KODER -> ProcessorDesc(ENUM_KODER, targetType, element)
            proto.classKind == Kind.CLASS -> when {
                proto.sealedSubclassFqNameCount > 0 -> throw Exception("Sealed classes is not supported: '$targetType'")
                proto.isDataClass -> if (annotationKind == KODER) ProcessorDesc(DATACLASS_KODER, targetType, element) else null
                proto.isInnerClass -> if (annotationKind == ENKODER) ProcessorDesc(OBJECT_ENKODER, getClass(element.enclosingElement)!!.asClassName(), typeEnkoder = element) else null
                annotationKind == DEKODER -> ProcessorDesc(OBJECT_DEKODER, targetType, typeDekoderOrKoder = element)
                else -> null
            }
            else -> null
        } ?: throw Exception("\nCheck annotation for type '$element' - it must be one of:\n@Koder - enums and data classes\n@Enkoder - inner classes for decoding nesting classes\n@Dekoder - usual classes")

        when (annotationKind) {
            DEKODER -> prefetchedDekoders.add(targetType)
            ENKODER -> prefetchedEnkoders.add(targetType)
            KODER -> {
                prefetchedDekoders.add(targetType)
                prefetchedEnkoders.add(targetType)
            }
        }

        prefetchedProcessors.add(processsor)
    }

    private fun List<ProcessorDesc>.mix(): ProcessorDesc = reduce { a, b ->
        if (a.targetType != b.targetType) throw Exception("Incompatible processors")
        return ProcessorDesc(a.processor, a.targetType, a.typeDekoderOrKoder ?: b.typeDekoderOrKoder, a.typeEnkoder ?: b.typeEnkoder)
    }

    private fun processPrefetchedTypes() {
        prefetchedProcessors.groupBy { it.targetType }.map {
            val desc = it.value.mix()
            printWarning("Kodable processing: ${desc.targetType}")
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

        val kodable = element.asClassName()
        val targetType = element.defaultKoder() ?: return

        element.interfaces
            .mapNotNull { it.asTypeName() as? ParameterizedTypeName }
            .firstOrNull { it.rawType == KODABLE_INTERFACE_TYPE && it.typeArguments.firstOrNull() == targetType } ?: throw Exception("Type $kodable does not implements IKodable<$targetType>")

        if (defaults[targetType] != null) throw Exception("Default Dekoder for '$element' already defined: ${defaults[targetType]}")

        defaults[targetType] = kodable
    }

    // objects

    class KoderMeta(val type: TypeElement, val properties: List<Element>, val typeMeta: KotlinClassMetadata, val propertiesMeta: List<ProtoBuf.Property>)

    private fun getDataClassAndProperties(element: Element?): KoderMeta? {
        val type: TypeElement = element as? TypeElement ?: return null
        val constructor = ElementFilter.constructorsIn(type.enclosedElements).firstOrNull() ?: return null
        val typeMeta: KotlinClassMetadata = type.kotlinMetadata as? KotlinClassMetadata ?: return null

        val nameResolver = typeMeta.data.nameResolver
        val propertiesMeta = typeMeta.data.classProto.propertyList
            .filter { it.hasGetter && (it.getterVisibility == Visibility.PUBLIC || it.getterVisibility == Visibility.INTERNAL) }
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
            ElementKind.CONSTRUCTOR -> element.enclosingElement as TypeElement
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
            ElementKind.CONSTRUCTOR -> {
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
        val nameResolver = typeMeta.data.nameResolver
        val propertiesMeta = typeMeta.data.classProto.propertyList
            .filter { it.hasGetter && (it.getterVisibility == Visibility.PUBLIC || it.getterVisibility == Visibility.INTERNAL) }
        val properties = ElementFilter.methodsIn(element.enclosedElements)

        val propertiesMap = propertiesMeta.map { "get${nameResolver.getString(it.name).capitalize()}()" to it }.toMap()

        val sortedProperties = properties.mapNotNull { executableProperty -> propertiesMap[executableProperty.toString()] }
        if (sortedProperties.isEmpty()) throw Exception("Inner class '$type' is marked by Enkoder annotation, but has no parameters with public getters")

        return EnkoderMeta(type, properties, typeMeta, sortedProperties)
    }

    open inner class DekoderProperty(val parameter: Element, val name: CharSequence) {
        private val types: List<ClassName>

        open val jsonName = parameter.getAnnotationsByType(KodableName::class.java).firstOrNull()?.name ?: name
        val nullable = parameter.getAnnotationsByType(Nullable::class.java).isNotEmpty()
        val listNestingCount get() = types.size - 1

        val customKoder = parameter.customKoder()
        val propertyType get() = types.last()
        val propertyTypeName: TypeName

        open val koderType
            get() = customKoder ?: defaults[propertyType]
            ?: prefetchedDekoders.firstOrNull { it == propertyType }?.let { propertyType.kodableName() }
            ?: throw Exception("Dekoder for '$propertyType' is not defined and not generated")

        init {
            @Suppress("LeakingThis")
            val typeName = getPropertyType()
            types = mutableListOf<ClassName>().also { unwrapType(typeName, it) }
            propertyTypeName = wrapType(types.first(), *types.drop(1).toTypedArray())
                .let {
                    if (nullable) it.asNullable()
                    else it
                }
        }

        private fun getPropertyType(): TypeName = when (parameter) {
            is ExecutableElement -> parameter.returnType.asTypeName()
            else -> parameter.asType().asTypeName()
        }

        private fun wrapType(outer: ClassName, vararg inner: ClassName): TypeName {
            return when (outer) {
                LIST_TYPE -> ParameterizedTypeName.get(outer, wrapType(inner.first(), *inner.drop(1).toTypedArray()))
                else -> outer
            }
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
    }

    inner class EnkoderProperty(parameter: Element, name: CharSequence) : DekoderProperty(parameter, name) {
        override val koderType: TypeName
            get() = customKoder ?: defaults[propertyType]
            ?: prefetchedEnkoders.firstOrNull { it == propertyType }?.let { propertyType.kodableName() }
            ?: throw Exception("Enkoder for '$propertyType' is not defined and not generated")
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
        if (proto.classKind != ProtoBuf.Class.Kind.ENUM_CLASS) return null

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
        val newType = TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
            .apply {
                val dekoderParams = meta.run {
                    propertiesMeta.mapIndexed { idx, parameter -> DekoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                addFunction(
                    FunSpec
                        .builder("readValue")
                        .addModifiers(OVERRIDE)
                        .addParameter("reader", READER_TYPE)
                        .returns(targetType)
                        .apply {
                            dekoderParams.forEach {
                                addStatement("var %N: %T = null", it.name, it.propertyTypeName.asNullable())
                            }

                            beginControlFlow("reader.readFromMapByElementValue")
                            beginControlFlow("when (it)")

                            dekoderParams.forEach {
                                val lists = 0.until(it.listNestingCount).joinToString("") { ".list" }
                                addStatement(""""${it.jsonName}" -> %N = %T$lists.readValueOrNull(reader) """, it.name, it.koderType)
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
            }
            .apply {
                val enkoderParams = meta.run {
                    propertiesMeta.mapIndexed { idx, parameter -> EnkoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                addFunction(
                    FunSpec
                        .builder("writeValue")
                        .addModifiers(OVERRIDE)
                        .addParameter("writer", WRITER_TYPE)
                        .addParameter("instance", targetType)
                        .apply {
                            beginControlFlow("with(instance)")
                            beginControlFlow("writer.writeIntoMap")
                            enkoderParams.forEach {
                                beginControlFlow("""writeMapElement("${it.jsonName}")""")
                                val lists = 0.until(it.listNestingCount).joinToString("") { ".list" }
                                if (it.nullable) {
                                    addStatement("""%T$lists.writeValueOrNull(this, %N)""", it.koderType, it.name)
                                } else {
                                    addStatement("""%T$lists.writeValue(this, %N)""", it.koderType, it.name)
                                }
                                endControlFlow()
                            }
                            endControlFlow()
                            endControlFlow()
                        }
                        .build()
                )
            }
            .build()

        val file = FileSpec
            .builder(targetKodableType.packageName(), targetKodableType.simpleName())
            .addStaticImport("$packageName.core", "utils.propertyAssert")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName(), "readFromMapByElementValue", "writeIntoMap", "writeMapElement")
            .addType(newType)
            .addFunction(extFunSpec(targetType, targetKodableType))
            .build()

        val output = generatedDir!!
        output.mkdir()

        file.writeTo(output)

        return true
    }

    private fun generateEnumKoder(targetType: ClassName, element: Element): Boolean {
        val meta = getEnumClassAndDefault(element) ?: return false
        val nameResolver = meta.typeMeta.data.nameResolver

        val targetKodableType = targetType.kodableName()

        val newType = TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
            .addFunction(
                FunSpec
                    .builder("readValue")
                    .addModifiers(OVERRIDE)
                    .addParameter("reader", READER_TYPE)
                    .returns(targetType)
                    .apply {
                        if (meta.defaultEntry != null)
                            addStatement("return try { %1T.valueOf(reader.readString()) } catch (_: Exception) { %1T.${nameResolver.getString(meta.defaultEntry.name)} }", targetType)
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

        val file = FileSpec
            .builder(targetKodableType.packageName(), targetKodableType.simpleName())
            .addStaticImport("$packageName.core", "defaults.kodable")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName())
            .addType(newType)
            .addFunction(extFunSpec(targetType, targetKodableType))
            .build()

        val output = generatedDir!!
        output.mkdir()

        file.writeTo(output)

        return true
    }

    private fun generateObjectKoder(targetType: ClassName, dekoderElement: Element?, enkoderElement: Element?): Boolean {
        val dekoderMeta = getClassAndCostructor(dekoderElement)
        val enkoderMeta = getInnerClassAndProperties(enkoderElement)

        if (dekoderElement == null && enkoderElement == null) throw Exception("Undefined behaviour: didn't find Enkoder and Dekoder for type '$targetType'")

        val targetKodableType = targetType.kodableName()

        val newType = TypeSpec
            .objectBuilder(targetKodableType)
            .addModifiers(PUBLIC)
            .addSuperinterface(ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, targetType))
            .apply {
                dekoderMeta ?: return@apply

                val dekoderParams = dekoderMeta.run {
                    val nameResolver = typeMeta.data.nameResolver
                    constructorMeta.valueParameterList.mapIndexed { idx, parameter -> DekoderProperty(constructor.parameters[idx], nameResolver.getName(parameter.name).asString()) }
                }

                addFunction(
                    FunSpec
                        .builder("readValue")
                        .addModifiers(OVERRIDE)
                        .addParameter("reader", READER_TYPE)
                        .returns(targetType)
                        .apply {
                            dekoderParams.forEach {
                                addStatement("var %N: %T = null", it.name, it.propertyTypeName.asNullable())
                            }

                            beginControlFlow("reader.readFromMapByElementValue")
                            beginControlFlow("when (it)")

                            dekoderParams.forEach {
                                val lists = 0.until(it.listNestingCount).joinToString("") { ".list" }
                                addStatement(""""${it.jsonName}" -> %N = %T$lists.readValueOrNull(reader) """, it.name, it.koderType)
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
            }
            .apply {
                enkoderMeta ?: return@apply

                val enkoderParams = enkoderMeta.run {
                    val nameResolver = typeMeta.data.nameResolver
                    propertiesMeta.mapIndexed { idx, parameter -> EnkoderProperty(properties[idx], nameResolver.getName(parameter.name).asString()) }
                }

                addFunction(
                    FunSpec
                        .builder("writeValue")
                        .addModifiers(OVERRIDE)
                        .addParameter("writer", WRITER_TYPE)
                        .addParameter("instance", targetType)
                        .apply {
                            beginControlFlow("with(instance) { ${enkoderMeta.type.asClassName().simpleName()}() }.apply")
                            beginControlFlow("writer.writeIntoMap")
                            enkoderParams.forEach {
                                beginControlFlow("""writeMapElement("${it.jsonName}")""")
                                val lists = 0.until(it.listNestingCount).joinToString("") { ".list" }
                                if (it.nullable) {
                                    addStatement("""%T$lists.writeValueOrNull(this, %N)""", it.koderType, it.name)
                                } else {
                                    addStatement("""%T$lists.writeValue(this, %N)""", it.koderType, it.name)
                                }
                                endControlFlow()
                            }
                            endControlFlow()
                            endControlFlow()
                        }
                        .build()
                )
            }
            .build()

        val file = FileSpec
            .builder(targetKodableType.packageName(), targetKodableType.simpleName())
            .addStaticImport("$packageName.core", "utils.propertyAssert")
            .addStaticImport(READER_TYPE.packageName(), READER_TYPE.simpleName(), "readFromMapByElementValue", "writeIntoMap", "writeMapElement")
            .addType(newType)
            .addFunction(extFunSpec(targetType, targetKodableType))
            .build()

        val output = generatedDir!!
        output.mkdir()

        file.writeTo(output)

        return true
    }

    private fun extFunSpec(type: TypeName, kodableType: ClassName): FunSpec {
        val kclassType = ParameterizedTypeName.get(KCLASS_TYPE, type)
        val kodable = ParameterizedTypeName.get(KODABLE_INTERFACE_TYPE, type)

        return FunSpec
            .builder("kodable")
            .addAnnotation(AnnotationSpec.builder(JvmName::class).addMember("\"${kodableType.simpleName()}_kodable\"").build())
            .receiver(kclassType)
            .returns(kodable)
            .addStatement("return " + kodableType.simpleName())
            .build()
    }


    private fun printInfo(message: String) {
        messager.printMessage(Diagnostic.Kind.OTHER, message)
    }

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

private fun Element.defaultKoder() = annotationValue<DeclaredType>(DefaultKodableForType::class)?.asTypeName()
private fun Element.customKoder() = annotationValue<DeclaredType>(CustomKodable::class)?.asTypeName()

inline fun <reified T> Element.annotationValue(annotation: KClass<out Annotation>, paramIndex: Int = 0): T? =
    annotationMirrors.firstOrNull { it.annotationType.asTypeName() == annotation.asTypeName() }?.elementValues?.values?.toList()?.getOrNull(paramIndex)?.value as? T