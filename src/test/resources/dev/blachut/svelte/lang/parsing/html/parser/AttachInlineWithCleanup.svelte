<div
	{@attach (node) => {
		node.textContent = "Hello"
		return () => console.log("cleanup")
	}}
>
	content
</div>
