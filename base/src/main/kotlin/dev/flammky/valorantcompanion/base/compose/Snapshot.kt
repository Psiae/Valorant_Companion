package dev.flammky.valorantcompanion.base.compose


@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
)
annotation class SnapshotRead {

}