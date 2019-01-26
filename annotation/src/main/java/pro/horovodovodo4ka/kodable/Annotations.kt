package pro.horovodovodo4ka.kodable

import pro.horovodovodo4ka.kodable.core.Kodable
import kotlin.reflect.KClass

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ObjectDekoder

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ListDekoder

@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SingleValueDekoder

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class JsonName(val jsonName: String)

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.SOURCE)
annotation class JsonKoder(val decoderClass: KClass<out Kodable<*>>)

@Target(AnnotationTarget.FIELD)
//@Retention(AnnotationRetention.SOURCE)
annotation class Default