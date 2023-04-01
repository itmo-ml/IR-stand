package ru.itmo.stand.util

import org.h2.mvstore.MVStore
import java.io.File

fun <K, V> diskBasedMap(filePath: String): MutableMap<K, V> {
    val store = MVStore.open(File(filePath).createPath().absolutePath)
    return store.openMap("disk_based_map")
}

fun <K, V> diskBasedMapWithEnsuredPersist(filePath: String, action: (MutableMap<K, V>) -> Unit) {
    val store = MVStore.open(File(filePath).createPath().absolutePath)
    val diskBasedMap = store.openMap<K, V>("disk_based_map")
    action(diskBasedMap)
    store.close()
}
