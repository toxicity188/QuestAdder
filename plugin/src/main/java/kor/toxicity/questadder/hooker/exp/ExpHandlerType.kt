package kor.toxicity.questadder.hooker.exp

import kor.toxicity.questadder.api.exp.ExpHandler

enum class ExpHandlerType {
    DEFAULT {
        override fun supply(): ExpHandler {
            return DefaultExpHandler()
        }
    },
    MMOCORE {
        override fun supply(): ExpHandler {
            return MMOCoreExpHandler()
        }
    }
    ;

    abstract fun supply(): ExpHandler
}