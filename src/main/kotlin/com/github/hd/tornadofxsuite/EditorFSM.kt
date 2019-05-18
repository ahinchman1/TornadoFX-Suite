package com.github.hd.tornadofxsuite

/**
 * Experimental example FSM for smarter testing
 */

sealed class EditorFragment {
    var modelState = ModelState.NotDirty

    fun commitModel() {}

    open class OwnerTextField: EditorFragment() {
        fun onChangeText() {
            modelState.initModelStateTransition()
        }
    }

    open class CatTextField: EditorFragment() {
        fun onChangeText() {
            modelState.initModelStateTransition()
        }
    }

    open class TimeTextField: EditorFragment() {
        fun onChangeText() {
            modelState.initModelStateTransition()
        }
    }

    class SaveButton(var toggle: Toggle = Toggle.Disable): EditorFragment() {
        fun toggleButtonOnDirtyModel(model: ModelState) {
            toggle.enableOnDirty(model)
        }

        fun saveModel() {
            if (toggle.isButtonEnabled()) {
                commitModel()
            }
        }
    }
}

enum class Toggle {
    Enable, Disable;

    fun isButtonEnabled(): Boolean {
        return when (this) {
            Enable -> true
            Disable -> false
        }
    }

    fun enableOnDirty(model: ModelState): Toggle {
        return when (model) {
            ModelState.Dirty -> Enable
            ModelState.NotDirty -> Disable
        }
    }
}

enum class ModelState {
    Dirty, NotDirty;

    fun initModelStateTransition(): ModelState {
        return when (this) {
            Dirty -> NotDirty
            NotDirty -> Dirty
        }
    }
}