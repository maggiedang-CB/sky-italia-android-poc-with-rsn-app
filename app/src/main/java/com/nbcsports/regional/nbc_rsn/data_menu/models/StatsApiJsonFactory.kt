package com.nbcsports.regional.nbc_rsn.data_menu.models

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.nbcsports.regional.nbc_rsn.data_bar.StatsTeam

class StatsApiJsonFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        val delegate = gson.getDelegateAdapter(this, type)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        when {
            type.rawType == StatsEventSplit::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        val jsonObj = jsonElement.asJsonObject
                        if (jsonObj.get("teamStats").isJsonObject) {
                            // we need to modify it into an array
                            val arrayWrapper = JsonArray()
                            arrayWrapper.add(jsonObj.get("teamStats"))
                            jsonObj.remove("teamStats")
                            jsonObj.add("teamStats", arrayWrapper)
                        }
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == StatsLeague::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        val jsonObj = jsonElement.asJsonObject
                        if (jsonObj.get("season")?.isJsonObject == true) {
                            // we need to modify it into an array
                            val arrayWrapper = JsonArray()
                            arrayWrapper.add(jsonObj.get("season"))
                            jsonObj.remove("season")
                            jsonObj.add("seasons", arrayWrapper)
                        }
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == BoxScoreNBA::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        removeEmptyArray(jsonElement.asJsonObject, "playerStats")
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == BoxScoreMLB::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        removeEmptyArray(jsonElement.asJsonObject, "playerBattingStats")
                        removeEmptyArray(jsonElement.asJsonObject, "playerPitchingStats")
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == BoxScoreNHL::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        removeEmptyArray(jsonElement.asJsonObject, "playerSkaterStats")
                        removeEmptyArray(jsonElement.asJsonObject, "playerGoaltenderStats")
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == PlayerStatsNFL::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        removeEmptyArray(jsonElement.asJsonObject, "passingStats")
                        removeEmptyArray(jsonElement.asJsonObject, "rushingStats")
                        removeEmptyArray(jsonElement.asJsonObject, "receivingStats")
                        removeEmptyArray(jsonElement.asJsonObject, "defenseStats")
                        removeEmptyArray(jsonElement.asJsonObject, "kickReturnStats")
                        removeEmptyArray(jsonElement.asJsonObject, "puntReturnStats")
                        removeEmptyArray(jsonElement.asJsonObject, "fieldGoalStats")
                        removeEmptyArray(jsonElement.asJsonObject, "patStats")
                        removeEmptyArray(jsonElement.asJsonObject, "puntingStats")
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            type.rawType == StatsTeam::class.java -> return object : TypeAdapter<T>() {
                override fun read(reader: JsonReader?): T {
                    val jsonElement = elementAdapter.read(reader)
                    if (jsonElement.isJsonObject) {
                        removeEmptyObject(jsonElement.asJsonObject, "record")
                    }
                    return delegate.fromJsonTree(jsonElement)
                }

                override fun write(writer: JsonWriter?, value: T?) {
                    delegate.write(writer, value)
                }
            }
            else -> return delegate
        }
    }

    private fun removeEmptyObject(target: JsonObject, key: String) {
        if (target.get(key)?.isJsonObject == true) {
            if (target.get(key).asJsonObject.entrySet().isEmpty()) {
                // we remove this property so the default value can be kept
                target.remove(key)
            }
        }
    }

    private fun removeEmptyArray(target: JsonObject, key: String) {
        if (target.get(key)?.isJsonArray == true) {
            if (target.get(key).asJsonArray.size() == 0) {
                // we remove this property so the default value can be kept
                target.remove(key)
            }
        }
    }

}