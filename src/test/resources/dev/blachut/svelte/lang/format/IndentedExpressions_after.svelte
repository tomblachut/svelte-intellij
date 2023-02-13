<div>
    {@html 'hello<script>virus()</script>'}
    { @html 'unescaped' }
    {@ html 'unescaped'}
    {@debug x}
    { @debug x }
    {@ debug x}
    {@const x = 1 + 1}
    { @const x = 1 + 1 }
    {@ const x = 1 + 1}
    {@unknown x}
</div>
