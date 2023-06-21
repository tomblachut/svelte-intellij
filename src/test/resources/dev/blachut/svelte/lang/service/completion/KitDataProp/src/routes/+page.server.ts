import type {PageServerLoadEvent} from './$types';

export async function load(event: PageServerLoadEvent) {
  return {
    somePost: {innerField: "Foo"},
    uniqueName: {innerField: "Bar"}
  };
}
