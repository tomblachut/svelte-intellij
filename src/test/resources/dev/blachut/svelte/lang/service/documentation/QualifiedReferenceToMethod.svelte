<script lang="ts">
  /**
   * Yes, umber is a word and it's a color!
   */
  class Umber {
    /**
     * Returns a string representing an umber in CSS hex notation.
     * @param _digits Number of digits after the hash. Must be in the range 6-6, inclusive.
     */
    toHex(_digits?: number): string {
      return "#635147";
    }
  }

  let local = new Umber();
</script>

{local.<caret>toHex()}