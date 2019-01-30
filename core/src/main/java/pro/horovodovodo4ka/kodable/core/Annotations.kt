package pro.horovodovodo4ka.kodable.core

import kotlin.reflect.KClass

/**
 * If class annotated then first constructor in it used for **decoding** type from json.
 * If constructor annotated then it used for **decoding** type from json.
 * Parameters from constructor are used to declare json properties to decode, also their types are used for decoding.
 */
@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Dekoder

/**
 * Mark class used for describing json **encoding**
 * Properties' names are used as Json object properties names.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Enkoder

/**
 * Used for generating kodables for enum classes and data classes
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Koder

/**
 * Specifies key in json for current parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class KodableName(val name: String)

/**
 * Specifies custom Dekoder for parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomKodable(val kodable: KClass<out IKodable<*>>)

/**
 * Mark enum's value as default for decoding if unrecognized value found
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Default

/**
 * Mark class as Dekoder implementation for specified type.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultKodableForType(val type: KClass<*>)