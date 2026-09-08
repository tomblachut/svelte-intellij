class Store {
  options: unknown

  constructor(options: unknown) {
    this.options = options
  }
}

new Store({
  props: {
    <warning descr="Unused property onSelect">onSelect</warning>: () => {}
  }
})
