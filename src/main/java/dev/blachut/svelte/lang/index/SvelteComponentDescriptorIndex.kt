package dev.blachut.svelte.lang.index

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import dev.blachut.svelte.lang.SvelteHtmlFileType
import dev.blachut.svelte.lang.codeInsight.SvelteComponentDescriptor
import dev.blachut.svelte.lang.codeInsight.SveltePropsVisitor
import dev.blachut.svelte.lang.parsing.js.SvelteJSScriptContentProvider
import dev.blachut.svelte.lang.psi.SvelteHtmlFile
import java.io.DataInput
import java.io.DataOutput

class SvelteComponentDescriptorIndex : SingleEntryFileBasedIndexExtension<SvelteComponentDescriptor>() {
    override fun getName(): ID<Int, SvelteComponentDescriptor> = INDEX

    override fun getIndexer(): SingleEntryIndexer<SvelteComponentDescriptor> {
        return object : SingleEntryIndexer<SvelteComponentDescriptor>(false) {
            override fun computeValue(inputData: FileContent): SvelteComponentDescriptor? {
                val psiFile = inputData.psiFile as SvelteHtmlFile

                val jsElement =
                    SvelteJSScriptContentProvider.getJsEmbeddedContent(psiFile.instanceScript) ?: return null

                val propsVisitor = SveltePropsVisitor()
                jsElement.accept(propsVisitor)

                return SvelteComponentDescriptor(propsVisitor.props.associateWith { it }, emptyMap(), emptyMap())
            }

        }
    }

    override fun getValueExternalizer(): DataExternalizer<SvelteComponentDescriptor> {
        // todo MapDataExternalizer
        // todo IOUtil
        return object : DataExternalizer<SvelteComponentDescriptor> {
            override fun save(output: DataOutput, value: SvelteComponentDescriptor) {
                DataInputOutputUtil.writeMap(output, value.props, { output.writeUTF(it) }, { output.writeUTF(it) })
            }

            override fun read(input: DataInput): SvelteComponentDescriptor {
                val props = DataInputOutputUtil.readMap(input, { input.readUTF() }, { input.readUTF() })

                return SvelteComponentDescriptor(props, emptyMap(), emptyMap())
            }

        }
    }

    override fun getVersion(): Int = 10

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(SvelteHtmlFileType.INSTANCE) {}
    }

    companion object {
        val INDEX = ID.create<Int, SvelteComponentDescriptor>("SvelteComponentDescriptorIndex")

        fun forFile(virtualFile: VirtualFile, project: Project): SvelteComponentDescriptor? {
            return FileBasedIndex.getInstance().getFileData(
                INDEX,
                virtualFile,
                project
            )[FileBasedIndex.getFileId(virtualFile)]
        }
    }
}
