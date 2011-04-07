; Copyright © 2011 Sattvik Software & Technology Resources, Ltd. Co.
; All rights reserved.
;
; This program and the accompanying materials are made available under the
; terms of the Eclipse Public License v1.0 which accompanies this distribution,
; and is available at <http://www.eclipse.org/legal/epl-v10.html>.
;
; By using this software in any fashion, you are agreeing to be bound by the
; terms of this license.  You must not remove this notice, or any other, from
; this software.

(ns neko.dialog.alert
  "Helps build and manage alert dialogs.  The core functionality of this
  namespace is built around the AlertDialogBuilder protocol.  This allows using
  the protocol with the FunctionalAlertDialogBuilder generated by new-builder
  as well as the AlertDialog.Builder class provided by the Android platform.

  In general, it is preferable to use the functional version of the builder as
  it is immutable.  Using the protocol with an AlertDialog.Builder object works
  by mutating the object."
  {:author "Daniel Solano Gómez"}
  (:import android.app.AlertDialog$Builder)
  (:use neko.context)
  )

(defprotocol AlertDialogBuilder
  "Defines the functionality needed to build new alert dialogues."
  (create [builder]
    "Actually creates the AlertDialog.")
  (with-cancellation [builder cancellable?]
    "Sets whether or not the dialog may be cancelled.")
  )

(defrecord FunctionalAlertDialogBuilder
  [^android.content.Context context
   ^boolean cancellable])

(defn- new-builder?
  "Predicate used for testing whether a new builder is a functional builder but
  is different from the original builder."
  [old-builder new-builder]
  (and (instance? FunctionalAlertDialogBuilder old-builder)
       (not (identical? old-builder new-builder))))

(extend-type FunctionalAlertDialogBuilder
  AlertDialogBuilder
  (create [this]
    {:post [(instance? android.app.AlertDialog %)]}
    (let [^AlertDialog$Builder builder (AlertDialog$Builder. (.context this))]
      (doto builder
        (.setCancelable (.cancellable this))
        )
      (.create builder)))

  (with-cancellation [this cancellable?]
    {:post [(new-builder? this %)
            (= (:cancellable %) cancellable?)]}
    (assoc this :cancellable (boolean cancellable?)))
  )

(extend-type AlertDialog$Builder
  AlertDialogBuilder
  (create [this]
    {:post [(instance? android.app.AlertDialog %)]}
    (.create this))

  (with-cancellation [this cancellable?]
    {:post [(identical? this %)]}
    (.setCancelable this (boolean cancellable?)))
  )

(defn new-builder
  "Creates a new functional alert dialog builder.  If within a with-context
  form, the context argument may be omitted."
  ([]
   {:pre  [(has-*context*?)]
    :post [(instance? FunctionalAlertDialogBuilder %)]}
   (new-builder *context*))
  ([context]
   {:pre  [(context? context)]
    :post [(instance? FunctionalAlertDialogBuilder %)]}
   (FunctionalAlertDialogBuilder. context true)))
