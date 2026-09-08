import { mount } from 'svelte'
import App from './App.svelte'

const props = {
  onSelect: () => {}
}

mount(App, {
  target: document.body,
  props
})
