<div
	{@attach condition ? (node) => {
		node.textContent = "Hello"
		return () => console.log("cleanup")
	} : null}
>
	content
</div>
