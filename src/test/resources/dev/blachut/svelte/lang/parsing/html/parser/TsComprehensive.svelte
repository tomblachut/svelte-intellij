<script lang="ts">
  interface Item {
    id: number;
    name: string;
    active?: boolean;
  }

  interface A {
    a: string;
  }

  interface B {
    b: number;
  }

  interface TypeShape {
    tag: string;
  }

  interface ActionArg {
    enabled: boolean;
  }

  type T = string;

  const value: unknown = "value";
  const maybeString: string | null = "text";
  const maybeItem: Item | null = { id: 1, name: "first", active: true };
  const obj: { prop?: string } | null = { prop: "prop" };
  const fallback = "fallback";
  const aAndB = { a: "a", b: 1 } as A & B;
  const items: Item[] = [{ id: 1, name: "first" }];
  const maybeItems: Item[] | null = items;
  const mapUnknown: unknown = new Map<string, number>();
  const tupleUnknown: unknown = ["name", 1];
  const conditionalUnknown: unknown = "x";
  const readonlyUnknown: unknown = ["a", "b"];
  const maybeProps: Record<string, unknown> | null = { title: "hello", hidden: false };
  const nestedProps: { attrs?: Record<string, unknown> } | null = { attrs: { role: "button" } };
  const promiseValue: Promise<Item> = Promise.resolve(items[0]);
  const something = { key: "id" } as const;

  let inputValue = "";

  const action = (node: HTMLElement, param?: unknown) => {
    void node;
    void param;
    return {
      destroy() {}
    };
  };
</script>

<!-- === CONTENT EXPRESSIONS === -->
{(value as string)}
{({ tag: "content" } satisfies TypeShape)}
{maybeItem!.name}
{obj?.prop}
{maybeString ?? fallback}

<!-- Complex type assertions (inside parentheses) -->
{(value as string | null)}
{(aAndB as A & B)}
{(value as Item[])}
{(mapUnknown as Map<string, number>)}
{(readonlyUnknown as readonly string[])}
{(tupleUnknown as [string, number])}
{(conditionalUnknown as T extends string ? string : number)}
{("key" as keyof typeof something)}

<!-- === ATTRIBUTE EXPRESSIONS === -->
<div
  data-as={(value as string)}
  data-satisfies={({ tag: "attr" } satisfies TypeShape)}
  data-non-null={maybeItem!.name}
  data-optional={obj?.prop}
  data-nullish={maybeString ?? fallback}
  data-union={(value as string | null)}
  data-intersection={(aAndB as A & B).a}
  data-array={(value as Item[]).length}
  data-generic={(mapUnknown as Map<string, number>).size}
  data-readonly={(readonlyUnknown as readonly string[]).length}
  data-tuple={(tupleUnknown as [string, number])[0]}
  data-conditional={(conditionalUnknown as T extends string ? string : number)}
  data-keyof={("key" as keyof typeof something)}
/>

<!-- === SPREAD ATTRIBUTES === -->
<div {...(maybeProps as Record<string, unknown>)} />
<div {...(({ role: "status" }) satisfies Record<string, unknown>)} />
<div {...maybeProps!} />
<div {...(nestedProps?.attrs as Record<string, unknown>)} />
<div {...(maybeProps ?? {})} />

<!-- === BLOCK CONDITIONS === -->
{#if (value as string)}
  <p>if-parenthesized-as-assertion</p>
{/if}

{#if ({ tag: "if" } satisfies TypeShape)}
  <p>if-satisfies</p>
{/if}

{#if maybeItem!.name}
  <p>if-non-null</p>
{/if}

{#if obj?.prop}
  <p>if-optional-chain</p>
{/if}

{#if (maybeString ?? fallback)}
  <p>if-nullish</p>
{/if}

{#each maybeItems ?? [] as item}
  <p>{item.name}</p>
{/each}

{#each (value as Item[]) as item}
  <p>{item.id}</p>
{/each}

{#await promiseValue}
  <p>await-pending</p>
{/await}

{#key (obj?.prop ?? fallback)}
  <p>keyed</p>
{/key}

<!-- === DIRECTIVES === -->
<input bind:value={(inputValue as string)} />
<div class:satisfies={({ enabled: true } satisfies ActionArg).enabled} />
<div class:nonNull={maybeItem!.active} />
<div class:optional={obj?.prop} />
<div class:nullish={(obj?.prop ?? fallback) === "prop"} />
<div use:action={action} />
<div use:action={maybeProps ?? {}} />

<button on:click={(e: MouseEvent) => e.clientX} />
<button on:focus={(): void => {
  inputValue = "focus";
}} />
<button on:change={<T,>(arg: T) => arg} />
<button on:blur={(a: string, b: number) => `${a}${b}`} />

<!-- === CONST TAG === -->
{@const constSatisfies = ({ tag: "const" } satisfies TypeShape)}
{@const constNonNull = maybeItem!}
{@const constOptional = obj?.prop}
{@const constNullish = maybeString ?? fallback}

<!-- === SNIPPET PARAMETERS === -->
{#snippet primitive(x: number)}
  <p>{x}</p>
{/snippet}

{#snippet interfaceRef(item: Item)}
  <p>{item.name}</p>
{/snippet}

{#snippet optionalParam(x?: string)}
  <p>{x ?? fallback}</p>
{/snippet}

{#snippet destructured({ id, name }: Item)}
  <p>{id}:{name}</p>
{/snippet}

{#snippet restParams(...args: string[])}
  <p>{args.length}</p>
{/snippet}
