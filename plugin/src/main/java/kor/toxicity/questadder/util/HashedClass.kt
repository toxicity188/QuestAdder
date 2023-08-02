package kor.toxicity.questadder.util

class HashedClass(val clazz: Class<*>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashedClass

        return other.clazz.name == clazz.name
    }

    override fun hashCode(): Int {
        return clazz.name.hashCode()
    }
}