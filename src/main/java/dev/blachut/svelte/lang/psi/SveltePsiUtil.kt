package dev.blachut.svelte.lang.psi

internal fun isSingleDollarPrefixedName(name: String?): Boolean {
  return name != null && name.length > 1 && name[0] == '$' && name[1] != '$'
}

internal fun removeSingleDollarPrefixUnchecked(name: String): String {
  return name.substring(1)
}